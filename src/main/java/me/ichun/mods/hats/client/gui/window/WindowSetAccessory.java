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
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementPadding;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class WindowSetAccessory extends Window<WorkspaceHats>
{
    private final @Nonnull ElementHatRender<?> parentElement;

    public WindowSetAccessory(WorkspaceHats parent, @Nonnull ElementHatRender<?> parentElement)
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

        setView(new ViewSetAccessory(this));
    }


    @Override
    public void renderBackground(MatrixStack stack){}//No BG

    @Override
    public int getMinWidth()
    {
        return parentElement.getMinWidth();
    }

    @Override
    public int getMinHeight()
    {
        return parentElement.getMinHeight();
    }

    public static class ViewSetAccessory extends View<WindowSetAccessory>
    {
        public int age;
        public float renderTick = 0F;

        public ViewSetAccessory(@Nonnull WindowSetAccessory parent)
        {
            super(parent, "hats.gui.window.hat.personaliser");
        }

        @Override
        public void tick()
        {
            super.tick();

            age++;
        }

        @Override
        public void resize(Minecraft mc, int width, int height)
        {
            super.resize(mc, width, height);
        }

        @Override
        public void renderBackground(MatrixStack stack)
        {
            float prog = 1F;
            if(age <= Hats.configClient.guiAnimationTime)
            {
                prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((age + renderTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            }

            int hatsListPadding = 6;
            int singProg = (int)(hatsListPadding * prog);
            int doubProg = (int)((hatsListPadding * 2) * prog);

            //RENDER the hat border first

            int otherRenderHatsListPadding = 3;
            int hatViewWidth = parentFragment.parentElement.getWidth();
            int hatViewX = parentFragment.parentElement.posX;
            int targetElementX = ((WindowHatsList.ViewHatsList)parentFragment.parent.windowHatsList.getCurrentView()).list.getLeft() + otherRenderHatsListPadding;

            parentFragment.parentElement.setPosX((int)(hatViewX + (targetElementX - parentFragment.parentElement.getLeft()) * prog));
            parentFragment.parentElement.setWidth((int)(parentFragment.parentElement.width + (parentFragment.parentElement.getMinWidth() - parentFragment.parentElement.width) * prog));

            parentFragment.parentElement.posX -= singProg;
            parentFragment.parentElement.width += doubProg;
            parentFragment.parentElement.posY -= singProg;
            parentFragment.parentElement.height += doubProg;

            if(renderMinecraftStyle())
            {
                int width = parentFragment.parentElement.width;
                int height = parentFragment.parentElement.height;

                RenderSystem.enableAlphaTest();
                //draw the corners
                bindTexture(Fragment.VANILLA_TABS);

                //fill space
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getTop() + 4, width - 8, height - 8, 0, 4D/256D, 24D/256D, 36D/256D, 60D/256D); //fill space

                //draw borders
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getTop() + 4, 4, height - 8, 0, 0D/256D, 4D/256D, 36D/256D, 60D/256D); //left border
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getTop(), width - 8, 4, 0, 4D/256D, 24D/256D, 32D/256D, 36D/256D); //top border
                RenderHelper.draw(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getTop() + 4, 4, height - 8, 0, 24D/256D, 28D/256D, 36D/256D, 60D/256D); //right border
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getBottom() - 4, width - 8, 4, 0, 4D/256D, 24D/256D, 124D/256D, 128D/256D); //bottom left

                //draw corners
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getTop(), 4, 4, 0, 0D/256D, 4D/256D, 32D/256D, 36D/256D); //top left
                RenderHelper.draw(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getTop(), 4, 4, 0, 24D/256D, 28D/256D, 32D/256D, 36D/256D); //top right
                RenderHelper.draw(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getBottom() - 4, 4, 4, 0, 0D/256D, 4D/256D, 124D/256D, 128D/256D); //bottom left
                RenderHelper.draw(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getBottom() - 4, 4, 4, 0, 24D/256D, 28D/256D, 124D/256D, 128D/256D); //bottom left
            }
            else
            {
                parentFragment.parentElement.fill(stack, getTheme().windowBorder, 0);
                parentFragment.parentElement.fill(stack, getTheme().windowBackground, 3);
            }

            parentFragment.parentElement.height -= doubProg;
            parentFragment.parentElement.posY += singProg;
            parentFragment.parentElement.width -= doubProg;
            parentFragment.parentElement.posX += singProg;

            parentFragment.parentElement.setWidth(hatViewWidth);
            parentFragment.parentElement.setPosX(hatViewX);
            //end render the hat border first

            posX -= singProg;
            width += doubProg;
            posY -= singProg;
            height += doubProg;

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

            height -= doubProg;
            posY += singProg;
            width -= doubProg;
            posX += singProg;
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            int hatsListPadding = 3;

            float prog = 1F;
            if(age <= Hats.configClient.guiAnimationTime)
            {
                prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((age + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            }

            int hatViewWidth = parentFragment.parentElement.getWidth();
            int hatViewX = parentFragment.parentElement.posX;
            int targetElementX = ((WindowHatsList.ViewHatsList)parentFragment.parent.windowHatsList.getCurrentView()).list.getLeft() + hatsListPadding;

            parentFragment.parentElement.setPosX((int)(hatViewX + (targetElementX - parentFragment.parentElement.getLeft()) * prog));
            parentFragment.parentElement.setWidth((int)(parentFragment.parentElement.width + (parentFragment.parentElement.getMinWidth() - parentFragment.parentElement.width) * prog));

            //This is in relation of the new parentElementPosition
            int targetX = parentFragment.parentElement.getRight() + (hatsListPadding * 4);
            int targetWidth = 70; //50 width + 2x list padding and border (5 x 2) + padding to scroll (4) + scrolll (14) + 2x padding (6)//TODO update this? Calculate number of accessories to fit on screen and if we need a scroll bar or not

            parentFragment.posX = (int)(parentFragment.parentElement.getLeft() + (targetX - parentFragment.parentElement.getLeft()) * prog);
            parentFragment.width = (int)(parentFragment.parentElement.width + (targetWidth - parentFragment.parentElement.width) * prog);
            parentFragment.resize(getWorkspace().getMinecraft(), parentFragment.width, parentFragment.height);

            renderTick = partialTick;
            stack.push();

            stack.translate(0F, 0F, 375F);

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();

            parentFragment.parentElement.parentFragment.setScissor();


            //We're using RenderSystem instead of MatrixStack because of the entity render
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0F, 0F, 25F);

            parentFragment.parentElement.render(stack, mouseX, mouseY, partialTick);

            parentFragment.parentElement.setWidth(hatViewWidth);
            parentFragment.parentElement.setPosX(hatViewX);

            RenderSystem.popMatrix();

            resetScissorToParent();
        }

        @Override
        public void setScissor()
        {
            int hatRend = 50 + 12 + 3 + 6; //element + borders + padding + our border
            RenderHelper.startGlScissor(getLeft() - hatRend, getTop() - 6, width + hatRend + 6, height + 12);
        }

        @Override
        public int getMinWidth()
        {
            return parentFragment.getMinWidth();
        }

        @Override
        public int getMinHeight()
        {
            return parentFragment.getMinHeight();
        }
    }
}
