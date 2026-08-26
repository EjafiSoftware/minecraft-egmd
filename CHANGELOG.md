# Changelog

## [1.2.0]

### Fixed
- Auto-built X-axis portals are no longer mirrored, and arriving entities now spawn centered in front of the portal instead of beside it
- Portal creation no longer fails when the destination surface is below Y=0
- `/egmd regenerate` now prints the actual dimension folder path instead of assuming the world folder is named `world`

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
