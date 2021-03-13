package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class WindowHatOptions extends Window<WorkspaceHats>
{
    private final @Nonnull ElementHatRender<?> parentElement;
    private final int parentLeft, parentTop;
    private boolean killed;

    public WindowHatOptions(WorkspaceHats parent, @Nonnull ElementHatRender<?> parentElement)
    {
        super(parent);

        setBorderSize(() -> 0);

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableDragResize();
        disableTitle();

        this.parentElement = parentElement;
        this.parentLeft = parentElement.getLeft();
        this.parentTop = parentElement.getTop();

        setView(new ViewHatOptions(this));
    }

    @Override
    public void renderBackground(MatrixStack stack){}//No BG

    @Override
    public int getMinWidth()
    {
        return parentElement.getWidth() + 21; //button +1 padding?
    }

    @Override
    public void unfocus(@Nullable IGuiEventListener guiReplacing)
    {
        super.unfocus(guiReplacing);
        if(!killed)
        {
            killed = true;
            parent.removeWindow(this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(parentLeft != parentElement.getLeft() || parentTop != parentElement.getTop())
        {
            killed = true;
            parent.removeWindow(this);
            return;
        }

        if(!killed)
        {
            super.render(stack, mouseX, mouseY, partialTick);
        }
    }

    public static class ViewHatOptions extends View<WindowHatOptions>
    {
        public static ResourceLocation TEX_COLOURISE = new ResourceLocation("hats", "textures/icon/colourise.png");
        public static ResourceLocation TEX_PERSONALISE = new ResourceLocation("hats", "textures/icon/personalise.png");
        public static ResourceLocation TEX_FAVOURITE = new ResourceLocation("hats", "textures/icon/favourite.png");

        public ArrayList<Element<?>> buttons = new ArrayList<>();

        public int age;

        public ViewHatOptions(@Nonnull WindowHatOptions parent)
        {
            super(parent, "hats.gui.window.hat.sidebar");

            ElementButtonTextured<?> btnStack;
            ElementButtonTextured<?> btnStackLast;

            int padding = (parent.parentElement.getHeight() - 60) / 2;

            //COLOURISE
            btnStack = new ElementButtonTextured<>(this, TEX_COLOURISE, btn -> {
                WindowSetColouriser windowSetColouriser = new WindowSetColouriser(parent.parent, parent.parentElement);
                windowSetColouriser.pos(parent.parentElement.getLeft(), parent.parentElement.getTop());
                windowSetColouriser.size(parent.parentElement.getWidth(), parent.parentElement.getHeight());
                parent.parent.addWindowWithGreyout(windowSetColouriser);
                windowSetColouriser.init();

                parent.parent.removeWindow(parent);
            });
            btnStack.setTooltip(I18n.format("hats.gui.button.colourise"));
            btnStack.setSize(20, 20);
            btnStack.constraints().right(this, Constraint.Property.Type.LEFT, 1).top(this, Constraint.Property.Type.TOP, 0);
            elements.add(btnStack);
            buttons.add(btnStack);
            btnStackLast = btnStack;

            //ACCESSORISE
            btnStack = new ElementButtonTextured<>(this, TEX_PERSONALISE, btn -> {
                WindowSetAccessory windowSetAccessory = new WindowSetAccessory(parent.parent, parent.parentElement);
                windowSetAccessory.pos(parent.parentElement.getLeft(), parent.parentElement.getTop());
                windowSetAccessory.size(parent.parentElement.getWidth(), parent.parentElement.getHeight());
                parent.parent.addWindowWithGreyout(windowSetAccessory);
                windowSetAccessory.init();

                parent.parent.removeWindow(parent);
            }); //TODO open a new GUI for accesorise?
            btnStack.setTooltip(I18n.format("hats.gui.button.personalise"));
            btnStack.setSize(20, 20);
            btnStack.constraints().right(this, Constraint.Property.Type.LEFT, 1).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnStack);
            buttons.add(btnStack);
            btnStackLast = btnStack;

            //FAVOURITE
            ElementToggleTextured<?> btnToggle = new ElementToggleTextured<>(this, I18n.format("hats.gui.button.favouriteHat"), TEX_FAVOURITE, btn -> {}); //TODO handle this
            btnToggle.setSize(20, 20);
            btnToggle.constraints().right(this, Constraint.Property.Type.LEFT, 1).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnToggle);
            buttons.add(btnToggle);
        }

        @Override
        public void renderBackground(MatrixStack stack){}

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount)
        {
            return super.mouseScrolled(mouseX, mouseY, amount); //TODO allow scroll passthrough
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            int offsetTime = 2;
            if(age <= buttons.size() * offsetTime + Hats.configClient.guiAnimationTime)
            {
                for(int i = 0; i < buttons.size(); i++)
                {
                    Element<?> element = buttons.get(i);

                    float prog = (float)Math.sin(Math.toRadians(MathHelper.clamp((((age - i * offsetTime) + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
                    float reverseProg = 1F - prog;

                    element.posX = (int)(21 * reverseProg);
                }
            }

            stack.push();

            stack.translate(0F, 0F, 375F);

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();

            parentFragment.parentElement.parentFragment.setScissor();

            parentFragment.parentElement.render(stack, mouseX, mouseY, partialTick);

            resetScissorToParent();
        }

        @Override
        public void tick()
        {
            super.tick();
            age++;
        }
    }
}
