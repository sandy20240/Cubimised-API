package dev.cubimised.api.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/** One-time welcome screen for first launch. */
public final class WelcomeScreen extends Screen {
    public WelcomeScreen() {
        super(Text.literal("Welcome to Cubimised!"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Let's Go!"), button -> {
            CubimisedClient.markWelcomeSeen();
            if (this.client != null) {
                this.client.setScreen(new TitleScreen());
            }
        }).dimensions(this.width / 2 - 100, this.height - 42, 200, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 28, 0xFFFFFF);

        int x = this.width / 2;
        int y = 62;
        drawCenteredText(matrices, this.textRenderer, Text.literal("Your Minecraft experience, reimagined."), x, y, 0xAADDFF);

        String[] lines = {
            "Thank you for downloading this mod!",
            "This mod took me 61 hours to create, and I'm excited to share it with you.",
            "",
            "EARLY ACCESS",
            "This mod is still in early access. More Minecraft versions supported",
            "by Minecraft Transit Railway, as well as all mod loaders supported",
            "by Minecraft Transit Railway, will be added in future updates."
        };
        for (String line : lines) {
            if (line.isEmpty()) {
                y += 8;
            } else {
                drawCenteredText(matrices, this.textRenderer, Text.literal(line), x, y, line.equals("EARLY ACCESS") ? 0xFFFF55 : 0xFFFFFF);
                y += 16;
            }
        }

        drawCenteredText(matrices, this.textRenderer, Text.literal("By Sandy"), x, this.height - 92, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, Text.literal("World MTR"), x, this.height - 78, 0xAADDFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}