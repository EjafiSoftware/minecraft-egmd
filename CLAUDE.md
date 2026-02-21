# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
│   └── EGMDClient.java                # Client-only setup (@Mod dist=CLIENT); registers portal block color tints
├── content/
│   └── block/
│       ├── MiningPortalBlock.java     # Portal block: handles entity transport, portal-finding/creation, dimension transition
│       ├── MiningPortalShape.java     # Validates portal frame geometry; fills interior with portal blocks
│       └── PortalIgnitionHandler.java # NeoForge event subscriber; listens for Flint & Steel on frame blocks
│   └── command/
│       └── ModCommands.java           # /egmd regenerate: evacuates players from a mining dim, gives manual regen instructions
└── core/
    ├── config/
    │   └── ModConfig.java             # NeoForge ModConfigSpec (currently empty; scaffold for future config)
    └── registration/
        ├── ModBlocks.java             # DeferredRegister for all blocks (3 portal variants + pearl_block)
        ├── ModDimensions.java         # ResourceKeys for all 3 dimension types and level stems
        └── ModItems.java              # DeferredRegister for block items (auto-registers from ModBlocks)
```

### Key Design Patterns

**Portal system flow:**
1. `PortalIgnitionHandler` catches `PlayerInteractEvent.RightClickBlock` for Flint & Steel hits on frame blocks. It iterates `ModBlocks.BLOCKS` to find which `MiningPortalBlock` owns that frame type.
2. `MiningPortalShape` validates the frame geometry (min 2×3 interior, max 21×21) and fills it with portal blocks.
3. `MiningPortalBlock` implements `Portal` and handles `entityInside` → `getPortalDestination`. It searches a 128-block radius for an existing portal in the target dimension, or calls `createPortal`/`makePortalStructure` to build one. Portals in mining dimensions are placed at Y=68 (flat world surface).

**Dimensions are data-driven**: all three dimensions are fully defined in `src/main/resources/data/egmd/dimension/` as JSON (flat generator with custom biome). Ore generation uses `configured_feature` + `placed_feature` JSON pairs in `src/main/resources/data/egmd/worldgen/`.

**Each `MiningPortalBlock` instance** stores its target `ResourceKey<Level>` and required `frameBlock` — enabling a single generic class to handle all three portals with different behaviors.

**Event bus distinction**: `PortalIgnitionHandler` and `ModCommands` use `@EventBusSubscriber` (NeoForge bus). Client color handlers in `EGMDClient` subscribe on the mod event bus via `modEventBus.addListener`.

### Coding Standards (from AGENTS.md)

- All registry objects must use `DeferredRegister`.
- Never hardcode user-facing strings — use translation keys in `src/main/resources/assets/egmd/lang/en_us.json`.
- Separate client-only code into the `client` package with `@Mod(dist = Dist.CLIENT)`.
- Use `EGMD.LOGGER` (SLF4J) for all logging.
- Naming: `PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants, `lowercase.with.dots` packages.

### Resource Locations

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
