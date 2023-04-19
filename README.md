# **Command Block IDE** <img align="right" src="src/main/resources/assets/commandblockide/icon.png" />

[![License](https://img.shields.io/github/license/arm32x/command-block-ide?label=license&style=flat-square)](https://github.com/arm32x/command-block-ide/blob/mc-release/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/arm32x/command-block-ide?logo=github&style=flat-square)](https://github.com/arm32x/command-block-ide/issues)
[![GitHub Repo stars](https://img.shields.io/github/stars/arm32x/command-block-ide?logo=github&style=flat-square)](https://github.com/arm32x/command-block-ide/stargazers)
[![GitHub last commit](https://img.shields.io/github/last-commit/arm32x/command-block-ide?logo=github&style=flat-square)](https://github.com/arm32x/command-block-ide/commits)
[![CurseForge downloads](https://cf.way2muchnoise.eu/short_483538_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/mc-mods/command-block-ide)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/command-block-ide?color=00af5c&logo=modrinth&style=flat-square)](https://modrinth.com/mod/command-block-ide/)

Replaces the command block GUI to allow editing multiple command blocks at once.

<img alt="Requires Fabric API" src="https://i.imgur.com/HabVZJR.png" width="179" height="60" />

## Features <small>(in the latest version)</small>

  - Edit multiple command blocks at once.
  - Apply changes without closing the GUI.
  - Edit functions in datapacks (beta).
  - Split a single command across multiple lines.

#### Planned

  - Move command blocks around within the GUI.
  - Add additional commands to the chain from within the GUI.

## Screenshots

![Screenshot](https://i.imgur.com/uHdvSSL.png)

## FAQ

#### How do you make a command block conditional?

Shift-click on the command block icon in the GUI.

#### What is the `commandblockide.bin` file in my game directory?

It's used to store the multi-line versions of commands, since Minecraft
doesn't natively support them. If this file is deleted, all commands
will be collapsed to their single-line versions.

#### Why can't other players see multi-line commands in multiplayer?

Multi-line commands are stored in the `commandblockide.bin` file on your
computer, which is not synced to other players in multiplayer. If you
need to work on commands collaboratively, you will have to stick to
single-line commands for the time being.

#### Why has my multi-line command been collapsed to one line?

Either the single-line and multi-line versions of the command are out of
sync or there is no multi-line version available. This can happen when
logging in to Minecraft from a different computer or when another player
edits the command.

#### Can you port this to Forge?

No.

#### Can you port this to *\<older Minecraft version>*?

No.

#### Can you update this to *\<newer Minecraft version>*?

Maybe.
