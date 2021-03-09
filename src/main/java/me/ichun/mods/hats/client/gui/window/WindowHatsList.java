package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.client.gui.window.element.ElementHatsScrollView;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class WindowHatsList extends Window<WorkspaceHats>
{
    public WindowHatsList(WorkspaceHats parent)
    {
        super(parent);

        setBorderSize(() -> (renderMinecraftStyle() ? 6 : 3));

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableTitle();

        setId("windowHatsList");

        setView(new ViewHatsList(this));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(parent.age <= Hats.configClient.guiAnimationTime)
        {
            float prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((parent.age + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            float reverseProg = 1F - prog;
            posX = (int)(Math.floor((parent.getWidth() / 2F)) + (Math.ceil((parent.getWidth() / 2F)) + 2) * reverseProg);
            if(parent.age == Hats.configClient.guiAnimationTime)
            {
                resize(parent.getMinecraft(), parent.width, parent.height);
            }
        }
        super.render(stack, mouseX, mouseY, partialTick);
    }

    @Override
    public void setScissor()
    {
        if(parent.age < Hats.configClient.guiAnimationTime)
        {
            RenderHelper.startGlScissor(getLeft(), getTop(), width * 2, height);
        }
        else
        {
            super.setScissor();
        }
    }

    public static class ViewHatsList extends View<WindowHatsList>
    {
        public ViewHatsList(@Nonnull WindowHatsList parent)
        {
            super(parent, "hats.gui.window.hatsList");

            int padding = 4;

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, padding)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, padding)
            );
            elements.add(sv);

            ElementHatsScrollView list = new ElementHatsScrollView(this).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).top(this, Constraint.Property.Type.TOP, padding + 1)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding + 1)
                    .left(this, Constraint.Property.Type.LEFT, padding + 1)
                    .right(sv, Constraint.Property.Type.LEFT, padding + 1)
            );
            elements.add(list);

            for(HatsSavedData.HatPart part : Hats.eventHandlerClient.hatsInventory.hatParts)
            {
                ElementHatRender hat = new ElementHatRender(list, part, btn -> {

                });
                hat.setSize(50, 70);
                list.addElement(hat);
            }
        }
    }
}
