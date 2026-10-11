# AGENTS.md

Guidance for coding agents working in this repository.

## Project

**Ejafi Games Mining Dimensions (EGMD)** is a NeoForge mod for Minecraft 1.21.1 with the mod ID `egmd`. It adds three flat, ore-rich mining dimensions, each reached through its own portal from the Overworld. The Minecraft, NeoForge, and library versions are in `gradle.properties`, and the NeoForge API reference for this version is [docs.neoforged.net/docs/1.21.1](https://docs.neoforged.net/docs/1.21.1/).

| Dimension | Frame block | Portal block | Biome |
|---|---|---|---|
| `egmd:overworld_mining` | Cobblestone | `egmd:mining_portal` | `egmd:mining_plains` |
| `egmd:nether_mining` | Netherrack | `egmd:nether_mining_portal` | `egmd:nether_mining_wastes` |
| `egmd:end_mining` | End Stone | `egmd:end_mining_portal` | `egmd:end_mining_wastes` |

## Commands

| Command | What it does |
|---|---|
| `./gradlew build` | Spotless check with palantir-java-format, compile under Error Prone with NullAway at error severity, unit tests, and the jar in `build/libs/` |
| `./gradlew runGameTestServer` | Runs every GameTest in the `egmd` namespace and exits |
| `./gradlew runData` | Regenerates `src/generated/resources/` |
| `./gradlew spotlessApply` | Formats every Java source set |
| `./gradlew runClient`, `./gradlew runServer` | Dev runs. Both load the `gameTest` source set and enable the `egmd` GameTest namespace, so `/test` works in them. |

**The gate** is `./gradlew build` followed by `./gradlew runGameTestServer`, and both must pass before work is done. CI (`.github/workflows/build.yml`) runs the same two commands.

The dev runs need JDK 21. `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 21, and Gradle downloads one from the URLs in that file when none is installed. The compile toolchain is also Java 21, set in `build.gradle`.

## Source layout

### `src/main/java`

```
com.ejafi.egmd/
├── EGMD.java                              # @Mod entry point: calls each feature's register(IEventBus) and registers PortalConfig.SPEC as the COMMON config
├── portal/
│   ├── PortalRegisters.java               # Block, POI type, and portal_spark particle registers; getPortalBlock(variant)
│   ├── PortalConfig.java                  # ModConfigSpec with portalSearchRadius (16 to 512, default 128); its description feeds the config comment and the screen tooltip
│   ├── PortalMessages.java                # Translation keys for the action-bar messages a portal shows
│   ├── PortalVariant.java                 # Record of one portal: registry ID, MiningDimension, frame block, map color, tint color
│   ├── PortalVariants.java                # The OVERWORLD, NETHER, and END variants
│   ├── MiningPortalBlock.java             # The portal block: chooseRoute() and travel, pane shape, ambient sound and tinted sparks, collapse, fluid refusal
│   ├── MiningPortalShape.java             # Reads a frame through BlockGetter, validates its interior size, and fills it with portal blocks
│   ├── PortalIgnitionHandler.java         # RightClickBlock subscriber that lights an empty frame; owns the egmd:portal_igniters tag key
│   ├── ArrivalTarget.java                 # Where an exit portal goes: OnFlatSurface in a mining dimension, or OnTerrain elsewhere
│   ├── ExitPortalFinder.java              # Finds an existing exit portal through the POI search, or builds one at a site PortalPositionFinder picks
│   ├── PortalPositionFinder.java          # Chooses a PortalSite for an ArrivalTarget: spiral search for an open site, else a forced site
│   ├── PortalSite.java                    # An Open site, or a Forced site whose exit cells must be cleared
│   ├── PortalFootprint.java               # The frame, portal, exit, and platform cells of a generated portal; judges canHost and canForce
│   ├── PortalStructureBuilder.java        # Writes a portal, its frame, and its platform to a LevelWriter; clears the exit of a forced site
│   ├── PortalExit.java                    # Arrival position and yaw in front of, behind, or inside a portal rectangle
│   ├── PortalExitSelector.java            # Picks the first safe arrival for an entity: front, then back, else inside
│   └── client/
│       ├── PortalClient.java              # @Mod(dist = CLIENT): config screen, portal block tints, portal_spark particle provider
│       └── PortalSparkParticle.java       # Translucent portal particle tinted from a ColorParticleOption
├── dimension/
│   ├── MiningDimension.java               # The three mining dimensions: level key, min Y, flat layers, computeSurfaceY(), findByLevelKey()
│   ├── DimensionMessages.java             # Translation keys for /egmd regenerate output and login messages
│   ├── HostileMobGuard.java               # admits() decision, and the EntityJoinLevelEvent subscriber that cancels hostile mobs in a mining dimension
│   ├── PendingRegenerations.java          # Dimensions closed by /egmd regenerate; cleared on ServerStoppedEvent
│   ├── PlayerRelocator.java               # Moves a player to the Overworld spawn; PlayerLoggedInEvent subscriber that chooses a LoginRescue
│   ├── OverworldMove.java                 # Arrived, or Canceled when the teleport did not happen
│   ├── DimensionRegenerator.java          # Closes a mining dimension, evacuates its players, and returns a RegenerationResult
│   ├── RegenerationResult.java            # NotMiningDimension, PlayersStranded, or Evacuated with the dimension's storage folder
│   └── RegenerationCommand.java           # RegisterCommandsEvent subscriber for /egmd regenerate <dimension>
├── pearl/
│   └── PearlBlocks.java                   # pearl_block block and item registers; adds the item to the Building Blocks tab
└── datagen/
    ├── DataGatherer.java                  # GatherDataEvent subscriber that registers every provider and the datapack registry set
    ├── EgmdBlockStateProvider.java        # Portal and pearl_block block states, block models, and the pearl_block item model
    ├── EgmdLanguageProvider.java          # en_us translations
    ├── EgmdBlockTagsProvider.java         # minecraft:mineable/pickaxe
    ├── EgmdItemTagsProvider.java          # egmd:portal_igniters
    ├── EgmdBlockLootProvider.java         # pearl_block loot table (1 to 4 Ender Pearls)
    ├── EgmdParticleDescriptionProvider.java # portal_spark sprites (vanilla generic_0 to generic_7)
    ├── MiningOre.java                     # One ore: name, OreConfiguration, vein count per chunk, height range
    ├── MiningOres.java                    # Every mod ore, listed once per dimension; bootstraps their configured and placed features
    ├── MiningBiomes.java                  # The three biome keys and definitions; each places its dimension's mod ores, then vanilla ones
    └── MiningLevels.java                  # Dimension types and level stems, built from MiningDimension
```

### Test source sets

```
src/testFixtures/java/com/ejafi/egmd/
├── BlockPosFaker.java                     # Random BlockPos within the build height and a horizontal limit
├── dimension/
│   └── MiningDimensionFaker.java          # MiningDimension from a generated level key, min Y, and layers; pickRegistered() for one of the three real ones
└── portal/
    ├── FakeLevel.java                     # Map-backed CollisionGetter and LevelWriter for unit tests
    ├── PortalFootprintFaker.java          # Random PortalFootprint at a Y where a portal fits
    ├── PortalFrameBuilder.java            # Writes a frame of any block around a portal interior
    └── RegisteredPortalBlocks.java        # The registered Overworld MiningPortalBlock

src/test/java/com/ejafi/egmd/
├── dimension/
│   ├── MiningDimensionTest.java
│   └── PlayerRelocatorTest.java
└── portal/
    ├── MiningPortalBlockTest.java
    ├── MiningPortalShapeTest.java
    ├── PortalExitTest.java
    ├── PortalFootprintTest.java
    ├── PortalIgnitionHandlerTest.java
    └── PortalPositionFinderTest.java

src/gameTest/java/com/ejafi/egmd/
├── GameTestTemplates.java                 # Template name constants (EMPTY_7X7X7)
├── dimension/
│   ├── HostileMobGuardGameTests.java
│   └── PlayerRelocatorGameTests.java
└── portal/
    └── PortalGameTests.java
```

## How it works

### Portal variants

One `MiningPortalBlock` class serves all three portals. `PortalRegisters` registers one instance per `PortalVariant`, and each instance reads its frame block, target dimension, and tint from its variant. A POI type per variant lets the destination search find existing portals.

### Lighting a portal

`PortalIgnitionHandler` handles `PlayerInteractEvent.RightClickBlock` when the held item is in `egmd:portal_igniters` or has the `FIRESTARTER_LIGHT` item ability. The click must hit a frame block whose clicked face opens onto air or fire, or hit fire standing on a frame block. Infested frame blocks count as frame. The handler refuses spectators, a denied item use, a position the player may not build at (`mayUseItemAt`), and a frame whose portal would lead nowhere from the current dimension. It then looks for an empty frame on the X axis, then the Z axis.

The handler cancels the click on both sides so the client sees it consumed. Only the server fills the frame, consumes a Fire Charge or damages any other igniter, plays the igniter's sound, and emits a `BLOCK_PLACE` game event. The handler finds a frame's variant in a static map built from `PortalVariants.ALL`, since frame blocks are vanilla blocks, and reads the variant's portal block through `PortalRegisters` when the click happens.

### Travel

`MiningPortalBlock.entityInside` hands the entity to the vanilla `Portal` mechanism, so the stand-in delay follows the Nether portal game rules. `getPortalDestination` then asks the static `chooseRoute` decision for a `PortalRoute`, checking these rules in order.

| Rule | `PortalRoute` | Effect |
|---|---|---|
| `resolveDestination` maps the Overworld to the portal's mining dimension and that mining dimension back to the Overworld. A portal in any other dimension leads nowhere. | `Unlinked` | Nothing |
| `HostileMobGuard.admits` refuses a hostile mob, or anything carrying one, bound for a mining dimension. | `Refused` | Nothing |
| A destination in `PendingRegenerations` refuses everyone. | `ClosedForRegeneration` | The closed-for-regeneration message |
| Otherwise | `Travel` with the destination | The steps below |

1. The entity's X and Z are scaled by the two dimensions' coordinate scales and clamped to the destination's world border.
2. `computeArrivalTarget` builds the `ArrivalTarget`. A mining dimension gives `OnFlatSurface` at `MiningDimension.computeSurfaceY()`. The Overworld gives `OnTerrain` at the scaled position.
3. `ExitPortalFinder` runs the POI search for an existing portal of the same variant in a square of radius `max(16, portalSearchRadius / coordinateScale)`, taking the nearest and then the lowest.
4. When none exists, `ExitPortalFinder` has `PortalPositionFinder` pick a site and `PortalStructureBuilder` build the portal there. When no site exists, the traveler sees the no-safe-location message and stays put.
5. `PortalExitSelector` picks where the traveler appears.

### Placement search

`PortalPositionFinder` clamps the target inside the world border and loads the chunks the search covers. It then spirals out to radius 16 around the target, looking for a footprint where `PortalFootprint.canHost` holds. Hosting needs the portal plane and the exit cells to be open, with no fluid and no block entity, and the platform cells to be overwritable. `OnFlatSurface` searches at the fixed surface Y. `OnTerrain` moves each column to the `MOTION_BLOCKING` heightmap.

With no open site, the finder forces one at the target column, provided `PortalFootprint.canForce` holds. Forcing needs every cell to be overwritable, meaning no block entity, no portal, and no unbreakable block. On terrain, the forced Y is clamped between `max(minY + 1, 70)` and `minY + min(height, logicalHeight) - 10`. `PortalStructureBuilder` clears the exit cells of a forced site before building.

### Exit selection

`PortalExitSelector` tries the cell in front of the portal, then the cell behind it. A candidate is safe when the block below has a sturdy top face and the entity's bounding box has no collision there. When neither is safe, the entity arrives inside the portal at the position vanilla `PortalShape.findCollisionFreePosition` returns.

### Regeneration

`/egmd regenerate <dimension>` needs permission level 2. `RegenerationCommand` calls `DimensionRegenerator`, which refuses a dimension that is not a mining dimension. Otherwise it adds the dimension to `PendingRegenerations` and moves every player in it to the Overworld spawn through `PlayerRelocator.sendToOverworld`. That method takes the spawn column's `MOTION_BLOCKING_NO_LEAVES` height and the first clear space above it, and messages each player who arrives.

If any teleport is canceled, the result is `PlayersStranded` and the command reports a failure. Otherwise it is `Evacuated`, and the command prints the manual steps with the dimension's storage folder path. The dimension stays closed until the server stops, because `PendingRegenerations` clears on `ServerStoppedEvent`.

### Login rescue

`PlayerRelocator.onPlayerLoggedIn` asks the pure `chooseRescue` decision what to do with a player who logs in.

| Situation | `LoginRescue` | Effect |
|---|---|---|
| Not in a mining dimension, or standing clear | `Stay` | Nothing |
| In a dimension closed for regeneration | `LeaveClosedDimension` | Moved to the Overworld spawn with `message.egmd.login.closed_for_regeneration` |
| Inside blocks, with clear space higher in the same column | `LiftToClearSpace` | Moved up with `message.egmd.login.relocated` |
| Inside blocks, with no clear space above | `SendToOverworld` | Moved to the Overworld spawn with `message.egmd.login.relocated` |

### Hostile mobs, beds, and raids

The biomes spawn no mobs. `HostileMobGuard` also cancels, on the server, any `EntityJoinLevelEvent` in a mining dimension whose entity or passengers include an `Enemy`, so mobs from spawners, commands, and portals are all stopped. Every mining dimension type sets `bed_works` and `has_raids` to false.

### Registration

Each feature package owns its registers and exposes `register(IEventBus)`, which `EGMD` calls: `PortalRegisters` for portal blocks, POI types, and the particle type, and `PearlBlocks` for the pearl block and its item. Portal blocks have no items. Dimensions, biomes, and ore features are datapack registry entries produced by data generation.

### Data generation

Every file a data provider can produce comes from `./gradlew runData`, which writes to `src/generated/resources/`. That folder is committed together with its `.cache`, is added to the main resources by `build.gradle`, and the jar leaves out the `.cache`. The run passes `--existing src/main/resources`, so providers can check that hand-written textures exist.

### Surface Y

`MiningDimension` holds each dimension's min Y and flat layers. `MiningLevels` builds the dimension type's `min_y` and the level stem's flat layers from them, and `computeSurfaceY()` returns min Y plus the total layer height. Portal placement uses that value, so the generated surface and the portal height come from one definition. The surfaces are Y 68 in Overworld Mining, Y 129 in Nether Mining, and Y 65 in End Mining.

### Event buses

| Bus | Subscribers |
|---|---|
| Game bus, through `@EventBusSubscriber` | `PortalIgnitionHandler`, `HostileMobGuard`, `PendingRegenerations`, `PlayerRelocator`, `RegenerationCommand` |
| Mod bus, through `@EventBusSubscriber` | `DataGatherer` (`GatherDataEvent`) |
| Mod bus, through `modEventBus.addListener` | `PearlBlocks` (creative tab), `PortalClient` (block colors, particle providers) |

On this NeoForge build, `@EventBusSubscriber` registers each handler on the bus its event belongs to, so `DataGatherer` needs no `bus` argument.

## Testing

**Unit tests** live in `src/test` and run on JUnit 5 through ModDevGradle's `unitTest` support with `net.neoforged:testframework`. World reads and writes go through `FakeLevel`, which implements `CollisionGetter` (and through it `BlockGetter`) and `LevelWriter`.

**GameTests** live in `src/gameTest`. Each holder class carries `@GameTestHolder(EGMD.MOD_ID)` and `@PrefixGameTestTemplate(false)`, and each test names its template through `GameTestTemplates.EMPTY_7X7X7`, which resolves to `src/gameTest/resources/data/egmd/structure/empty_7x7x7.nbt`. The GameTest server loads only the vanilla dimensions, so behavior that needs a mining dimension loaded is tested through its pure decision method.

**Fixtures** live in the `testFixtures` source set from Gradle's `java-test-fixtures` plugin, shared by the unit tests and the GameTests. `build.gradle` binds `main`, `testFixtures`, and `gameTest` to the `egmd` mod, so the dev runs and the unit-test runtime load all three as one mod, while the jar contains only `main`. Datafaker is also on `additionalRuntimeClasspath`, because the GameTests and fixtures that the dev runs load use it.

## Coding standards

Code follows the frie-skills skills: `brandon-standards` with its `references/java.md`, `neoforge-modding`, and `engineering-patterns`. Only the repo-specific facts below are recorded here.

- The mod has no `api` package.
- Each translation key is a constant in its feature (`DimensionMessages`, `PortalMessages`, `PortalConfig`), and its English text is in `EgmdLanguageProvider`.
- The igniter tag key is `PortalIgnitionHandler.PORTAL_IGNITERS`, and its contents are in `EgmdItemTagsProvider`: Flint and Steel, the optional `c:tools/igniter` tag, and the optional `create_unbreakable_tools_series:unbreakable_netherite_flint_and_steel`.

## Resource locations

| Files | Source |
|---|---|
| `assets/egmd/blockstates/*.json`, `assets/egmd/models/block/*.json`, `assets/egmd/models/item/pearl_block.json` | `EgmdBlockStateProvider` |
| `assets/egmd/lang/en_us.json` | `EgmdLanguageProvider` |
| `assets/egmd/particles/portal_spark.json` | `EgmdParticleDescriptionProvider` |
| `data/egmd/loot_table/blocks/pearl_block.json` | `LootTableProvider` with `EgmdBlockLootProvider` |
| `data/minecraft/tags/block/mineable/pickaxe.json` | `EgmdBlockTagsProvider` |
| `data/egmd/tags/item/portal_igniters.json` | `EgmdItemTagsProvider` |
| `data/egmd/dimension_type/*.json`, `data/egmd/dimension/*.json` | `MiningLevels` |
| `data/egmd/worldgen/biome/*.json` | `MiningBiomes` |
| `data/egmd/worldgen/configured_feature/*.json`, `data/egmd/worldgen/placed_feature/*.json` | `MiningOres` |

Every path in that table is under `src/generated/resources/`.

No provider covers the files below, so they are hand-written.

| File | Path |
|---|---|
| Portal texture and its animation | `src/main/resources/assets/egmd/textures/block/mining_portal.png` and `.png.mcmeta` |
| Mod logo | `src/main/resources/logo.png` |
| Mod metadata template, expanded by `generateModMetadata` | `src/main/templates/META-INF/neoforge.mods.toml` |
| GameTest template | `src/gameTest/resources/data/egmd/structure/empty_7x7x7.nbt` |
