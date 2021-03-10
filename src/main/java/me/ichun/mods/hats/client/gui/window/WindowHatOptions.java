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
        public boolean showColouriser;

        public ViewHatOptions(@Nonnull WindowHatOptions parent)
        {
            super(parent, "hats.gui.window.hat.sidebar");

            ElementButtonTextured<?> btnStack;
            ElementButtonTextured<?> btnStackLast;

            int padding = (parent.parentElement.getHeight() - 60) / 2;

            //COLOURISE
            btnStack = new ElementButtonTextured<>(this, TEX_COLOURISE, btn -> {
                showColouriser = !showColouriser;
                handleShowColouriser(showColouriser);
            }); //TODO support for coloriser
            btnStack.setTooltip(I18n.format("hats.gui.button.colourise"));
            btnStack.setSize(20, 20);
            btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(this, Constraint.Property.Type.TOP, 0);
            elements.add(btnStack);
            buttons.add(btnStack);
            btnStackLast = btnStack;

            //ACCESSORISE
            btnStack = new ElementButtonTextured<>(this, TEX_PERSONALISE, btn -> {}); //TODO open a new GUI for accesorise?
            btnStack.setTooltip(I18n.format("hats.gui.button.personalise"));
            btnStack.setSize(20, 20);
            btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnStack);
            buttons.add(btnStack);
            btnStackLast = btnStack;

            //FAVOURITE
            ElementToggleTextured<?> btnToggle = new ElementToggleTextured<>(this, I18n.format("hats.gui.button.favouriteHat"), TEX_FAVOURITE, btn -> {}); //TODO handle this
            btnToggle.setSize(20, 20);
            btnToggle.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnToggle);
            buttons.add(btnToggle);
        }

        @Override
        public void renderBackground(MatrixStack stack)
        {
            if(showColouriser)
            {
                renderColourizerBackground(stack);
            }
        }

        private void renderColourizerBackground(MatrixStack stack)
        {
            posX += 21;
            width -= 21;
            if(renderMinecraftStyle())
            {
                RenderSystem.enableAlphaTest();
                //draw the corners
                bindTexture(Fragment.VANILLA_TABS);

                //fill space
                RenderHelper.draw(stack, getLeft() + 4, getTop() + 4, width - 8, height - 8, 0, 4D/256D, 24D/256D, 36D/256D, 60D/256D); //fill space

                //draw borders
                RenderHelper.draw(stack, getLeft(), getTop() + 4, 4, height - 8, 0, 0D/256D, 4D/256D, 36D/256D, 60D/256D); //left border
                RenderHelper.draw(stack, getLeft() + 4, getTop(), width - 8, 4, 0, 4D/256D, 24D/256D, 32D/256D, 36D/256D); //top border
                RenderHelper.draw(stack, getRight() - 4, getTop() + 4, 4, height - 8, 0, 24D/256D, 28D/256D, 36D/256D, 60D/256D); //right border
                RenderHelper.draw(stack, getLeft() + 4, getBottom() - 4, width - 8, 4, 0, 4D/256D, 24D/256D, 124D/256D, 128D/256D); //bottom left

                //draw corners
                RenderHelper.draw(stack, getLeft(), getTop(), 4, 4, 0, 0D/256D, 4D/256D, 32D/256D, 36D/256D); //top left
                RenderHelper.draw(stack, getRight() - 4, getTop(), 4, 4, 0, 24D/256D, 28D/256D, 32D/256D, 36D/256D); //top right
                RenderHelper.draw(stack, getLeft(), getBottom() - 4, 4, 4, 0, 0D/256D, 4D/256D, 124D/256D, 128D/256D); //bottom left
                RenderHelper.draw(stack, getRight() - 4, getBottom() - 4, 4, 4, 0, 24D/256D, 28D/256D, 124D/256D, 128D/256D); //bottom left
            }
            else
            {
                fill(stack, getTheme().windowBorder, 0);
                fill(stack, getTheme().windowBackground, 3);
            }
            width += 21;
            posX -= 21;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount)
        {
            return super.mouseScrolled(mouseX, mouseY, amount); //TODO allow scroll passthrough
        }

        public void handleShowColouriser(boolean show)
        {
            if(show)
            {
                ElementButton<?> reset = new ElementButton<>(this, "hats.gui.button.reset", btn -> {

                });
                reset.setSize(parentFragment.parentElement.getWidth() - 10, 14);
                reset.constraints().bottom(this, Constraint.Property.Type.BOTTOM, 3).right(this, Constraint.Property.Type.RIGHT, 5);
                reset.setId("colouriserReset");
                elements.add(reset);

                //Scrolls minimum width = 14.
                int scrollPadding = (int)Math.floor(((getWidth() - 21) - (14 * 3)) / 4F);
                ElementScrollBar<?> svB = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.13F); //100%?
                svB.constraints().top(this, Constraint.Property.Type.TOP, 3).bottom(reset, Constraint.Property.Type.TOP, 2).right(this, Constraint.Property.Type.RIGHT, scrollPadding);
                svB.setId("colouriserB");
                svB.setScrollProg(0F); //TODO set this
                elements.add(svB);

                ElementTextWrapper textB = new ElementTextWrapper(this);
                textB.setText("B");
                textB.constraints().bottom(reset, Constraint.Property.Type.TOP, 1).left(svB, Constraint.Property.Type.LEFT, 0);
                textB.setId("colouriserTextB");
                elements.add(textB);

                ElementScrollBar<?> svG = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.13F); //100%?
                svG.constraints().top(this, Constraint.Property.Type.TOP, 3).bottom(reset, Constraint.Property.Type.TOP, 2).right(svB, Constraint.Property.Type.LEFT, scrollPadding);
                svG.setId("colouriserG");
                svG.setScrollProg(0F); //TODO set this
                elements.add(svG);

                ElementTextWrapper textG = new ElementTextWrapper(this);
                textG.setText("G");
                textG.constraints().bottom(reset, Constraint.Property.Type.TOP, 1).left(svG, Constraint.Property.Type.LEFT, 0);
                textG.setId("colouriserTextG");
                elements.add(textG);

                ElementScrollBar<?> svR = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.13F); //100%?
                svR.constraints().top(this, Constraint.Property.Type.TOP, 3).bottom(reset, Constraint.Property.Type.TOP, 2).right(svG, Constraint.Property.Type.LEFT, scrollPadding);
                svR.setId("colouriserR");
                svR.setScrollProg(0F); //TODO set this
                elements.add(svR);

                ElementTextWrapper textR = new ElementTextWrapper(this);
                textR.setText("R");
                textR.constraints().bottom(reset, Constraint.Property.Type.TOP, 1).left(svR, Constraint.Property.Type.LEFT, 0);
                textR.setId("colouriserTextR");
                elements.add(textR);

                this.init();
            }
            else
            {
                elements.removeIf(e -> e.id != null && e.id.startsWith("colouriser"));

                this.init();
            }
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

            if(!showColouriser)
            {
                parentFragment.parentElement.parentFragment.setScissor();

                parentFragment.parentElement.render(stack, mouseX, mouseY, partialTick);

                resetScissorToParent();
            }
        }

        @Override
        public void tick()
        {
            super.tick();
            age++;
        }
    }
}
