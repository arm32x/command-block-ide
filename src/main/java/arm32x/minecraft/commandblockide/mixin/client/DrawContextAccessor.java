package arm32x.minecraft.commandblockide.mixin.client;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public interface DrawContextAccessor {
    @Invoker void invokeDrawTexturedQuad(Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int color);
}
