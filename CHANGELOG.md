# Changelog

## [2.1.0]

### Added
- The Haunted dimension, reached through a Bone Block portal with carved pumpkin or jack o'lantern corners.
- The Haunted dimension is always midnight, and spiders and slimes spawn there often.
- Slimes spawn on the Haunted surface every night, whatever the moon phase.

## [2.0.0]

### Changed
- Portal frames need corner blocks to light. Portals that are already lit keep working.
- Beds in a mining dimension no longer explode.
- Nether Mining has less Ancient Debris.
- The Pearl Block is in the Natural Blocks tab.

### Added
- Silk Touch and Fortune work on the Pearl Block.
- A frame with the wrong corners shows which corner blocks it needs.

### Fixed
- A Wither built in a mining dimension no longer disappears.
- Portals no longer link to a portal location that holds a different block.

### Removed
- The `/egmd regenerate` command.

## [1.3.0]

### Fixed
- Lighting a portal with a Fire Charge uses up the Fire Charge.
- Spectators cannot light portals.
- Adventure mode's building rules apply to lighting portals.
- Frames that include Infested Cobblestone light.
- Fluids cannot be placed in or flow into portal blocks.

### Changed
- A portal links only the Overworld and its own mining dimension.
- A frame built in any other dimension does not light.
- Beds explode in Overworld Mining, as they do in the other mining dimensions.
- Raids cannot start in Overworld Mining.
- A portal built in a mining dimension sits on the dimension's flat surface.
- A portal clears room for itself when no open site is near its destination.
- A portal never replaces containers, other portals, or unbreakable blocks when it clears room.
- An arriving entity lands in front of the portal.
- An arriving entity lands behind the portal when the front is blocked, and inside it when both sides are blocked.
- `/egmd regenerate` moves players to clear ground at the Overworld spawn.
- `/egmd regenerate` reports how many players it moved.
- `/egmd regenerate` fails and reports a count when it cannot move every player.
- End Mining prismarine veins mix Prismarine, Prismarine Bricks, and Dark Prismarine.

### Added
- Hostile mobs cannot enter a mining dimension.
- An entity carrying a hostile mob cannot enter a mining dimension.
- `/egmd regenerate` closes the dimension's portals until the server stops.
- A portal into a closed dimension tells the player that the dimension is closed.
- A player who logs in to a dimension closed for regeneration moves to the Overworld spawn and receives a message.
- A player who logs in inside solid blocks in a mining dimension moves up to the first space that fits them.
- A player who logs in inside solid blocks with no space above moves to the Overworld spawn.
- Clicking a fire on the bottom of a frame lights the portal.
- Lighting a portal plays the igniter's sound.
- A portal tells the player when it finds no safe place on the other side.
- Portals have an animated texture.

## [1.2.0]

**Note**: License changed from All Rights Reserved to MIT. I meant to do that before I released it, but I forgot.

### Fixed
- Teleporting now finds existing destination portals even when their chunks are unloaded, instead of building a duplicate portal nearby
- Portal search in the Nether Mining dimension now accounts for its 8x coordinate compression, so portals no longer link to far-away portals
- Stale portal points of interest (where the portal block was removed) no longer crash teleportation
- Arriving through a portal now plays the portal sound and keeps the destination chunk loaded during the transition
- Breaking a single portal block now collapses the whole portal instead of leaving a floating remnant
- Auto-built portals no longer lose their top frame when placed at the build height limit, and no longer place their platform below the world floor
- Pearl Block now appears in the Building Blocks creative tab
- The config screen now shows a proper name and tooltip for the portal search radius

### Changed
- The mod now accepts any NeoForge 21.1.x version instead of requiring the exact build it was compiled against
- Portals now use the vanilla stand-in delay (about 4 seconds in survival, governed by the same game rules as Nether portals) and show the vanilla portal overlay, instead of teleporting instantly
- Portal blocks no longer have inventory items, matching vanilla portals; portal items already in inventories or containers will disappear
- Auto-built X-axis portals are no longer mirrored, and arriving entities now spawn centered in front of the portal instead of beside it
- Portal creation no longer fails when the destination surface is below Y=0
- `/egmd regenerate` now prints the actual dimension folder path instead of assuming the world folder is named `world`

### Added
- Support for Create: Unbreakable series ([Curse](https://www.curseforge.com/minecraft/mc-mods/create-unbreakable-netherite-tools-series))
- Portals now play ambient portal sounds and emit particles tinted to match each portal variant

## [1.1.0]

### Fixed
- Portal teleportation no longer causes server lag spikes
- Fixed portals sometimes activating incorrectly in singleplayer
- Fixed a rare bug where portal placement could go wrong near world borders

### Changed
- Portal search radius is now configurable (default: 128 blocks)
- Rebalanced all Overworld Mining ore generation to approximately 10× vanilla spawn rates
- Overworld Mining ore veins are now appropriately sized and no longer stack with vanilla ore passes
- Diamond veins are significantly smaller and less frequent
- Coal ore in Overworld Mining now generates from Y=-64 to Y=64 (previously Y=0 to Y=64) and spawns more frequently
- Pearl block veins are now smaller

### Added
- Copper and emerald ore now spawn in the Overworld Mining dimension
