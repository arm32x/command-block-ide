package arm32x.minecraft.commandblockide.client;

public interface Dirtyable {
    boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);

    boolean isDirty();
}
