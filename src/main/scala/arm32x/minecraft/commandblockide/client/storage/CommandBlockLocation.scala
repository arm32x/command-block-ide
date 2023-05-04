package arm32x.minecraft.commandblockide.client.storage

import net.minecraft.util.math.BlockPos

case class CommandBlockLocation(
    isSingleplayer: Boolean,
    world: String,
    pos: BlockPos,
)
