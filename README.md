# Ejafi Games Mining Dimensions (EGMD)

EGMD is a NeoForge mod for Minecraft 1.21.1 that adds three flat, ore-rich mining dimensions, so you can mine without damaging your main world. It also adds the Haunted dimension, a dark forest at permanent midnight that is full of Spiders and Slimes.

## Player Guide

### Portals

Build a frame like a Nether portal, with an inside from 2x3 to 21x21, and put one of the dimension's corner blocks in each of its four corners. Light it by clicking the frame, or a fire burning on the bottom of the frame, with Flint and Steel or a Fire Charge. A portal leads from the Overworld to its own dimension and lights only in those two dimensions. In the Overworld, a frame with the wrong corners tells you which corner blocks it needs.

Corners are needed only to light a portal. This is a protection against accidental portals in dimensions like Nether Mining or End Mining, where every tunnel is a frame. A lit portal keeps working without the corners.

When the other side has no portal, the mod builds one with the corner blocks of the portal you came through. It uses the dimension's default corner block for any corner that doesn't qualify, and for Sand, Red Sand, or Gravel in a bottom corner. In a mining dimension the portal sits on the flat surface, and elsewhere it sits on the ground. It clears room for itself when needed, but never replaces another portal, an unbreakable block, or a block that stores data, such as a chest, sign, or spawner.

When no safe spot exists, you stay where you are and see a message. Otherwise you arrive in front of the portal, behind it if the front is blocked, or inside it if both sides are blocked.

### The Dimensions

| Dimension | Frame | Corners | Default corner | Layers, top to bottom | Surface |
|---|---|---|---|---|---|
| Overworld Mining | Cobblestone | Dirt, Coarse Dirt, Rooted Dirt, Grass Block, Podzol, Mycelium, Gravel, Sand, or Red Sand | Dirt | Grass Block, 2 Dirt, 64 Stone, 64 Deepslate, Bedrock | Y 68 |
| Nether Mining | Netherrack | Cobblestone, Mossy Cobblestone, Cobbled Deepslate, Stone, Andesite, Diorite, Granite, Deepslate, or Tuff | Cobblestone | 128 Netherrack, Bedrock | Y 129 |
| End Mining | End Stone | Netherrack, Obsidian, Crying Obsidian, Prismarine, Prismarine Bricks, or Dark Prismarine | Netherrack | 64 End Stone, Bedrock | Y 65 |
| Haunted | Bone Block | Carved Pumpkin or Jack o'Lantern | Jack o'Lantern | Hills, caves, and oceans, as in the Overworld | Varies |

Infested blocks count as the block they imitate, in both the frame and the corners.

**Overworld Mining** has far more Coal, Iron, Copper, Gold, Redstone, Lapis Lazuli, Diamond, and Emerald ore than the Overworld, in stone and deepslate forms, along with Amethyst Geodes and patches of Dirt, Gravel, Granite, Diorite, Andesite, and Tuff. It is always midday there.

**Nether Mining** has Nether Gold Ore, Nether Quartz Ore, Ancient Debris, Magma Blocks, and Soul Sand. As in the Nether, one block of travel equals eight in the Overworld, water evaporates, and Respawn Anchors work.

**End Mining** has Prismarine veins mixed with Prismarine Bricks and Dark Prismarine, along with Sea Lanterns and Pearl Blocks. A Pearl Block mined with a pickaxe drops 1 to 4 Ender Pearls, plus up to one more per Fortune level, or drops itself with Silk Touch.

**Haunted** is a dark forest where it is always midnight, though the moon still goes through its phases. Its terrain, caves, oceans, and ores are like the Overworld's. It is covered in Dark Oak trees, huge mushrooms, Pumpkins, Carved Pumpkins, Jack o'Lanterns, Cobwebs, and patches of Soul Sand, and it has no villages or other structures. Spiders and Slimes spawn often, along with Zombies and Skeletons, and torches keep them away as they do in the Overworld. Slimes spawn on the surface between Y 51 and 69 every night, whatever the moon phase, and in slime chunks below Y 40.

### Rules

These rules apply in every dimension the mod adds.

* Beds don't work, and they don't explode.
* Raids can't start.

These rules apply only in the mining dimensions.

* No mobs spawn, and hostile mobs can't enter. A Wither you build there is the exception.
* If you log in inside solid blocks, you move up to the first open space, or to the Overworld spawn if there is none.

## Configuration

`config/egmd-common.toml` has one setting, which you can also change from the mod's config screen.

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `portalSearchRadius` | 128 | 16 to 512 | Radius in blocks to search for an existing portal before building one. Nether Mining divides it by 8, with a minimum of 16. Large values cause a lag spike the first time a portal searches an unexplored area. |

## Development

[AGENTS.md](AGENTS.md) covers the build, the tests, and the source layout. EGMD uses Mojang's official mapping names with Parchment parameter names, under the [Mojang License](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md).
