package me.ichun.mods.hats.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkspaceHats extends Workspace
{
    public float lastDriftYaw, driftYaw;
    public float lastDriftPitch, driftPitch;
    public float lastCamDist, camDist;

    public WorkspaceHats(Screen lastScreen)
    {
        super(lastScreen, new TranslationTextComponent("hats.gui.selection.title"), Hats.configClient.guiMinecraftStyle);
    }

    @Override
    public void renderBackground(MatrixStack stack)
    {
//        this.renderBackground(stack, 0);

        RenderSystem.pushMatrix();
    }

    @Override
    public void resetBackground()
    {
        RenderSystem.popMatrix();
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void closeScreen()
    {
        super.closeScreen();

        Hats.eventHandlerClient.closeHatsMenu();
    }
}
