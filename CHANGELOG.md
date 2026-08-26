# Changelog

## [1.2.0]

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
- Portals now use the vanilla stand-in delay (about 4 seconds in survival, governed by the same game rules as Nether portals) and show the vanilla portal overlay, instead of teleporting instantly
- Portal blocks no longer have inventory items, matching vanilla portals; portal items already in inventories or containers will disappear
- Auto-built X-axis portals are no longer mirrored, and arriving entities now spawn centered in front of the portal instead of beside it
- Portal creation no longer fails when the destination surface is below Y=0
- `/egmd regenerate` now prints the actual dimension folder path instead of assuming the world folder is named `world`

### Added
- Support for Create: Unbreakable series ([Curse](https://www.curseforge.com/minecraft/mc-mods/create-unbreakable-netherite-tools-series))

## [1.1.0]

### Fixed
- Portal teleportation no longer causes server lag spikes
- Fixed portals sometimes activating incorrectly in singleplayer
- Fixed a rare bug where portal placement could go wrong near world borders

### Changed
- Portal search radius is now configurable (default: 128 blocks)
- Rebalanced all Overworld Mining ore generation — veins are now appropriately sized and no longer stack with vanilla ore passes; tuned to approximately 10× vanilla spawn rates
- Diamond veins are significantly smaller and less frequent
- Coal ore in Overworld Mining now generates from Y=-64 to Y=64 (previously Y=0 to Y=64) and spawns more frequently
- Pearl block veins are now smaller

### Added
- Copper and emerald ore now spawn in the Overworld Mining dimension
