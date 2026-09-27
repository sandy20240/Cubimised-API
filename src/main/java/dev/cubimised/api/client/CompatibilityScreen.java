package dev.cubimised.api.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/** Lists detected renderer-related mods without claiming unsupported incompatibilities. */
public final class CompatibilityScreen extends Screen {
    private final Screen parent;
    private List<String> detected = List.of();
    public CompatibilityScreen(Screen parent) { super(Text.literal("Mod Compatibility")); this.parent=parent; }
    @Override protected void init() {
        detected = new ArrayList<>();
        for (var mod : FabricLoader.getInstance().getAllMods()) {
            String id=mod.getMetadata().getId().toLowerCase();
            if (id.contains("sodium") || id.contains("iris") || id.contains("optifine") || id.contains("indium") || id.contains("canvas") || id.contains("entityculling"))
                detected.add(mod.getMetadata().getName()+" ("+id+")");
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent)).dimensions(width/2-100,height-34,200,20).build());
    }
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer,title,width/2,18,0xFFFFFF);
        if(detected.isEmpty()) context.drawCenteredTextWithShadow(textRenderer,Text.literal("No recognized rendering/performance mods detected."),width/2,55,0xDDDDDD);
        else {
            context.drawCenteredTextWithShadow(textRenderer,Text.literal("Detected mods (informational only):"),width/2,42,0xDDDDDD);
            for(int i=0;i<Math.min(detected.size(),12);i++) context.drawCenteredTextWithShadow(textRenderer,Text.literal(detected.get(i)),width/2,60+i*15,0xFFFFFF);
        }
        context.drawCenteredTextWithShadow(textRenderer,Text.literal("Detection does not guarantee compatibility or incompatibility."),width/2,height-55,0xAAAAAA);
        super.render(context,mouseX,mouseY,delta);
    }
}
