# Changelog

## [1.3.0]

### Fixed
- Fire Charges are now used up when they light a portal
- Spectators can no longer light portals, and adventure mode's building rules now apply to lighting them
- Frames that include Infested Cobblestone now light
- Water and other fluids can no longer be placed in or flow into portal blocks

### Changed
- A portal now lights and leads only between the Overworld and its own mining dimension, so a frame built anywhere else stays unlit
- Beds now explode and raids can no longer start in Overworld Mining, as in the other mining dimensions
- Portals built in a mining dimension now sit on its flat surface
- When no open site is near the destination, a portal now clears room for itself there, but never replaces chests, furnaces, and similar blocks, other portals, or unbreakable blocks
- Arriving entities now land in front of the portal, behind it if the front is blocked, or inside it as a last resort
- `/egmd regenerate` now moves players to clear ground at the Overworld spawn, reports how many players it moved, and fails with a count of any players it could not move
- End Mining prismarine veins now mix Prismarine, Prismarine Bricks, and Dark Prismarine instead of placing only Prismarine
- The portal search radius comment in the config file no longer repeats the default value, which NeoForge already lists below it

### Added
- Hostile mobs, and anything carrying one, can no longer enter a mining dimension by any route, including portals
- `/egmd regenerate` now closes the dimension's portals until the server stops, and a portal into a closed dimension says so
- Players who log in to a dimension closed for regeneration are moved to the Overworld spawn with a message saying why
- Players who log in inside solid blocks in a mining dimension, such as after it is regenerated, are moved up to the first space where they fit, or to the Overworld spawn when there is none
- Clicking a fire burning on the bottom of a frame now lights the portal
- Lighting a portal now plays the igniter's sound
- A portal that finds no safe place on the other side now says so
- Portals have a new animated texture

## [1.2.0]

**Note**: License changed from All Rights Reserved to MIT.  I meant to do that before I released it, but I forgot.

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
