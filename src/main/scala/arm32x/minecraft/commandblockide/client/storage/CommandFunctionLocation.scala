package arm32x.minecraft.commandblockide.client.storage

import net.minecraft.util.Identifier

case class CommandFunctionLocation(
    isSingleplayer: Boolean,
    world: String,
    function: Identifier,
    lineIndex: Int,
)
