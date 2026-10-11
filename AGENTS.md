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
│   ├── PortalVariant.java                 # Record of one portal: registry ID, MiningDimension, frame block, default corner block, corner hint key, map color, tint color; getCornerTag() derives egmd:<id>_corners
│   ├── PortalVariants.java                # The OVERWORLD, NETHER, and END variants
│   ├── MiningPortalBlock.java             # The portal block: chooseRoute() and travel, pane shape, ambient sound and tinted sparks, collapse, fluid refusal
│   ├── MiningPortalShape.java             # Reads a frame and its corners through BlockGetter, validates its interior size, and fills it with portal blocks
│   ├── PortalCorner.java                  # The four frame corners (LOW_BOTTOM, HIGH_BOTTOM, LOW_TOP, HIGH_TOP) and locateAll() for an interior rectangle
│   ├── PortalCornerMaterials.java         # Judges corners against a Predicate<BlockState> and copies them, with a fallback block; create(variant) binds the corner tag
│   ├── PortalIgnitionHandler.java         # RightClickBlock subscriber that lights an empty frame with valid corners; owns the egmd:portal_igniters tag key
│   ├── ArrivalTarget.java                 # Where an exit portal goes: OnFlatSurface in a mining dimension, or OnTerrain elsewhere
│   ├── ExitPortalFinder.java              # Finds an existing exit portal through the POI search, or builds one at a site PortalPositionFinder picks
│   ├── PortalPositionFinder.java          # Chooses a PortalSite for an ArrivalTarget: spiral search for an open site, else a forced site
│   ├── PortalSite.java                    # An Open site, or a Forced site whose exit cells must be cleared
│   ├── PortalFootprint.java               # The frame, corner, portal, exit, and platform cells of a generated portal; judges canHost and canForce
│   ├── PortalStructureBuilder.java        # Writes a portal, its frame with the given corner blocks, and its platform to a LevelWriter; clears the exit of a forced site
│   ├── PortalExit.java                    # Arrival position and yaw in front of, behind, or inside a portal rectangle
│   ├── PortalExitSelector.java            # Picks the first safe arrival for an entity: front, then back, else inside
│   └── client/
│       ├── PortalClient.java              # @Mod(dist = CLIENT): config screen, portal block tints, portal_spark particle provider
│       └── PortalSparkParticle.java       # Translucent portal particle tinted from a ColorParticleOption
├── dimension/
│   ├── MiningDimension.java               # The three mining dimensions: level key, min Y, flat layers, computeSurfaceY(), findByLevelKey()
│   ├── DimensionMessages.java             # Translation keys for the login relocation and refused bed messages
│   ├── HostileMobGuard.java               # admits() decision, and the EntityJoinLevelEvent subscriber that cancels hostile mobs other than the Wither in a mining dimension
│   ├── BedGuard.java                      # allowsUse() decision, and the RightClickBlock subscriber that keeps beds in a mining dimension from being used
│   └── PlayerRelocator.java               # PlayerLoggedInEvent subscriber that chooses a LoginRescue and moves a player who logs in inside blocks
├── pearl/
│   └── PearlBlocks.java                   # pearl_block block and item registers; adds the item to the Natural Blocks tab
└── datagen/
    ├── DataGatherer.java                  # GatherDataEvent subscriber that registers every provider and the datapack registry set
    ├── EgmdBlockStateProvider.java        # Portal and pearl_block block states, block models, and the pearl_block item model
    ├── EgmdLanguageProvider.java          # en_us translations
    ├── EgmdBlockTagsProvider.java         # minecraft:mineable/pickaxe and the three egmd:*_corners tags
    ├── EgmdItemTagsProvider.java          # egmd:portal_igniters
    ├── EgmdBlockLootProvider.java         # pearl_block loot table: itself with Silk Touch, else 1 to 4 Ender Pearls with a Fortune uniform bonus and explosion decay
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
    ├── PortalCornersFaker.java            # Four distinct non-frame corner states; fillCorners() for one state at every corner
    ├── PortalFootprintFaker.java          # Random PortalFootprint at a Y where a portal fits
    ├── PortalFrameBuilder.java            # Writes a frame of any block around a portal interior, with corners that default to the frame block
    └── RegisteredPortalBlocks.java        # The registered Overworld MiningPortalBlock

src/test/java/com/ejafi/egmd/
├── dimension/
│   ├── BedGuardTest.java
│   ├── MiningDimensionTest.java
│   └── PlayerRelocatorTest.java
└── portal/
    ├── MiningPortalBlockTest.java
    ├── MiningPortalShapeTest.java
    ├── PortalCornerMaterialsTest.java
    ├── PortalExitTest.java
    ├── PortalFootprintTest.java
    ├── PortalIgnitionHandlerTest.java
    ├── PortalPositionFinderTest.java
    └── PortalStructureBuilderTest.java

src/gameTest/java/com/ejafi/egmd/
├── GameTestTemplates.java                 # Template name constants (EMPTY_7X7X7)
├── dimension/
│   ├── BedGuardGameTests.java
│   ├── HostileMobGuardGameTests.java
│   └── PlayerRelocatorGameTests.java
├── pearl/
│   └── PearlBlockGameTests.java
└── portal/
    └── PortalGameTests.java
```

## How it works

### Portal variants

One `MiningPortalBlock` class serves all three portals. `PortalRegisters` registers one instance per `PortalVariant`, and each instance reads its frame block, target dimension, and tint from its variant. A POI type per variant lets the destination search find existing portals.

Each variant also names its corner tag, `egmd:<id>_corners`, and a default corner block that is in that tag.

| Variant | Frame | Corner tag contents | Default corner |
|---|---|---|---|
| `mining_portal` | Cobblestone | Dirt, Coarse Dirt, Rooted Dirt, Grass Block, Podzol, Mycelium, `#c:gravels`, `#c:sands` | Dirt |
| `nether_mining_portal` | Netherrack | `#c:cobblestones`, `#c:stones` | Cobblestone |
| `end_mining_portal` | End Stone | `#c:netherracks`, `#c:obsidians`, Prismarine, Prismarine Bricks, Dark Prismarine | Netherrack |

### Lighting a portal

`PortalIgnitionHandler` handles `PlayerInteractEvent.RightClickBlock` when the held item is in `egmd:portal_igniters` or has the `FIRESTARTER_LIGHT` item ability. The click must hit a frame block whose clicked face opens onto air or fire, or hit fire standing on a frame block. Infested frame blocks count as frame. The handler refuses spectators, a denied item use, a position the player may not build at (`mayUseItemAt`), and a frame whose portal would lead nowhere from the current dimension. It then collects the empty frames on the X axis and the Z axis, and lights the first whose four corners are all in the variant's corner tag.

The corners are the cells diagonal to the interior rectangle, which `MiningPortalShape.readCorners()` reads and `PortalCornerMaterials.acceptsAll` judges. An infested corner counts as its host block. The corners matter only for lighting, since `isValid`, `isComplete`, and `MiningPortalBlock.updateShape` never read them, so a lit portal stays lit without them.

When an empty frame exists but none has valid corners, the handler leaves the click alone, so vanilla Flint and Steel places fire as usual. On the server, and only in the Overworld, the player also gets the variant's corner hint on the action bar. In Nether Mining and End Mining every tunnel through Netherrack or End Stone is a frame without corners, so the hint would fire constantly there.

When it lights a frame, the handler cancels the click on both sides so the client sees it consumed. Tags sync to the client, so both sides reach the same decision. Only the server fills the frame, consumes a Fire Charge or damages any other igniter, plays the igniter's sound, and emits a `BLOCK_PLACE` game event. The handler finds a frame's variant in a static map built from `PortalVariants.ALL`, since frame blocks are vanilla blocks, and reads the variant's portal block through `PortalRegisters` when the click happens.

### Travel

`MiningPortalBlock.entityInside` hands the entity to the vanilla `Portal` mechanism, so the stand-in delay follows the Nether portal game rules. `getPortalDestination` then asks the static `chooseRoute` decision for a `PortalRoute`, checking these rules in order.

| Rule | `PortalRoute` | Effect |
|---|---|---|
| `resolveDestination` maps the Overworld to the portal's mining dimension and that mining dimension back to the Overworld. A portal in any other dimension leads nowhere. | `Unlinked` | Nothing |
| `HostileMobGuard.admits` refuses a hostile mob other than the Wither, or anything carrying one, bound for a mining dimension. | `Refused` | Nothing |
| Otherwise | `Travel` with the destination | The steps below |

1. The entity's X and Z are scaled by the two dimensions' coordinate scales and clamped to the destination's world border.
2. `computeArrivalTarget` builds the `ArrivalTarget`. A mining dimension gives `OnFlatSurface` at `MiningDimension.computeSurfaceY()`. The Overworld gives `OnTerrain` at the scaled position.
3. `ExitPortalFinder` runs the POI search for an existing portal of the same variant in a square of radius `max(16, portalSearchRadius / coordinateScale)`. It keeps only POI positions that hold the variant's own portal block, so a stale POI is skipped, and takes the nearest and then the lowest.
4. When none exists, `ExitPortalFinder` has `PortalPositionFinder` pick a site and `PortalStructureBuilder` build the portal there with the corners described below. When no site exists, the traveler sees the no-safe-location message and stays put.
5. `PortalExitSelector` picks where the traveler appears.

### Exit portal corners

`getPortalDestination` reads the source portal's corners through `MiningPortalShape.readCorners()` at the source portal block, and `PortalCornerMaterials.copyAll` maps them onto the exit portal by `PortalCorner`. Bottom corners stay at the bottom and top corners at the top. The corner at the lower coordinate along the source portal's axis goes to the corner at the lower coordinate along the exit's axis, which is the same axis.

Each copied state is replaced by the default state of its infested host, and a block outside the corner tag is replaced by the variant's default corner. That keeps a player from swapping valuable blocks into the corners of a lit portal and having them duplicated on the other side. A `FallingBlock`, such as Sand or Gravel, is replaced by the default corner at the bottom corners only, since nothing checks what lies under the platform and it would fall. When the source frame cannot be read, all four corners use the default.

`PortalFootprint.locateCorners()` places the bottom corners in the middle row of the platform and the top corners in the frame plane, and `PortalStructureBuilder` writes each frame and platform cell once, choosing the corner state for a corner cell and the frame block everywhere else.

### Placement search

`PortalPositionFinder` clamps the target inside the world border and loads the chunks the search covers. It then spirals out to radius 16 around the target, looking for a footprint where `PortalFootprint.canHost` holds. Hosting needs the portal plane and the exit cells to be open, with no fluid and no block entity, and the platform cells to be overwritable. `OnFlatSurface` searches at the fixed surface Y. `OnTerrain` moves each column to the `MOTION_BLOCKING` heightmap.

With no open site, the finder forces one at the target column, provided `PortalFootprint.canForce` holds. Forcing needs every cell to be overwritable, meaning no block entity, no portal, and no unbreakable block. On terrain, the forced Y is clamped between `max(minY + 1, 70)` and `minY + min(height, logicalHeight) - 10`. `PortalStructureBuilder` clears the exit cells of a forced site before building.

### Exit selection

`PortalExitSelector` tries the cell in front of the portal, then the cell behind it. A candidate is safe when the block below has a sturdy top face and the entity's bounding box has no collision there. When neither is safe, the entity arrives inside the portal at the position vanilla `PortalShape.findCollisionFreePosition` returns.

### Login rescue

`PlayerRelocator.onPlayerLoggedIn` asks the pure `chooseRescue` decision what to do with a player who logs in.

| Situation | `LoginRescue` | Effect |
|---|---|---|
| Not in a mining dimension, or standing clear | `Stay` | Nothing |
| Inside blocks, with clear space higher in the same column | `LiftToClearSpace` | Moved up with `message.egmd.login.relocated` |
| Inside blocks, with no clear space above | `SendToOverworld` | Moved to the Overworld spawn with `message.egmd.login.relocated` |

A move to the Overworld spawn takes the spawn column's `MOTION_BLOCKING_NO_LEAVES` height and the first clear space above it. The player gets the message only when the teleport actually lands them in the Overworld.

### Hostile mobs, beds, and raids

The biomes spawn no mobs. `HostileMobGuard` also cancels, on the server, any `EntityJoinLevelEvent` in a mining dimension whose entity or passengers include an `Enemy` other than a `WitherBoss`, so mobs from spawners, commands, and portals are all stopped. The Wither is exempt so that one a player summons inside a mining dimension is not canceled after its skulls and Soul Sand are used up. A Wither never arrives through a portal, because vanilla `WitherBoss.canUsePortal` returns false and it cannot ride anything. A Wither carrying any other hostile mob is still refused.

Every mining dimension type sets `bed_works` and `has_raids` to false. With `bed_works` false, vanilla `BedBlock.useWithoutItem` blows the bed up, and that method is the only path to the explosion. `BedGuard` handles the main-hand `PlayerInteractEvent.RightClickBlock` on both sides. When the pure `allowsUse` decision refuses a `BedBlock` in a mining dimension and vanilla would use the bed, it cancels the click with `InteractionResult.SUCCESS`, and the server shows `message.egmd.bed.refused` on the action bar. Vanilla uses the bed only from the main hand, and skips it when the player sneaks holding an item in either hand, unless every held item returns true from `doesSneakBypassUse`. `BedGuard` mirrors that check from `ServerPlayerGameMode.useItemOn` and leaves those clicks alone, so a sneaking player can still place a block against a bed. Respawn Anchors are not affected.

### Registration

Each feature package owns its registers and exposes `register(IEventBus)`, which `EGMD` calls: `PortalRegisters` for portal blocks, POI types, and the particle type, and `PearlBlocks` for the pearl block and its item. Portal blocks have no items. Dimensions, biomes, and ore features are datapack registry entries produced by data generation.

### Data generation

Every file a data provider can produce comes from `./gradlew runData`, which writes to `src/generated/resources/`. That folder is committed together with its `.cache`, is added to the main resources by `build.gradle`, and the jar leaves out the `.cache`. The run passes `--existing src/main/resources`, so providers can check that hand-written textures exist.

### Surface Y

`MiningDimension` holds each dimension's min Y and flat layers. `MiningLevels` builds the dimension type's `min_y` and the level stem's flat layers from them, and `computeSurfaceY()` returns min Y plus the total layer height. Portal placement uses that value, so the generated surface and the portal height come from one definition. The surfaces are Y 68 in Overworld Mining, Y 129 in Nether Mining, and Y 65 in End Mining.

### Event buses

| Bus | Subscribers |
|---|---|
| Game bus, through `@EventBusSubscriber` | `PortalIgnitionHandler`, `HostileMobGuard`, `BedGuard`, `PlayerRelocator` |
| Mod bus, through `@EventBusSubscriber` | `DataGatherer` (`GatherDataEvent`) |
| Mod bus, through `modEventBus.addListener` | `PearlBlocks` (creative tab), `PortalClient` (block colors, particle providers) |

On this NeoForge build, `@EventBusSubscriber` registers each handler on the bus its event belongs to, so `DataGatherer` needs no `bus` argument.

## Testing

**Unit tests** live in `src/test` and run on JUnit 5 through ModDevGradle's `unitTest` support with `net.neoforged:testframework`. World reads and writes go through `FakeLevel`, which implements `CollisionGetter` (and through it `BlockGetter`) and `LevelWriter`. The unit-test runtime binds no data tags, so every tag is empty there. A unit test therefore never checks tag membership, and logic that depends on a tag takes a `Predicate<BlockState>` that the test supplies, as `PortalCornerMaterials` does. Tag contents are tested in GameTests, where the server loads data tags.

**GameTests** live in `src/gameTest`. Each holder class carries `@GameTestHolder(EGMD.MOD_ID)` and `@PrefixGameTestTemplate(false)`, and each test names its template through `GameTestTemplates.EMPTY_7X7X7`, which resolves to `src/gameTest/resources/data/egmd/structure/empty_7x7x7.nbt`. The GameTest server loads only the vanilla dimensions, so behavior that needs a mining dimension loaded is tested through its pure decision method.

**Fixtures** live in the `testFixtures` source set from Gradle's `java-test-fixtures` plugin, shared by the unit tests and the GameTests. `build.gradle` binds `main`, `testFixtures`, and `gameTest` to the `egmd` mod, so the dev runs and the unit-test runtime load all three as one mod, while the jar contains only `main`. Datafaker is also on `additionalRuntimeClasspath`, because the GameTests and fixtures that the dev runs load use it.

## Coding standards

Code follows the frie-skills skills: `brandon-standards` with its `references/java.md`, `neoforge-modding`, and `engineering-patterns`. Only the repo-specific facts below are recorded here.

- The mod has no `api` package.
- Each translation key is a constant in its feature (`DimensionMessages`, `PortalMessages`, `PortalConfig`), and its English text is in `EgmdLanguageProvider`.
- The igniter tag key is `PortalIgnitionHandler.PORTAL_IGNITERS`, and its contents are in `EgmdItemTagsProvider`: Flint and Steel, the optional `c:tools/igniter` tag, and the optional `create_unbreakable_tools_series:unbreakable_netherite_flint_and_steel`.
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
