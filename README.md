# Ejafi Games Mining Dimensions (EGMD)

Ejafi Games Mining Dimensions (EGMD) is a NeoForge mod that adds extra-high-resource mining dimensions to your Minecraft world. These dimensions are designed to provide a flat, easy-to-mine environment with significantly increased ore spawn rates, making resource gathering more efficient and less destructive to your main world.

## Player Guide

### Accessing the Mining Dimensions

To enter a mining dimension, you must build a portal frame similar to a Nether portal (minimum 2x3 inner area) and ignite it with a **Flint and Steel**.

#### 1. Overworld Mining Dimension
*   **Frame Material:** `Cobblestone`
*   **Features:** Contains all standard overworld ores (Coal, Iron, Copper, Gold, Redstone, Lapis, Diamond, Emerald) at much higher frequencies. The terrain is flat "Mining Plains".

#### 2. Nether Mining Dimension
*   **Frame Material:** `Netherrack`
*   **Features:** Contains Nether ores like Quartz and Gold. The terrain is flat "Nether Mining Wastes".

#### 3. End Mining Dimension
*   **Frame Material:** `End Stone`
*   **Features:** Contains End-related resources and unique ores. The terrain is flat "End Mining Wastes".
*   **New Block:** Look out for **Pearl Blocks** found in this dimension!

### Tips for Mining
*   All mining dimensions are generated as flat worlds to make navigation and automated mining easier.
*   Bedrock is located at the bottom (y=0 or below depending on the dimension type).

---

## Commands

EGMD includes administrative commands to manage the mining dimensions.

### `egmd regenerate`
**Usage:** `/egmd regenerate <dimension>`
**Permission Level:** 2 (OP)

This command is used to prepare a dimension for regeneration. 
1.  It teleports all players currently in the specified mining dimension back to the Overworld spawn.
2.  It provides the administrator with manual instructions to complete the regeneration (requires server restart and folder deletion).

---

## Development

This mod is built using NeoForge.

### Mapping Names
EGMD uses official Mojang mapping names. Refer to the [Mojang License](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md) for details.

### Additional Resources
*   [NeoForged Documentation](https://docs.neoforged.net/)
*   [NeoForged Discord](https://discord.neoforged.net/)
