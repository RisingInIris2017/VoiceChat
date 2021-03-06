package net.gliby.voicechat.client.gui.options;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiUIPlacement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiScreenOptionsUI extends GuiScreen {

    private final VoiceChatClient voiceChat;
    private final GuiScreen parent;
    private GuiBoostSlider opacity;


    public GuiScreenOptionsUI(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(this.parent);
                break;
            case 1:
                this.voiceChat.getSettings().resetUI(this.width, this.height);
                this.opacity.sliderValue = 1.0F;
                break;
            case 2:
                this.mc.displayGuiScreen(new GuiUIPlacement(this));
        }

    }

    @Override
    public void drawScreen(int x, int y, float time) {
        this.drawDefaultBackground();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2) - (float) (this.fontRenderer.getStringWidth("Immersive Voice Options") / 2) * 1.5F, 0.0F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 0.0F);
        this.drawString(this.mc.fontRenderer, "Immersive Voice Options", 0, 6, -1);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2 - this.fontRenderer.getStringWidth(I18n.format("menu.uiOptions")) / 2), 12.0F, 0.0F);
        this.drawString(this.mc.fontRenderer, I18n.format("menu.uiOptions"), 0, 12, -1);
        GL11.glPopMatrix();
        super.drawScreen(x, y, time);
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 34, 150, 20, I18n.format("gui.back")));
        buttonList.add(new GuiButton(1, this.width / 2 - 75, 73, 150, 20, I18n.format("menu.resetAll")));
        buttonList.add(new GuiButton(2, this.width / 2 - 150, 50, 150, 20, I18n.format("menu.uiPlacement")));
        buttonList.add(this.opacity = new GuiBoostSlider(-1, this.width / 2 + 2, 50, "", I18n.format("menu.uiOpacity") + ": " + (this.voiceChat.getSettings().getUIOpacity() == 0.0F ? I18n.format("options.off") : (int) (this.voiceChat.getSettings().getUIOpacity() * 100.0F) + "%"), 0.0F));
        this.opacity.sliderValue = this.voiceChat.getSettings().getUIOpacity();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.voiceChat.getSettings().getConfiguration().save();
    }

    @Override
    public void updateScreen() {
        super.onGuiClosed();
        this.voiceChat.getSettings().setUIOpacity(this.opacity.sliderValue);
        this.opacity.setDisplayString(I18n.format("menu.uiOpacity") + ": " + (this.voiceChat.getSettings().getUIOpacity() == 0.0F ? I18n.format("options.off") : (int) (this.voiceChat.getSettings().getUIOpacity() * 100.0F) + "%"));
    }
}
