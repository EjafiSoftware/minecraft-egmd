# AGENTS.md

Guidance for coding agents working in this repository.

## Project Overview

**Ejafi Games Mining Dimensions (EGMD)** is a NeoForge mod for Minecraft 1.21.1 (mod ID: `egmd`). It adds three flat, ore-rich mining dimensions accessible via custom portals. Dimensions: Overworld Mining (Cobblestone frame), Nether Mining (Netherrack frame), End Mining (End Stone frame).

## Build & Run Commands

```bash
# Build the mod JAR
./gradlew build

# Run the Minecraft client with the mod loaded
./gradlew runClient

# Run a dedicated server
./gradlew runServer

# Run data generators (regenerates src/generated/resources/)
./gradlew runData

# Run game tests
./gradlew runGameTestServer
```

The built JAR is output to `build/libs/`. Java 21 is required (set via toolchain in `build.gradle`).

## Architecture

### Package Structure

```
com.ejafi.egmd/
├── EGMD.java                          # @Mod entry point; wires registries and config
├── client/
│   ├── EGMDClient.java                # Client-only setup (@Mod dist=CLIENT); registers portal block color tints
│   └── PortalSparkParticle.java       # Tinted portal spark particle
├── content/
│   ├── block/
│   │   ├── MiningPortalBlock.java     # Portal block: entity transport, portal-finding/creation, dimension transition
│   │   ├── MiningPortalShape.java     # Validates portal frame geometry (reads via BlockGetter); fills interior with portal blocks
│   │   ├── PortalIgnitionHandler.java # NeoForge event subscriber; listens for igniter items on frame blocks
│   │   ├── PortalSearchStrategy.java  # Strategy interface for finding a portal placement position
│   │   ├── SpiralSearch.java          # Spirals outward from the target XZ, descending from the surface
│   │   ├── FallbackSearch.java        # Last resort: straight-down scan at the center XZ
│   │   ├── PortalPositionFinder.java  # Chains search strategies; snaps to surface Y in mining dimensions
│   │   ├── PortalStructureBuilder.java# Places frame, portal blocks, and landing platform
│   │   ├── PortalExitPlacement.java   # Pure math: exit spawn position and rotation for a portal rectangle
│   │   └── PortalGameTests.java       # GameTest suite for world-dependent behavior (runGameTestServer)
│   └── command/
│       └── ModCommands.java           # /egmd regenerate: evacuates players, gives manual regen instructions
└── core/
    ├── config/
    │   └── ModConfig.java             # NeoForge ModConfigSpec (portal search radius)
    └── registration/
        ├── ModBlocks.java             # DeferredRegister for all blocks (3 portal variants + pearl_block)
        ├── ModDimensions.java         # ResourceKeys for all 3 dimension types and level stems
        ├── ModItems.java              # DeferredRegister for block items (auto-registers from ModBlocks)
        ├── ModParticles.java          # DeferredRegister for particle types (portal_spark)
        ├── ModPoiTypes.java           # POI types for portal blocks (used for fast portal lookup)
        ├── PortalVariant.java         # Record describing one portal variant (frame, dimension, tint, surface Y)
        └── PortalVariants.java        # The three variant definitions (Overworld, Nether, End)
```

### Key Design Patterns

**Portal system flow:**
1. `PortalIgnitionHandler` catches `PlayerInteractEvent.RightClickBlock` for igniter items (`egmd:portal_igniters` tag or firestarter ability) on frame blocks. Its frame-to-portal lookup is built by `PortalIgnitionHandler.init()` during common setup.
2. `MiningPortalShape` validates the frame geometry (min 2×3 interior, max 21×21) and fills it with portal blocks.
3. `MiningPortalBlock` implements `Portal` and handles `entityInside` → `getPortalDestination`. It searches a configurable radius (default 128) for an existing portal POI in the target dimension, or finds a position via `PortalPositionFinder` and builds one via `PortalStructureBuilder`. Portals in mining dimensions are placed at each dimension's flat surface Y (see `ModDimensions`). Exit spawn position and rotation come from `PortalExitPlacement`.

**Dimensions are data-driven**: all three dimensions are fully defined in `src/main/resources/data/egmd/dimension/` as JSON (flat generator with custom biome). Ore generation uses `configured_feature` + `placed_feature` JSON pairs in `src/main/resources/data/egmd/worldgen/`.

**Each `MiningPortalBlock` instance** stores its target `ResourceKey<Level>` and required `frameBlock` — enabling a single generic class to handle all three portals with different behaviors.

**Event bus distinction**: `PortalIgnitionHandler` and `ModCommands` use `@EventBusSubscriber` (NeoForge bus). Client color handlers in `EGMDClient` subscribe on the mod event bus via `modEventBus.addListener`. Check which bus an event fires on before subscribing.

### Testing

Two tiers; both must pass before claiming done:

- **Unit tests** (`./gradlew test`): plain JUnit on the FML test runtime. Registries and mod blocks are loaded, but **no world exists** — `EphemeralTestServerProvider`'s server has no levels, so `server.overworld()` is null; never write a unit test that touches a real level. World reads/writes in unit tests go through the map-backed `FakeLevel` (`BlockGetter` + `LevelWriter`) in the test source set.
- **GameTests** (`./gradlew runGameTestServer`): `PortalGameTests` covers world-dependent behavior (structure building, searches, ignition end to end). Tests use the shipped empty template `data/egmd/structure/empty_7x7x7.nbt`; `@GameTestHolder` supplies the `egmd:` namespace, so `@GameTest(template = ...)` takes the bare template name.

## Coding Standards

- All registry objects use `DeferredRegister`.
- User-facing strings are translation keys in `src/main/resources/assets/egmd/lang/en_us.json`, never hardcoded.
- Client-only code lives in the `client` package with `@Mod(dist = Dist.CLIENT)`.
- Configuration goes through `ModConfig` (NeoForge `ModConfigSpec`).
- Log through `EGMD.LOGGER` (SLF4J), never `System.out`.
- Naming: `PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants, `lowercase.with.dots` packages.
- Explicit imports only: no wildcards, no unused imports.
- Javadoc on public APIs and complex logic.
- Mark nullable values with `@Nullable`; everything else is non-null by default (`@ParametersAreNonnullByDefault`).
- Keep per-tick work light; cache expensive calculations.

## Resource Locations

| Resource type | Path |
|---|---|
| Block/item models | `src/main/resources/assets/egmd/models/` |
| Block states | `src/main/resources/assets/egmd/blockstates/` |
| Translations | `src/main/resources/assets/egmd/lang/en_us.json` |
| Dimension JSON | `src/main/resources/data/egmd/dimension/` |
| Dimension type JSON | `src/main/resources/data/egmd/dimension_type/` |
| Biome JSON | `src/main/resources/data/egmd/worldgen/biome/` |
| Ore features | `src/main/resources/data/egmd/worldgen/{configured,placed}_feature/` |
| Loot tables | `src/main/resources/data/egmd/loot_table/` |
| Mod metadata template | `src/main/templates/META-INF/neoforge.mods.toml` |
| Generated resources | `src/generated/resources/` (do not edit manually) |
