# Cubimised API

**A client-side performance and rendering utility mod for Minecraft 1.20.1 (Fabric).**

Cubimised API brings performance controls, lightweight telemetry, and configurable rendering preferences into one small in-game interface. It is designed to make it easier to experiment with performance settings without repeatedly navigating Minecraft's options menus.

> **Early access / work in progress:** Features and compatibility may change. Back up worlds and configs before testing development builds. Some controls are experimental or currently provide only partial functionality; see the feature notes below.

## Introduction

Minecraft performance can vary widely depending on hardware, world complexity, render distance, entities, particles, and installed mods. Cubimised API aims to provide a simple place to monitor a few useful runtime statistics and quickly switch between performance preferences.

The mod is currently built for **Minecraft 1.20.1**, **Fabric Loader**, and **Java 17**. **Sodium is intentionally incompatible with Cubimised API** because Cubimised currently uses its own renderer-level mixins. Fabric Loader will reject a setup containing Sodium. FerriteCore is an optional recommended companion for memory optimization.

## Goals

- Provide accessible performance controls through a simple in-game screen.
- Offer quick presets for users who prefer performance, balanced settings, or visual quality.
- Show basic runtime information such as FPS, frame time, memory use, and entity count.
- Make client preferences persist across restarts.
- Offer optional client-side culling and particle-reduction experiments.
- Help users identify selected rendering-related mods for troubleshooting.
- Keep the project open to feedback, testing, and future improvements.

## Features

| Feature | Description / current status |
| --- | --- |
| Performance settings screen | Open the Cubimised settings screen in-game with the **O** key (default key binding). |
| One-click profiles | Choose **Potato**, **Balanced**, or **Quality** to apply a group of Cubimised preferences. |
| FPS and performance HUD | Displays FPS, approximate frame time, Java memory usage, and the world's regular entity count when enabled. |
| Smart FPS Booster | Experimental: monitors frame time and can enable particle reduction when performance is below its target. |
| Chunk loading optimizer | Currently applies a configurable maximum to Minecraft's client view-distance option while the smart booster is enabled. It is not a custom chunk scheduler. |
| Entity density control | Includes an entity-density setting used by the culling logic; effectiveness depends on the current implementation and should be tested in-game. |
| Entity / block-entity culling | Optional distance-based culling experiments intended to reduce rendering work at a distance. |
| Particle reduction | Optional particle suppression intended to reduce particle workload. This may change visual effects. |
| Config save system | Saves Cubimised settings in the Fabric config directory to restore them on subsequent launches. |
| Mod compatibility information | Lists selected rendering/performance-related mods detected in the Fabric mod list. Detection is informational. Sodium is declared incompatible at the Fabric Loader level. |
| Dynamic resolution | Experimental setting/state. Actual framebuffer/render-resolution scaling is not fully integrated yet; enabling it may not produce a visible resolution change. |

Feature behavior can change between commits. A successful Gradle build confirms compilation and packaging, not that every feature has been tested in a running Minecraft client.

## Installation

### Requirements

- Minecraft **1.20.1**
- Java **17**
- Fabric Loader **0.15.7 or newer** (the project currently uses 0.15.11)
- Fabric API for Minecraft 1.20.1

### Install a built JAR

1. Install Minecraft 1.20.1 and Fabric Loader.
2. Install the matching Fabric API release.
3. Download the Cubimised API JAR from the project's [GitHub Actions](https://github.com/sandy20240/Cubimised-API/actions) build artifacts or Releases, when a release is available.
4. Put the JAR in your Minecraft instance's `mods` folder.
5. Launch the Fabric 1.20.1 profile.

Only use JARs produced for the matching Minecraft and Fabric versions. If no downloadable artifact or release is available, build it from source using the steps below.

## Compatibility and recommended pairing

### Sodium

Cubimised API is intentionally declared incompatible with Sodium for the current 1.20.1 architecture. Sodium replaces major parts of Minecraft's rendering pipeline, while Cubimised currently uses renderer-level mixins of its own. The mod therefore declares Sodium in Fabric's `breaks` metadata so the loader refuses the combination instead of allowing a potentially unstable mixed renderer setup.

This is a compatibility restriction, not a claim that Sodium is a poor performance mod. Sodium's 1.20.1 releases are themselves designed as high-performance rendering replacements. citeturn0search1turn0search0

### FerriteCore

FerriteCore is an optional companion. It focuses on reducing Minecraft's memory usage rather than replacing the renderer, making it a complementary type of optimization for Cubimised's client performance tools. FerriteCore supports Fabric and Minecraft 1.20.x releases. citeturn0search8

## How to use Cubimised API

### 1. First launch

On first launch, Cubimised displays a welcome screen. Select **Let's Go!** to continue to the Minecraft title screen. The welcome acknowledgement is saved in the Fabric config directory.

### 2. Open the performance menu

1. Launch a world or remain at the title screen.
2. Press **O** (the default Cubimised performance-settings key).
3. Use the in-game buttons to toggle features or select a profile.
4. Select **Done** to close the screen.

If another mod uses the same key, open Minecraft's **Options → Controls → Key Binds** and search for Cubimised to change the binding.

### 3. Choose a performance profile

- **Potato** — enables the available culling/particle-saving preferences and a shorter culling distance.
- **Balanced** — applies a middle-ground set of preferences.
- **Quality** — disables Cubimised's culling and particle-reduction preferences and uses a longer culling distance.

These presets change Cubimised settings; they do not automatically tune every Minecraft or third-party mod option. You can still adjust individual toggles afterward.

### 4. Read the HUD

When **FPS + Dashboard** is enabled, the upper-left HUD shows:

- Current FPS reported by Minecraft.
- Approximate frame time in milliseconds.
- Java memory currently used and the maximum heap available to the game.
- Regular entity count reported by the client world.

Memory figures refer to the Java heap, not total system or graphics-card memory. HUD values are runtime estimates and can fluctuate.

### 5. Check detected mods

Open the performance screen and choose the compatibility information button. Cubimised lists recognized rendering/performance-related mods found in Fabric Loader's mod list. Use the information as a troubleshooting aid, not as a definitive compatibility report.

### 6. Settings and config file

Settings are stored in:

- **Windows:** `%AppData%/.minecraft/config/cubimised-api.properties`
- **Linux:** `~/.minecraft/config/cubimised-api.properties`
- **macOS:** `~/Library/Application Support/minecraft/config/cubimised-api.properties`

For a custom launcher or instance, use that instance's game directory. Close Minecraft before manually editing or removing the file. If you remove it, Cubimised will recreate it with default values on the next launch.

## Build from source

### Requirements

- Git
- JDK 17
- Internet access for Gradle to download dependencies

### Steps

```bash
git clone https://github.com/sandy20240/Cubimised-API.git
cd Cubimised-API
```

On Windows:

```powershell
.\gradlew.bat build
```

On Linux or macOS:

```bash
chmod +x ./gradlew
./gradlew build
```

The built mod JAR is placed in:

```
build/libs/
```

The repository's GitHub Actions workflow also builds the project when changes are pushed.

## Troubleshooting

- **The O key does nothing:** Confirm the mod is installed in the Fabric instance, then check or reassign the key in Options → Controls → Key Binds.
- **The mod does not load:** Confirm Minecraft is 1.20.1, Java is 17, and Fabric API is installed. Check the launcher log and `logs/latest.log`.
- **A setting does not appear to change performance:** Results depend on hardware, world conditions, other mods, and whether the feature is fully implemented. Not every setting guarantees an FPS increase.
- **Visual effects look different:** Turn off Reduced Particles or the relevant culling option in the Cubimised menu.
- **A rendering mod behaves unexpectedly:** Test with other rendering/performance mods disabled, one at a time, and share the relevant log with a bug report.

## Contributing and feedback

Bug reports, suggestions, and testing feedback are welcome through [GitHub Issues](https://github.com/sandy20240/Cubimised-API/issues). When reporting a problem, include:

- Minecraft and Fabric Loader versions.
- Java version.
- Cubimised API version or commit.
- Other installed mods, especially rendering/performance mods.
- Steps to reproduce and relevant parts of `logs/latest.log`.

## License

Cubimised API is licensed under the [MIT License](https://opensource.org/license/mit). See the repository's license file for the full terms.

---

Made by **sandy20240**.
