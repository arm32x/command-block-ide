package arm32x.minecraft.commandblockide.server.function;

import arm32x.minecraft.commandblockide.mixin.server.PathPackResourcesAccessor;
import arm32x.minecraft.commandblockide.mixin.server.ServerFunctionLibraryAccessor;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FunctionIO {
    /**
     * Loads a function from the filesystem.
     *
     * @param server The Minecraft server the function is loaded in.
     * @param functionId The namespaced ID of the function to save.
     * @return Either the lines read from the {@code .mcfunction} file, or a
     *         feedback message to show to the user indicating what went wrong.
     */
    public static Either<List<String>, Component> loadFunction(MinecraftServer server, Identifier functionId) {
        // TODO: Use proper error handling instead of returning Text.
        // TODO: Make loading functions use a CompletableFuture so a loading
        //       screen can be shown.

        // Convert the function ID ('some_datapack:some_function') to a resource
        // path ('some_datapack:functions/some_function.mcfunction').
        var resourceFinder = ServerFunctionLibraryAccessor.getResourceFinder();
        var functionResourcePath = resourceFinder.idToFile(functionId);

        // Figure out which resource pack the function is in.
        var resourceManager = server.getResourceManager();
        var functionResource = resourceManager.getResource(functionResourcePath);
        if (functionResource.isEmpty()) {
            return Either.right(Component.translatable("commandBlockIDE.loadFunction.failed.noResourcePack", functionId));
        }
        var pack = functionResource.get().source();

        // Only directory-based resource packs are supported.
        if (pack instanceof FilePackResources) {
            return Either.right(Component.translatable("commandBlockIDE.loadFunction.failed.zipNotSupported", functionId).withStyle(ChatFormatting.RED));
        } else if (!(pack instanceof PathPackResources)) {
            return Either.right(Component.translatable("commandBlockIDE.loadFunction.failed.packClassNotSupported", functionId, pack.getClass().getSimpleName()).withStyle(ChatFormatting.RED));
        }
        var directoryPack = (PathPackResources)pack;

        // Get the path to the function resource in the filesystem.
        DataResult<Path> pathResult = getFilesystemPathOfResource(directoryPack, PackType.SERVER_DATA, functionResourcePath);
        if (pathResult.result().isEmpty()) {
            String errorMessage = pathResult.error().get().message();
            return Either.right(Component.translatable("commandBlockIDE.loadFunction.failed.invalidPath", functionId, functionResourcePath, errorMessage));
        }
        Path path = pathResult.result().get();

        // Read the content of the mcfunction file.
        try {
            return Either.left(Files.readAllLines(path));
        } catch (IOException e) {
            LOGGER.error("IO exception occurred while loading function '" + functionId.toString() + "':", e);
            return Either.right(Component.translatable("commandBlockIDE.loadFunction.failed.ioException", functionId).withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Saves a function to the filesystem.
     *
     * @param server The Minecraft server the function is loaded in.
     * @param functionId The namespaced ID of the function to save.
     * @param lines The lines to write into the {@code .mcfunction} file.
     * @return The feedback message to show to the user.
     */
    public static Component saveFunction(MinecraftServer server, Identifier functionId, List<String> lines) {
        // TODO: Use proper error handling instead of returning Text.
        // TODO: Make saving functions use a CompletableFuture so errors can be
        //       properly shown to the user.

        // Convert the function ID ('some_datapack:some_function') to a resource
        // path ('some_datapack:functions/some_function.mcfunction').
        var resourceFinder = ServerFunctionLibraryAccessor.getResourceFinder();
        var functionResourcePath = resourceFinder.idToFile(functionId);

        // Figure out which resource pack the function is in.
        var resourceManager = server.getResourceManager();
        var functionResource = resourceManager.getResource(functionResourcePath);
        if (functionResource.isEmpty()) {
            // Error saving function '...': Not found in any datapack.
            return Component.translatable("commandBlockIDE.saveFunction.failed.noResourcePack", functionId.toString());
        }
        var pack = functionResource.get().source();

        // Only directory-based resource packs are supported.
        if (pack instanceof FilePackResources) {
            return Component.translatable("commandBlockIDE.saveFunction.failed.zipNotSupported", functionId.toString()).withStyle(
                ChatFormatting.RED);
        } else if (!(pack instanceof PathPackResources)) {
            return Component.translatable("commandBlockIDE.saveFunction.failed.packClassNotSupported", functionId.toString(), pack.getClass().getSimpleName()).withStyle(ChatFormatting.RED);
        }
        var directoryPack = (PathPackResources)pack;

        // Get the path to the function resource in the filesystem.
        DataResult<Path> pathResult = getFilesystemPathOfResource(directoryPack, PackType.SERVER_DATA, functionResourcePath);
        if (pathResult.result().isEmpty()) {
            String errorMessage = pathResult.error().get().message();
            // Error saving function '...': Invalid path '...': ...
            return Component.translatable("commandBlockIDE.saveFunction.failed.invalidPath", functionId.toString(), functionResourcePath.toString(), errorMessage);
        }
        Path path = pathResult.result().get();

        // Replace the content of the mcfunction file.
        try {
            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("IO exception occurred while saving function '" + functionId.toString() + "':", e);
            return Component.translatable("commandBlockIDE.saveFunction.failed.ioException", functionId.toString()).withStyle(ChatFormatting.RED);
        }

        return Component.translatable("commandBlockIDE.saveFunction.success.file", functionId.toString());
    }

    /**
     * Determines the filesystem path to a resource in a directory-based
     * resource pack.
     *
     * <p>This code implements the same logic as DirectoryResourcePack.open,
     * except without opening the file at the end. If an error occurs, it is
     * returned as a {@link DataResult}.</p>
     *
     * <p>Since {@code DataResult} is a part of the open-source DataFixerUpper,
     * it should be more stable between updates than other Minecraft code.</p>
     *
     * @param pack The resource pack containing the resource.
     * @param resourceType Whether the resource is from a client-side resource
     *                     pack or server-side datapack.
     * @param resourcePath The path to the resource inside the resource pack.
     * @return A filesystem path to the same resource as {@code resourcePath}.
     */
    @SuppressWarnings("SameParameterValue")
    private static DataResult<Path> getFilesystemPathOfResource(PathPackResources pack, PackType resourceType, Identifier resourcePath) {
        Path root = ((PathPackResourcesAccessor)pack).getRoot();
        Path namespaceDir = root.resolve(resourceType.getDirectory()).resolve(resourcePath.getNamespace());

        return FileUtil.decomposePath(resourcePath.getPath())
            .map(segments -> FileUtil.resolvePath(namespaceDir, segments));
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
