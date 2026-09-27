package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persists Cubimised client preferences in the Fabric config directory. */
public final class CubimisedConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("cubimised-api.properties");
    private static final Properties P = new Properties();
    private CubimisedConfig() {}

    public static void load() {
        if (Files.exists(FILE)) try (InputStream in = Files.newInputStream(FILE)) { P.load(in); }
        catch (IOException e) { System.err.println("[Cubimised] Config load failed: " + e.getMessage()); }
        CubimisedApi.showFps = bool("showFps", true);
        CubimisedApi.entityCullingEnabled = bool("entityCulling", true);
        CubimisedApi.blockEntityCullingEnabled = bool("blockEntityCulling", true);
        CubimisedApi.reduceParticles = bool("reduceParticles", false);
        CubimisedApi.dynamicResolutionEnabled = bool("dynamicResolution", false);
        CubimisedApi.smartBoosterEnabled = bool("smartBooster", true);
        CubimisedApi.entityDensity = integer("entityDensity", 100, 10, 100);
        CubimisedApi.chunkViewDistanceCap = integer("chunkViewDistanceCap", 16, 2, 32);
        CubimisedApi.performanceProfile = P.getProperty("profile", "Balanced");
        CubimisedApi.androidTurboEnabled = bool("androidTurbo", false);
    }
    private static boolean bool(String k, boolean d) { return Boolean.parseBoolean(P.getProperty(k, Boolean.toString(d))); }
    private static int integer(String k, int d, int min, int max) { try { return Math.max(min, Math.min(max, Integer.parseInt(P.getProperty(k, Integer.toString(d))))); } catch (NumberFormatException e) { return d; } }
    public static void save() {
        P.setProperty("showFps", Boolean.toString(CubimisedApi.showFps));
        P.setProperty("entityCulling", Boolean.toString(CubimisedApi.entityCullingEnabled));
        P.setProperty("blockEntityCulling", Boolean.toString(CubimisedApi.blockEntityCullingEnabled));
        P.setProperty("reduceParticles", Boolean.toString(CubimisedApi.reduceParticles));
        P.setProperty("dynamicResolution", Boolean.toString(CubimisedApi.dynamicResolutionEnabled));
        P.setProperty("smartBooster", Boolean.toString(CubimisedApi.smartBoosterEnabled));
        P.setProperty("entityDensity", Integer.toString(CubimisedApi.entityDensity));
        P.setProperty("chunkViewDistanceCap", Integer.toString(CubimisedApi.chunkViewDistanceCap));
        P.setProperty("profile", CubimisedApi.performanceProfile);
        P.setProperty("androidTurbo", Boolean.toString(CubimisedApi.androidTurboEnabled));
        try { Files.createDirectories(FILE.getParent()); try (OutputStream out = Files.newOutputStream(FILE)) { P.store(out, "Cubimised API client settings"); } }
        catch (IOException e) { System.err.println("[Cubimised] Config save failed: " + e.getMessage()); }
    }
}
