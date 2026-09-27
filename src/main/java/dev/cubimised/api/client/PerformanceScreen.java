package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import java.util.List;
import java.util.stream.Collectors;

/** In-game performance controls, profiles, and compatibility information. */
public final class PerformanceScreen extends Screen {
    private final Screen parent;
    public PerformanceScreen(Screen parent) { super(Text.literal("Cubimised V1.1 Performance")); this.parent = parent; }

    @Override protected void init() {
        int x = width / 2 - 150, y = 42, w = 300;
        addDrawableChild(ButtonWidget.builder(profileText("Potato"), b -> applyProfile("Potato")).dimensions(x,y,w,20).build());
        addDrawableChild(ButtonWidget.builder(profileText("Balanced"), b -> applyProfile("Balanced")).dimensions(x,y+24,w,20).build());
        addDrawableChild(ButtonWidget.builder(profileText("Quality"), b -> applyProfile("Quality")).dimensions(x,y+48,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Performance Monitor [L]", CubimisedApi.performanceMonitorEnabled), b -> { CubimisedApi.performanceMonitorEnabled=!CubimisedApi.performanceMonitorEnabled; b.setMessage(toggle("Performance Monitor [L]",CubimisedApi.performanceMonitorEnabled)); save(); }).dimensions(x,y+78,w,20).build());\n        addDrawableChild(ButtonWidget.builder(toggle("FPS + Dashboard", CubimisedApi.showFps), b -> { CubimisedApi.showFps=!CubimisedApi.showFps; b.setMessage(toggle("FPS + Dashboard",CubimisedApi.showFps)); save(); }).dimensions(x,y+78,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Smart FPS Booster", CubimisedApi.smartBoosterEnabled), b -> { CubimisedApi.smartBoosterEnabled=!CubimisedApi.smartBoosterEnabled; b.setMessage(toggle("Smart FPS Booster",CubimisedApi.smartBoosterEnabled)); save(); }).dimensions(x,y+102,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Entity Culling", CubimisedApi.entityCullingEnabled), b -> { CubimisedApi.entityCullingEnabled=!CubimisedApi.entityCullingEnabled; b.setMessage(toggle("Entity Culling",CubimisedApi.entityCullingEnabled)); save(); }).dimensions(x,y+126,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Block Entity Culling", CubimisedApi.blockEntityCullingEnabled), b -> { CubimisedApi.blockEntityCullingEnabled=!CubimisedApi.blockEntityCullingEnabled; b.setMessage(toggle("Block Entity Culling",CubimisedApi.blockEntityCullingEnabled)); save(); }).dimensions(x,y+150,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Reduced Particles", CubimisedApi.reduceParticles), b -> { CubimisedApi.reduceParticles=!CubimisedApi.reduceParticles; b.setMessage(toggle("Reduced Particles",CubimisedApi.reduceParticles)); save(); }).dimensions(x,y+174,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Dynamic Resolution (experimental)", CubimisedApi.dynamicResolutionEnabled), b -> { CubimisedApi.dynamicResolutionEnabled=!CubimisedApi.dynamicResolutionEnabled; b.setMessage(toggle("Dynamic Resolution (experimental)",CubimisedApi.dynamicResolutionEnabled)); save(); }).dimensions(x,y+198,w,20).build());
        addDrawableChild(ButtonWidget.builder(toggle("Android Low-End Turbo", CubimisedApi.androidTurboEnabled), b -> { CubimisedApi.androidTurboEnabled=!CubimisedApi.androidTurboEnabled; if(CubimisedApi.androidTurboEnabled){ CubimisedApi.performanceProfile="Potato"; CubimisedApi.smartBoosterEnabled=true; CubimisedApi.dynamicResolutionEnabled=true; CubimisedApi.entityCullingEnabled=true; CubimisedApi.blockEntityCullingEnabled=true; CubimisedApi.reduceParticles=true; CubimisedApi.targetFps=200; CubimisedApi.chunkViewDistanceCap=Math.min(CubimisedApi.chunkViewDistanceCap,10); } b.setMessage(toggle("Android Low-End Turbo",CubimisedApi.androidTurboEnabled)); save(); }).dimensions(x,y+222,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Compatibility: " + compatibilitySummary()), b -> client.setScreen(new CompatibilityScreen(this))).dimensions(x,y+246,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x,y+270,w,20).build());
    }
    private Text profileText(String p) { return Text.literal((CubimisedApi.performanceProfile.equals(p) ? "✓ " : "") + "Profile: " + p); }
    private Text toggle(String label, boolean enabled) { return Text.literal(label + ": " + (enabled ? "ON" : "OFF")); }
    private void applyProfile(String profile) {
        CubimisedApi.performanceProfile=profile;
        switch(profile) {
            case "Potato" -> { CubimisedApi.entityCullingEnabled=true; CubimisedApi.blockEntityCullingEnabled=true; CubimisedApi.reduceParticles=true; CubimisedApi.smartBoosterEnabled=true; CubimisedApi.updateCullingDistance(64); }
            case "Quality" -> { CubimisedApi.entityCullingEnabled=false; CubimisedApi.blockEntityCullingEnabled=false; CubimisedApi.reduceParticles=false; CubimisedApi.smartBoosterEnabled=false; CubimisedApi.updateCullingDistance(256); }
            default -> { CubimisedApi.entityCullingEnabled=true; CubimisedApi.blockEntityCullingEnabled=true; CubimisedApi.reduceParticles=false; CubimisedApi.smartBoosterEnabled=true; CubimisedApi.updateCullingDistance(128); }
        }
        clearAndInit(); save();
    }
    private void save() { CubimisedConfig.save(); }
    private String compatibilitySummary() {
        List<String> mods=FabricLoader.getInstance().getAllMods().stream().map(m->m.getMetadata().getId().toLowerCase()).filter(id->id.contains("sodium")||id.contains("iris")||id.contains("optifine")||id.contains("indium")||id.contains("canvas")).collect(Collectors.toList());
        return mods.isEmpty() ? "No known renderer mods detected" : mods.size()+" renderer mod(s) detected";
    }
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer,title,width/2,18,0xFFFFFF);
        super.render(context,mouseX,mouseY,delta);
    }
    @Override public void close() { CubimisedConfig.save(); if(client!=null) client.setScreen(parent); }
}
