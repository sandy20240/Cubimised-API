# Multi-version and multi-loader development

## Scope

Cubimised API is targeting MTR-compatible Minecraft lines and intentionally excludes Minecraft 1.21.x for now.

| Minecraft | Fabric | Forge | NeoForge | State |
|---|---|---|---|---|
| 1.16.5 | Planned | Planned | Not targeted | Planned |
| 1.18.2 | Planned | Planned | Not targeted | Planned |
| 1.19.2 | Planned | Planned | Not targeted | Planned |
| 1.20.1 | Existing baseline | Planned | Planned (where toolchain support applies) | Fabric baseline only |

The authoritative machine-readable list is `compatibility/targets.json`.

## Architecture direction

- Keep loader entrypoints and loader-specific integrations isolated from shared optimization logic.
- Use separate loader modules/source sets for Fabric, Forge, and NeoForge rather than shipping Fabric classes as if they were cross-loader compatible.
- Keep Minecraft-version-specific mixins and mappings isolated per version. Mixin descriptors and Minecraft internals are not binary-compatible across these versions.
- Select Java and Gradle toolchains per Minecraft target; 1.16.5 does not use the same Java baseline as the newer targets.
- Do not advertise a loader/version combination as supported until its artifact builds and launches in a smoke test.

## Current status

This commit adds the compatibility target manifest and migration plan. The current source and Gradle build remain the existing Fabric 1.20.1 implementation; this commit does **not** yet provide Forge/NeoForge jars or working builds for the older versions. Those require version-specific source and mixin ports plus loader modules, and should be added in tested increments.

## Build baseline

The root Gradle project remains the existing Fabric 1.20.1 baseline. Avoid changing its Minecraft, mappings, or loader coordinates to an older target without a matching source port, because doing so can make the current project fail to compile.
