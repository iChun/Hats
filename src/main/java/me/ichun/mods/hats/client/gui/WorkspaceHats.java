package me.ichun.mods.hats.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.window.WindowHatsList;
import me.ichun.mods.hats.client.gui.window.WindowInputReceiver;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

public class WorkspaceHats extends Workspace
{
    public final boolean fallback;
    public final @Nonnull LivingEntity hatEntity;
    public final String hatDetails;

    public int age;

    public WindowHatsList windowHatsList;

    public WorkspaceHats(Screen lastScreen, boolean fallback, @Nonnull LivingEntity hatEntity) //TODO too dark, fallback
    {
        super(lastScreen, new TranslationTextComponent("hats.gui.selection.title"), Hats.configClient.guiMinecraftStyle);
        windows.add(new WindowInputReceiver(this));

        this.fallback = fallback || hatEntity != Minecraft.getInstance().player;
        this.hatEntity = hatEntity;
        this.hatDetails = HatHandler.getHatDetails(hatEntity);

        addWindow(windowHatsList = new WindowHatsList(this));
    }

    @Override
    protected void init()
    {
        //TODO ADD the hats list

        int padding = 10;
        windowHatsList.setConstraint(new Constraint(windowHatsList).right(this, Constraint.Property.Type.RIGHT, padding).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding));
        windowHatsList.setWidth((int)Math.floor((getWidth() / 2F)) - padding);
        windowHatsList.constraint.apply();

        super.init(); //TODO render the player in fallback mode.
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        int padding = 10;
        windowHatsList.setWidth((int)Math.floor((width / 2F)) - padding);
        super.resize(mc, width, height);
    }

    @Override
    public boolean canDockWindows()
    {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        return (this.getListener() != null && this.isDragging()) && this.getListener().mouseDragged(mouseX, mouseY, button, distX, distY);
    }

    @Override
    public void renderBackground(MatrixStack stack)
    {
        if(fallback)
        {
            this.renderBackground(stack, 0);
        }

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

        age++;
    }

    @Override
    public void onClose()
    {
        super.onClose();

        HatHandler.assignSpecificHat(hatEntity, hatDetails); //Reset

        Hats.eventHandlerClient.closeHatsMenu();
    }
}
