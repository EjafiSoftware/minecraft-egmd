# Coding Standards for Mining Dimension

This document outlines the coding standards and best practices for developing this Minecraft mod using NeoForge and Java.

## Core Principles

- **Clean Code**: Write code that is easy to read, understand, and maintain.
- **Clean Architecture**: Separate concerns and maintain a clear structure.
- **SOLID**: Follow the five SOLID principles of object-oriented design.
- **DRY (Don't Repeat Yourself)**: Avoid code duplication by abstracting common logic.
- **KISS (Keep It Simple, Stupid)**: Prefer simple solutions over complex ones.

## Java Coding Standards

- **Naming Conventions**:
    - Classes: `PascalCase`
    - Methods and Variables: `camelCase`
    - Constants: `UPPER_SNAKE_CASE`
    - Packages: `lowercase.with.dots`
- **Imports**: Always optimize imports. Remove unused imports and avoid wildcard imports.
- **Formatting**: Use consistent indentation (standard 4 spaces or as defined by the project's IDE settings).
- **Documentation**: Use Javadoc for public APIs and complex logic.
- **Null Safety**: Use `@Nullable` and `@ParametersAreNonnullByDefault` (or similar annotations) where appropriate to avoid `NullPointerException`.

## NeoForge & Minecraft Modding Best Practices

- **Registry Management**: Use `DeferredRegister` for all registry objects (Blocks, Items, EntityTypes, etc.).
- **Events**:
    - Use `@SubscribeEvent` on methods within classes registered to the event bus.
    - Prefer functional event listeners where appropriate.
    - Be mindful of the bus the event is fired on (NeoForge vs Mod bus).
- **Side Management**: Always respect physical vs. logical sides. Use `@OnlyIn` (with caution) or `DistExecutor` / `SidedProxy` patterns when necessary, though NeoForge encourages cleaner alternatives.
- **Localization**: Never hardcode strings that are displayed to the user. Use translation keys in `lang` files.
- **Performance**:
    - Avoid heavy operations during game ticks if possible.
    - Use caching for expensive calculations.
    - Be mindful of memory usage, especially with large data structures.

## Architecture

- **Separation of Concerns**:
    - Logic should be separated from presentation (rendering).
    - Mod configuration should be handled by a dedicated config system.
    - Common utility methods should be placed in utility classes.
- **Dependency Injection**: Use dependency injection where possible to make code more testable and modular.

## Quality Assurance

- **Testing**: Write unit tests for logic that can be tested independently of the Minecraft environment.
- **Logging**: Use a logger to record important information, warnings, and errors. Avoid `System.out.println`.
