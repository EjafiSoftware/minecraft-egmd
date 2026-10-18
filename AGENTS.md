# AGENTS.md

This file guides coding agents working in this repository.

## Project

**Ejafi Games Mining Dimensions (EGMD)** is a NeoForge mod for Minecraft 1.21.1 with the mod ID `egmd`. It adds three flat, ore-rich mining dimensions and the Haunted dimension, each reached through its own portal from the Overworld. The Minecraft and NeoForge versions are in `gradle.properties`, and the NeoForge API reference for this version is [docs.neoforged.net/docs/1.21.1](https://docs.neoforged.net/docs/1.21.1/).

| Dimension | Frame block | Portal block | Biome |
|---|---|---|---|
| `egmd:overworld_mining` | Cobblestone | `egmd:mining_portal` | `egmd:mining_plains` |
| `egmd:nether_mining` | Netherrack | `egmd:nether_mining_portal` | `egmd:nether_mining_wastes` |
| `egmd:end_mining` | End Stone | `egmd:end_mining_portal` | `egmd:end_mining_wastes` |
| `egmd:haunted` | Bone Block | `egmd:haunted_portal` | `egmd:haunted_woods` |

## Commands

| Command | What it does |
|---|---|
| `./gradlew build` | Spotless check with palantir-java-format, compile under Error Prone with NullAway at error severity, PMD and CPD over every source set, unit tests, and the jar in `build/libs/` |
| `./gradlew runGameTestServer` | Runs every GameTest in the `egmd` namespace and exits |
| `./gradlew runData` | Regenerates `src/generated/resources/` |
| `./gradlew spotlessApply` | Formats every Java source set |
| `./gradlew runClient`, `./gradlew runServer` | Dev runs. Both load the `gameTest` source set and enable the `egmd` GameTest namespace, so `/test` works in them. |

**The gate** is `./gradlew runData` with `git status --porcelain` printing nothing, then `./gradlew build`, then `./gradlew runGameTestServer`, and all of them must pass before work is done. CI (`.github/workflows/build.yml`) runs the same steps through the shared `mod-gate.yml` workflow, which also reviews dependency changes on pull requests. CI also runs CodeQL through `mod-codeql.yml` on pushes to `main`, and skips changes that touch only markdown. Dependabot (`.github/dependabot.yml`) opens monthly grouped updates for Gradle and GitHub Actions.

### Build configuration

`build.gradle` applies only the `com.ejafi.neoforge-mod` convention plugin from [EjafiSoftware/minecraft-build](https://github.com/EjafiSoftware/minecraft-build), which owns the source sets, runs, tool and library versions, and the `neoforge.mods.toml` expansion. Its README describes what the plugin configures and the GitHub Packages token Gradle needs to download it. A build change that every mod should get belongs in that repo, and this repo picks it up by moving the plugin version in `build.gradle` and the workflow refs in `build.yml` to the new tag.

The dev runs need JDK 21. `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 21, and Gradle downloads one from the URLs in that file when none is installed. Each run uses `runs/<run name>` as its game directory.

### Releases

Pushing a tag that equals `mod_version`, such as `2.2.0`, on a commit in `main` runs the gate, then publishes a GitHub release titled with the version. The release carries `egmd-<version>.jar` and the `## [<version>]` section of `CHANGELOG.md`. A second job then uploads the same jar and notes to the CurseForge project the `CURSE_PROJECT_ID` repository variable names, using the `CURSE_API_TOKEN` organization secret. The release job fails without publishing when the tag doesn't match `mod_version`, the commit isn't in `main`, or the changelog has no section for the version.

## Source layout

### `src/main/java`

```
com.ejafi.egmd/
├── Egmd.java                              # @Mod entry point: calls each feature's register(IEventBus) and registers PortalConfig.SPEC as the COMMON config
├── portal/
│   ├── PortalRegisters.java               # Block, POI type, and portal_spark particle registers; getPortalBlock(variant)
│   ├── PortalConfig.java                  # ModConfigSpec with portalSearchRadius (16 to 512, default 128); its description feeds the config comment and the screen tooltip
│   ├── PortalMessages.java                # Translation key for the no-safe-location message
│   ├── PortalVariant.java                 # Record of one portal: registry ID, EgmdDimension, frame block, default corner block, map color, tint color; derives the corner tag and corner hint key from the ID
│   ├── PortalVariants.java                # The OVERWORLD_MINING, NETHER_MINING, END_MINING, and HAUNTED variants
│   ├── DimensionPortalBlock.java          # The portal block: chooseRoute() and travel, fill() for a frame interior, pane shape, ambient sound and tinted sparks, collapse, fluid refusal
│   ├── PortalFrame.java                   # Record of a frame: its interior rectangle, measured east along X or south along Z from the lowest corner, and its axis
│   ├── PortalFrameReader.java             # Finds a frame around a position through BlockGetter, validates its interior size, and reads its corners and portal blocks
│   ├── PortalCorner.java                  # The four frame corners (LOW_BOTTOM, HIGH_BOTTOM, LOW_TOP, HIGH_TOP) and locateAll() for a PortalFrame
│   ├── PortalCornerMaterials.java         # Judges corners against a Predicate<BlockState> and copies them, with a fallback block; create(variant) binds the corner tag
│   ├── PortalIgnitionHandler.java         # chooseIgnition() decision, and the RightClickBlock subscriber that lights an empty frame with valid corners; owns the egmd:portal_igniters tag key
│   ├── ArrivalTarget.java                 # Where an exit portal goes: OnFlatSurface for flat terrain, or OnTerrain elsewhere
│   ├── ExitPortalFinder.java              # Finds an existing exit portal through the POI search, or builds one at a site PortalPositionFinder picks
│   ├── PortalPositionFinder.java          # Chooses a PortalSite for an ArrivalTarget: spiral search for an open site, else a forced site; judges canHost and canForce
│   ├── PortalSite.java                    # An Open site, or a Forced site whose exit cells must be cleared
│   ├── PortalFootprint.java               # The plane, frame, corner, exit, and platform cells of a generated portal, and its PortalFrame
│   ├── PortalStructureBuilder.java        # Writes a portal, its frame with the given corner blocks, and its platform to a LevelWriter; clears the exit of a forced site
│   ├── PortalExit.java                    # Arrival position and yaw in front of, behind, or inside a PortalFrame
│   ├── PortalExitSelector.java            # Picks the first safe arrival for an entity: front, then back, else inside
│   └── client/
│       ├── PortalClient.java              # @Mod(dist = CLIENT): config screen, portal block tints, portal_spark particle provider
│       └── PortalSparkParticle.java       # Translucent portal particle tinted from a ColorParticleOption
├── dimension/
│   ├── EgmdDimension.java                 # Record of one dimension the mod adds: level key, Terrain, and Restriction set
│   ├── EgmdDimensions.java                # The four dimensions; find(levelKey) and getRestrictions(levelKey)
│   ├── EgmdBiomes.java                    # The four biome keys, shared by game code, datagen, and GameTests
│   ├── Terrain.java                       # Flat (min Y, flat layers, computeSurfaceY()) or Generated (min Y, noise settings key)
│   ├── Restriction.java                   # NO_HOSTILE_MOBS and NO_BEDS
│   ├── DimensionMessages.java             # Translation keys for the login relocation and refused bed messages
│   ├── HostileMobGuard.java               # admits() decision, and the EntityJoinLevelEvent subscriber that cancels hostile mobs other than the Wither where NO_HOSTILE_MOBS applies
│   ├── BedGuard.java                      # allowsUse() and bypassesBlockUse() decisions, and the RightClickBlock subscriber that keeps beds from being used where NO_BEDS applies
│   ├── SlimeSpawnChecker.java             # SpawnPlacementCheck subscriber that allows surface slimes at full moon odds in egmd:allows_full_moon_slime_spawns biomes; owns that tag key
│   ├── PlayerRelocator.java               # PlayerLoggedInEvent subscriber that chooses a LoginRescue and moves a player who logs in inside blocks in flat terrain
│   └── ClearSpaceFinder.java              # The first position at or above a start where an entity fits without collision
└── pearl/
    └── PearlBlocks.java                   # pearl_block block and item registers; adds the item to the Natural Blocks tab
```

### `src/datagen/java`

```
com.ejafi.egmd.datagen/
├── DataGatherer.java                      # GatherDataEvent subscriber that registers every provider and the datapack registry set
├── model/
│   └── EgmdBlockStateProvider.java        # Portal and pearl_block block states, block models, and the pearl_block item model
├── particle/
│   └── EgmdParticleDescriptionProvider.java # portal_spark sprites (vanilla generic_0 to generic_7)
├── lang/
│   └── EgmdLanguageProvider.java          # en_us translations
├── tag/
│   ├── EgmdBlockTagsProvider.java         # minecraft:mineable/pickaxe and the egmd:*_corners tags
│   ├── EgmdItemTagsProvider.java          # egmd:portal_igniters
│   └── EgmdBiomeTagsProvider.java         # egmd:allows_full_moon_slime_spawns
├── loot/
│   └── EgmdBlockLootProvider.java         # pearl_block loot table: itself with Silk Touch, else 1 to 4 Ender Pearls with a Fortune uniform bonus and explosion decay
├── worldgen/
│   ├── MiningOre.java                     # One ore: name, OreConfiguration, vein count per chunk, height range
│   ├── MiningOres.java                    # Every mining ore, listed once per dimension; bootstraps their configured and placed features
│   ├── MiningBiomes.java                  # The three mining biome definitions; each places its dimension's mod ores, then vanilla ones
│   ├── HauntedFeatures.java               # Pumpkin, carved pumpkin, Jack o'Lantern, and cobweb patches, and soul sand disks
│   └── HauntedBiomes.java                 # Haunted Woods: its spawns, effects, and generation
└── dimension/
    └── EgmdLevels.java                    # One dimension type and level stem per EgmdDimension; the generator comes from its Terrain, and the Overworld-like types share one factory
```

Datagen packages are named for the data kind each provider produces, and `DataGatherer` at the root is the only class that reaches across them.

### Test source sets

```
src/testFixtures/java/com/ejafi/egmd/
├── BlockPosFaker.java                     # Random BlockPos within the build height and a horizontal limit
├── dimension/
│   ├── EgmdDimensionFaker.java            # EgmdDimension from a generated level key, flat terrain, and restrictions; pickRegistered() with an optional filter
│   ├── FlatTerrainFaker.java              # Terrain.Flat from a generated min Y and layers
│   └── RestrictionsFaker.java             # Random Restriction sets, optionally including or excluding one restriction
└── portal/
    ├── FakeLevel.java                     # Map-backed CollisionGetter and LevelWriter for unit tests
    ├── PortalCornersFaker.java            # Four distinct non-frame corner states; fillCorners() for one state at every corner
    ├── PortalFootprintFaker.java          # Random PortalFootprint at a Y where a portal fits
    └── PortalFrameBuilder.java            # Writes a frame of any block around a portal interior, with corners that default to the frame block

src/test/java/com/ejafi/egmd/
├── dimension/
│   ├── BedGuardTest.java
│   ├── PlayerRelocatorTest.java
│   ├── SlimeSpawnCheckerTest.java
│   └── TerrainTest.java
└── portal/
    ├── DimensionPortalBlockTest.java
    ├── PortalCornerMaterialsTest.java
    ├── PortalExitTest.java
    ├── PortalFootprintTest.java
    ├── PortalFrameReaderTest.java
    ├── PortalIgnitionHandlerTest.java
    ├── PortalPositionFinderTest.java
    └── PortalStructureBuilderTest.java

src/gameTest/java/com/ejafi/egmd/
├── GameTestTemplates.java                 # Template name constant EMPTY_7X7X7 and its center position, EMPTY_7X7X7_CENTER
├── dimension/
│   ├── BedGuardGameTests.java
│   ├── ClearSpaceFinderGameTests.java
│   ├── EgmdBiomesGameTests.java
│   └── HostileMobGuardGameTests.java
├── pearl/
│   └── PearlBlockGameTests.java
└── portal/
    ├── PortalGameTestSupport.java         # Shared portal origin, portal states, frame at origin, and interior assertion
    ├── DimensionPortalBlockGameTests.java
    ├── ExitPortalFinderGameTests.java
    ├── PortalCornerMaterialsGameTests.java
    ├── PortalExitSelectorGameTests.java
    ├── PortalIgnitionHandlerGameTests.java
    ├── PortalPositionFinderGameTests.java
    └── PortalStructureBuilderGameTests.java
```

## How it works

### Dimensions

Each dimension the mod adds is one `EgmdDimension` in `EgmdDimensions`. It holds the level key, the `Terrain`, and the set of `Restriction`s that apply there. Every rule reads the property it needs from that definition, so no rule assumes anything about a dimension beyond what its definition says.

| Dimension | Terrain | Restrictions |
|---|---|---|
| Overworld Mining, Nether Mining, End Mining | `Flat` | `NO_HOSTILE_MOBS`, `NO_BEDS` |
| Haunted | `Generated` with `minecraft:overworld` noise settings | `NO_BEDS` |

A new dimension needs these additions and no change to any rule, the portal block, ignition, registration, or the client.

1. An `EgmdDimension` constant in `EgmdDimensions`, appended to its `ALL` list.
2. A `PortalVariant` constant in `PortalVariants`, appended to its `ALL` list.
3. Its biome key in `EgmdBiomes`, and its biome and any features in a class in `datagen.worldgen`, called from the matching registry in `DataGatherer`.
4. A `LevelDefinition` in `EgmdLevels`, with a dimension type factory.
5. Its corner tag contents in `EgmdBlockTagsProvider`, and its portal name, corner hint, and biome name in `EgmdLanguageProvider`.

A new kind of terrain is the one change that reaches existing code. It adds a `Terrain` case, and the compiler then flags the switches in `DimensionPortalBlock.computeArrivalTarget` and `EgmdLevels.createGenerator`.

### Portal variants

One `DimensionPortalBlock` class serves every portal. `PortalRegisters` registers one instance per `PortalVariant`, and each instance reads its frame block, target dimension, and tint from its variant. A POI type per variant lets the destination search find existing portals.

Each variant also names its corner tag, `egmd:<id>_corners`, its corner hint key, `message.egmd.portal.<id>_corners`, and a default corner block that is in the tag.

| Variant | Frame | Corner tag contents | Default corner |
|---|---|---|---|
| `mining_portal` | Cobblestone | Dirt, Coarse Dirt, Rooted Dirt, Grass Block, Podzol, Mycelium, `#c:gravels`, `#c:sands` | Dirt |
| `nether_mining_portal` | Netherrack | `#c:cobblestones`, `#c:stones` | Cobblestone |
| `end_mining_portal` | End Stone | `#c:netherracks`, `#c:obsidians`, Prismarine, Prismarine Bricks, Dark Prismarine | Netherrack |
| `haunted_portal` | Bone Block | Carved Pumpkin, Jack o'Lantern | Jack o'Lantern |

### Lighting a portal

`PortalIgnitionHandler` handles `PlayerInteractEvent.RightClickBlock` when the held item is in `egmd:portal_igniters` or has the `FIRESTARTER_LIGHT` item ability. The click must hit a frame block whose clicked face opens onto air or fire, or hit fire standing on a frame block. Infested frame blocks count as frame.

The handler asks the `chooseIgnition` decision for an `Ignition`. The decision refuses spectators, a denied item use, a position the player may not build at (`mayUseItemAt`), and a frame whose portal would lead nowhere from the current dimension. It then collects the empty frames on the X axis and the Z axis, and picks the first whose four corners are all in the variant's corner tag.

| Situation | `Ignition` | Effect |
|---|---|---|
| Not an igniter, no frame at the click, a refused click, or no empty frame | `Ignored` | Nothing |
| Empty frames, none with valid corners | `MissingCorners` | The corner hint described below |
| An empty frame with valid corners | `Lights` | The frame fills with portal blocks |

The corners are the cells diagonal to the interior rectangle, which `PortalFrameReader.readCorners` reads and `PortalCornerMaterials.acceptsAll` judges. An infested corner counts as its host block. The corners matter only for lighting, since `PortalFrameReader.find`, `PortalFrameReader.isComplete`, and `DimensionPortalBlock.updateShape` never read them, so a lit portal stays lit without them.

When an empty frame exists but none has valid corners, the handler leaves the click alone, so vanilla Flint and Steel places fire as usual. On the server, and only in the Overworld, the player also gets the variant's corner hint on the action bar. In Nether Mining and End Mining every tunnel through Netherrack or End Stone is a frame without corners, so the hint would fire constantly there.

When it lights a frame, the handler cancels the click on both sides so the client sees it consumed. Tags sync to the client, so both sides reach the same decision. Only the server fills the frame, consumes a Fire Charge or damages any other igniter, plays the igniter's sound, and emits a `BLOCK_PLACE` game event. The handler finds a frame's variant in a static map built from `PortalVariants.ALL`, since frame blocks are vanilla blocks, and reads the variant's portal block through `PortalRegisters` when the click happens.

### Travel

`DimensionPortalBlock.entityInside` hands the entity to the vanilla `Portal` mechanism, so the stand-in delay follows the Nether portal game rules. `getPortalDestination` then asks the static `chooseRoute` decision for a `PortalRoute`, checking these rules in order.

| Rule | `PortalRoute` | Effect |
|---|---|---|
| `resolveDestination` maps the Overworld to the portal's dimension and that dimension back to the Overworld. A portal in any other dimension leads nowhere. | `Unlinked` | Nothing |
| `HostileMobGuard.admits` refuses a hostile mob other than the Wither, or anything carrying one, bound for a dimension with `NO_HOSTILE_MOBS`. | `Refused` | Nothing |
| Otherwise | `Travel` with the destination | The steps below |

1. The entity's X and Z are scaled by the two dimensions' coordinate scales and clamped to the destination's world border.
2. `computeArrivalTarget` builds the `ArrivalTarget` from the destination's terrain. `Flat` gives `OnFlatSurface` at `computeSurfaceY()`. `Generated` terrain and the Overworld give `OnTerrain` at the scaled position.
3. `ExitPortalFinder` runs the POI search for an existing portal of the same variant in a square of radius `max(16, portalSearchRadius / coordinateScale)`. It keeps only POI positions that hold the variant's own portal block, so a stale POI is skipped, and takes the nearest and then the lowest.
4. When none exists, `ExitPortalFinder` has `PortalPositionFinder` pick a site and `PortalStructureBuilder` build the portal there with the corners described below. When no site exists, the traveler sees the no-safe-location message and stays put.
5. `PortalExitSelector` picks where the traveler appears.

### Exit portal corners

`getPortalDestination` reads the source portal's corners through `PortalFrameReader` at the source portal block, and `PortalCornerMaterials.copyAll` maps them onto the exit portal by `PortalCorner`. Bottom corners stay at the bottom and top corners at the top. The corner at the lower coordinate along the source portal's axis goes to the corner at the lower coordinate along the exit's axis, which is the same axis.

Each copied state is replaced by the default state of its infested host, and a block outside the corner tag is replaced by the variant's default corner. That keeps a player from swapping valuable blocks into the corners of a lit portal and having them duplicated on the other side. A `FallingBlock`, such as Sand or Gravel, is replaced by the default corner at the bottom corners only, since nothing checks what lies under the platform and it would fall. When the source frame cannot be read, all four corners use the default.

`PortalFootprint.locateCorners()` places the bottom corners in the middle row of the platform and the top corners in the frame plane, and `PortalStructureBuilder` writes each frame and platform cell once, choosing the corner state for a corner cell and the frame block everywhere else.

### Placement search

`PortalPositionFinder` clamps the target inside the world border and loads the chunks the search covers. It then spirals out to radius 16 around the target, looking for a footprint where `canHost` holds. Hosting needs the portal plane and the exit cells to be open, with no fluid and no block entity, and the platform cells to be overwritable. `OnFlatSurface` searches at the fixed surface Y. `OnTerrain` moves each column to the `MOTION_BLOCKING_NO_LEAVES` heightmap, so a portal is built under a tree canopy rather than on top of it.

With no open site, the finder forces one at the target column, provided `canForce` holds. Forcing needs every cell to be overwritable, meaning no block entity, no portal, and no unbreakable block. On terrain, the forced Y is clamped between `max(minY + 1, 70)` and `minY + min(height, logicalHeight) - 10`. `PortalStructureBuilder` clears the exit cells of a forced site before building.

### Exit selection

`PortalExitSelector` tries the cell in front of the portal, then the cell behind it. A candidate is safe when the block below has a sturdy top face, and the entity's bounding box there has no collision and touches no fire or lava. When neither is safe, the entity arrives inside the portal at the position vanilla `PortalShape.findCollisionFreePosition` returns.

### Login rescue

`PlayerRelocator.onPlayerLoggedIn` asks the pure `chooseRescue` decision what to do with a player who logs in.

| Situation | `LoginRescue` | Effect |
|---|---|---|
| Not in an EGMD dimension with `Flat` terrain, or standing clear | `Stay` | Nothing |
| Inside blocks, with clear space higher in the same column | `LiftToClearSpace` | Moved up with `message.egmd.login.relocated` |
| Inside blocks, with no clear space above | `SendToOverworld` | Moved to the Overworld spawn with `message.egmd.login.relocated` |

The rescue covers only flat terrain, whose layers can change between versions. In generated terrain a player who logged out crawling in a one-block gap would read as inside blocks, because the pose is restored only on the first tick, so the rescue leaves them alone.

A move to the Overworld spawn takes the spawn column's `MOTION_BLOCKING_NO_LEAVES` height and the first clear space above it. The player gets the message only when the teleport actually lands them in the Overworld.

### Hostile mobs, beds, and raids

`HostileMobGuard` and `BedGuard` take the current dimension's restrictions from `EgmdDimensions.getRestrictions`, which is empty for a vanilla dimension.

The mining biomes spawn no mobs. Where `NO_HOSTILE_MOBS` applies, `HostileMobGuard` also cancels, on the server, any `EntityJoinLevelEvent` whose entity or passengers include an `Enemy` other than a `WitherBoss`, so mobs from spawners, commands, and portals are all stopped.

The Wither is exempt so that one a player summons inside a mining dimension is not canceled after its skulls and Soul Sand are used up. A Wither never arrives through a portal, because vanilla `WitherBoss.canUsePortal` returns false and it cannot ride anything. A Wither carrying any other hostile mob is still refused.

Every EGMD dimension type sets `has_raids` to false. `EgmdLevels` sets `bed_works` to false wherever `NO_BEDS` applies, so the dimension type and `BedGuard` read the same definition. With `bed_works` false, vanilla `BedBlock.useWithoutItem` blows the bed up, and that method is the only path to the explosion.

`BedGuard` handles the main-hand `PlayerInteractEvent.RightClickBlock` on both sides. When the pure `allowsUse` decision refuses a `BedBlock` and vanilla would use the bed, it cancels the click with `InteractionResult.SUCCESS`, and the server shows `message.egmd.bed.refused` on the action bar.

Vanilla uses the bed only from the main hand, and skips it when the player sneaks holding an item in either hand, unless every held item returns true from `doesSneakBypassUse`. `BedGuard.bypassesBlockUse` mirrors that check from `ServerPlayerGameMode.useItemOn`, and the handler leaves those clicks alone, so a sneaking player can still place a block against a bed. Respawn Anchors are not affected.

### The Haunted dimension

The Haunted dimension type fixes the time at 18000, which is midnight. Vanilla `DimensionType.moonPhase` ignores the fixed time and counts the Overworld's days, so the moon still cycles through its phases. Sky light and the Overworld sky effects stay on, so the moon shows, and the monster spawn light settings match the Overworld, so torches stop spawns.

Haunted Woods spawns Spiders and Slimes at weight 100 each, Zombies and Skeletons at 40, and Bats as ambient mobs. Vanilla `Slime.checkSlimeSpawnRules` allows a surface slime only in a biome tagged `minecraft:allows_surface_slime_spawns`, between Y 51 and 69, with odds scaled by moon brightness.

Haunted Woods is in `egmd:allows_full_moon_slime_spawns` instead. When the vanilla check fails for a natural slime in a biome with that tag, `SlimeSpawnChecker` repeats the surface rule with the moon brightness of a full moon and allows the spawn when it passes. Slime chunks below Y 40 still spawn slimes through the vanilla check.

The biome generates the vanilla Overworld carvers, lakes, geodes, dungeons, springs, ores, and soft disks, the Dark Forest vegetation, fossils, and the patches and disks from `HauntedFeatures`. The level stem uses a `FixedBiomeSource`, and the biome is in no structure tag, so no structures generate.

### Registration

Each feature package owns its registers and exposes `register(IEventBus)`, which `Egmd` calls. `PortalRegisters` registers the portal blocks, POI types, and the particle type, and `PearlBlocks` registers the pearl block and its item. Portal blocks have no items. Dimensions, biomes, and features are datapack registry entries produced by data generation.

### Data generation

Every file a data provider can produce comes from `./gradlew runData`, which writes to `src/generated/resources/`. That folder is committed and is added to the main resources by the convention plugin. Its `.cache` is gitignored and left out of the jar. The run passes `--existing src/main/resources`, so providers can check that hand-written textures exist.

The providers live in the `datagen` source set, which sees `main` and which only the data run loads, so the jar never contains them. A key that game code also reads, such as a biome key, is a constant in its feature package in `main`.

### Terrain and surface Y

`Terrain.minY()` sets each dimension type's `min_y`. A `Flat` terrain holds its flat layers, `EgmdLevels` builds the flat level stem from them, and `computeSurfaceY()` returns min Y plus the total layer height. Portal placement uses that value, so the generated surface and the portal height come from one definition.

The surfaces are Y 68 in Overworld Mining, Y 129 in Nether Mining, and Y 65 in End Mining. A `Generated` terrain's min Y must match its noise settings, which is Y -64 for `minecraft:overworld`.

### Event buses

| Bus | Subscribers |
|---|---|
| Game bus, through `@EventBusSubscriber` | `PortalIgnitionHandler`, `HostileMobGuard`, `BedGuard`, `SlimeSpawnChecker`, `PlayerRelocator` |
| Mod bus, through `@EventBusSubscriber` | `DataGatherer` (`GatherDataEvent`) |
| Mod bus, through `modEventBus.addListener` | `PearlBlocks` (creative tab), `PortalClient` (block colors, particle providers) |

On this NeoForge build, `@EventBusSubscriber` registers each handler on the bus its event belongs to, so `DataGatherer` needs no `bus` argument.

## Testing

**Unit tests** live in `src/test` and run on JUnit 5 through ModDevGradle's `unitTest` support with `net.neoforged:testframework`. World reads and writes go through `FakeLevel`, which implements `CollisionGetter` (and through it `BlockGetter`) and `LevelWriter`. The unit-test runtime binds no data tags, so every tag is empty there.

A unit test therefore never checks tag membership, and logic that depends on a tag takes a `Predicate<BlockState>` that the test supplies, as `PortalCornerMaterials` does. Tag contents are tested in GameTests, where the server loads data tags.

**GameTests** live in `src/gameTest`. Each holder class carries `@GameTestHolder(Egmd.MOD_ID)` and `@PrefixGameTestTemplate(false)`, and each test names its template through `GameTestTemplates.EMPTY_7X7X7`, which resolves to `src/gameTest/resources/data/egmd/structure/empty_7x7x7.nbt`. The GameTest server loads only the vanilla dimensions, so behavior that needs an EGMD dimension loaded is tested through its pure decision method. The rule decisions take a `Set<Restriction>`, so their tests use `RestrictionsFaker` rather than a registered dimension.

**Fixtures** live in the `testFixtures` source set from Gradle's `java-test-fixtures` plugin, shared by the unit tests and the GameTests. The convention plugin binds `main`, `testFixtures`, `gameTest`, and `datagen` to the `egmd` mod, so the dev runs and the unit-test runtime load them as one mod, while the jar contains only `main`. Datafaker is also on `additionalRuntimeClasspath`, because the GameTests and fixtures that the dev runs load use it.

## Coding standards

Code follows the frie-skills skills `brandon-standards` with its `references/java.md`, `neoforge-modding`, and `engineering-patterns`. Only the repo-specific facts below are recorded here.

- The mod has no `api` package.
- Each fixed translation key is a constant in its feature (`DimensionMessages`, `PortalMessages`, `PortalConfig`), and its English text is in `EgmdLanguageProvider`. Corner hint keys come from `PortalVariant.getCornerHintKey()`.
- The igniter tag key is `PortalIgnitionHandler.PORTAL_IGNITERS`, and its contents in `EgmdItemTagsProvider` are Flint and Steel, the optional `c:tools/igniter` tag, and the optional `create_unbreakable_tools_series:unbreakable_netherite_flint_and_steel`.
- The full moon slime biome tag key is `SlimeSpawnChecker.ALLOWS_FULL_MOON_SLIME_SPAWNS`, and its contents are in `EgmdBiomeTagsProvider`.
- Each corner tag key comes from `PortalVariant.getCornerTag()`, and its contents are in `EgmdBlockTagsProvider`. The `c:` tags are required references, since data generation loads NeoForge alongside the mod.

## Resource locations

| Files | Source |
|---|---|
| `assets/egmd/blockstates/*.json`, `assets/egmd/models/block/*.json`, `assets/egmd/models/item/pearl_block.json` | `EgmdBlockStateProvider` |
| `assets/egmd/lang/en_us.json` | `EgmdLanguageProvider` |
| `assets/egmd/particles/portal_spark.json` | `EgmdParticleDescriptionProvider` |
| `data/egmd/loot_table/blocks/pearl_block.json` | `LootTableProvider` with `EgmdBlockLootProvider` |
| `data/minecraft/tags/block/mineable/pickaxe.json`, `data/egmd/tags/block/*_corners.json` | `EgmdBlockTagsProvider` |
| `data/egmd/tags/item/portal_igniters.json` | `EgmdItemTagsProvider` |
| `data/egmd/tags/worldgen/biome/allows_full_moon_slime_spawns.json` | `EgmdBiomeTagsProvider` |
| `data/egmd/dimension_type/*.json`, `data/egmd/dimension/*.json` | `EgmdLevels` |
| `data/egmd/worldgen/biome/*.json` | `MiningBiomes`, `HauntedBiomes` |
| `data/egmd/worldgen/configured_feature/*.json`, `data/egmd/worldgen/placed_feature/*.json` | `MiningOres`, `HauntedFeatures` |

Every path in that table is under `src/generated/resources/`.

No provider covers the files below, so they are hand-written.

| File | Path |
|---|---|
| Portal texture and its animation | `src/main/resources/assets/egmd/textures/block/mining_portal.png` and `.png.mcmeta` |
| Mod logo | `src/main/resources/logo.png` |
| Mod metadata template, expanded by `generateModMetadata` | `src/main/templates/META-INF/neoforge.mods.toml` |
| GameTest template | `src/gameTest/resources/data/egmd/structure/empty_7x7x7.nbt` |
