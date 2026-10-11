# Ejafi Games Mining Dimensions (EGMD)

Ejafi Games Mining Dimensions (EGMD) is a NeoForge mod for Minecraft 1.21.1 that adds three mining dimensions to your world. Each one is a flat world packed with ore, so you can gather resources without tearing up your main world.

## Player Guide

### Reaching the Mining Dimensions

Build a portal frame in the Overworld the same way as a Nether portal, with an inner area from 2x3 up to 21x21, and light it with **Flint and Steel** or a **Fire Charge**. You can also light it by clicking a fire burning on the bottom of the frame. Each portal leads from the Overworld to its own mining dimension, and the matching portal on the other side takes you back. A portal lights only in the Overworld or in its own mining dimension.

When no portal exists on the other side, one is built for you. In a mining dimension it sits on the flat surface. If no open spot is nearby, the portal clears room for itself, but it never replaces chests, furnaces, and similar blocks, other portals, or unbreakable blocks. If it still finds no safe place, it tells you so and doesn't send you through. You arrive in front of the portal, behind it if the front is blocked, or inside it as a last resort.

### The Dimensions

| Dimension | Frame | Layers, top to bottom | Surface |
|---|---|---|---|
| Overworld Mining | Cobblestone | Grass Block, 2 Dirt, 64 Stone, 64 Deepslate, Bedrock | Y 68 |
| Nether Mining | Netherrack | 128 Netherrack, Bedrock | Y 129 |
| End Mining | End Stone | 64 End Stone, Bedrock | Y 65 |

**Overworld Mining** places Coal, Iron, Copper, Gold, Redstone, Lapis Lazuli, Diamond, and Emerald ore at far higher rates than the Overworld, in both their stone and deepslate forms. It also has patches of Dirt, Gravel, Granite, Diorite, Andesite, and Tuff, and Amethyst Geodes. It is always midday there.

**Nether Mining** places Nether Gold Ore, Nether Quartz Ore, and Ancient Debris, with patches of Magma Blocks and Soul Sand. As in the Nether, one block of travel equals eight in the Overworld, water evaporates, and Respawn Anchors work.

**End Mining** places veins of Prismarine mixed with Prismarine Bricks and Dark Prismarine, along with Sea Lanterns and Pearl Blocks. A Pearl Block is unique to this mod and drops 1 to 4 Ender Pearls when mined with a pickaxe.

### Tips

* No mobs spawn in the mining dimensions, and hostile mobs can't enter them by any route, including portals.
* Beds explode if you try to sleep in a mining dimension, so go back to the Overworld to sleep.
* Raids can't start in a mining dimension.

---

## Configuration

`config/egmd-common.toml` has one setting, which you can also change from the mod's config screen in the Mods menu.

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `portalSearchRadius` | 128 | 16 to 512 | How far, in blocks, a portal looks for an existing portal on the other side before building a new one. When the other side is Nether Mining, the radius is divided by 8, with a minimum of 16. |

---

## Commands

### `egmd regenerate`
**Usage:** `/egmd regenerate <dimension>`

**Permission Level:** 2 (OP)

This command prepares a mining dimension for regeneration.
1.  It closes the dimension's portals until the server stops, so nobody can enter it.
2.  It moves every player in the dimension to a safe spot at the Overworld spawn. If any player can't be moved, the command fails and says how many.
3.  It gives the administrator the remaining manual steps: stop the server, delete the dimension's folder, and restart.

A player who logs in to a dimension that is still closed is moved to the Overworld spawn. A player who logs in inside solid blocks in a mining dimension, as happens after its terrain is regenerated, is moved up to the first space where they fit, or to the Overworld spawn when there is none above.

---

## Development

[AGENTS.md](AGENTS.md) covers the build, the tests, and the source layout.

### Mapping Names
EGMD uses the official Mojang mapping names, with Parchment parameter names. Refer to the [Mojang License](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md) for details.
