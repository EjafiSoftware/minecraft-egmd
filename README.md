# Ejafi Games Mining Dimensions (EGMD)

Ejafi Games Mining Dimensions (EGMD) is a NeoForge mod that adds extra-high-resource mining dimensions to your Minecraft world. These dimensions are designed to provide a flat, easy-to-mine environment with significantly increased ore spawn rates, making resource gathering more efficient and less destructive to your main world.

## Player Guide

### Accessing the Mining Dimensions

To enter a mining dimension, build a portal frame in the Overworld the same way as a Nether portal (minimum 2x3 inner area, maximum 21x21) and light it with a **Flint and Steel** or a **Fire Charge**. Each portal connects the Overworld to its own mining dimension, and the matching portal on the other side takes you back. A portal can be lit only in the Overworld or in its own mining dimension.

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
*   No hostile mobs spawn in or travel into the mining dimensions.
*   Beds do not work in the mining dimensions; plan to return to the Overworld to sleep.

---

## Commands

EGMD includes administrative commands to manage the mining dimensions.

### `egmd regenerate`
**Usage:** `/egmd regenerate <dimension>`
**Permission Level:** 2 (OP)

This command prepares a mining dimension for regeneration.
1.  It closes the dimension's portals until the server stops, so nobody can re-enter it.
2.  It moves every player in the dimension to a safe spot at the Overworld spawn. If any player cannot be moved, the command fails and says so.
3.  It gives the administrator the remaining manual steps: stop the server, delete the dimension's folder, and restart.

Players who were offline in a regenerated dimension are moved out of the new terrain when they next log in.

---

## Development

This mod is built using NeoForge.

### Mapping Names
EGMD uses official Mojang mapping names. Refer to the [Mojang License](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md) for details.

### Additional Resources
*   [NeoForged Documentation](https://docs.neoforged.net/)
*   [NeoForged Discord](https://discord.neoforged.net/)
