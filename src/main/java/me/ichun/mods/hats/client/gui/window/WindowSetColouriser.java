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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class WindowSetColouriser extends Window<WorkspaceHats>
{
    private final @Nonnull ElementHatRender<?> parentElement;

    public WindowSetColouriser(WorkspaceHats parent, @Nonnull ElementHatRender<?> parentElement)
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
        isNotUnique();

        this.parentElement = parentElement;

        setView(new ViewSetColouriser(this));
    }

    @Override
    public void setScissor()
    {
        currentView.setScissor();
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

    public static class ViewSetColouriser extends View<WindowSetColouriser>
    {
        public static ResourceLocation TEX_RESET = new ResourceLocation("hats", "textures/icon/reset.png");

        public ElementButtonTextured<?> toggleHSBtoRGB;
        public ElementButtonTextured<?> reset;

        public ElementPadding padding;
        public ElementScrollBar<?> svR;
        public ElementScrollBar<?> svG;
        public ElementScrollBar<?> svB;
        public ElementScrollBar<?> svA;
        public ElementTextWrapper textR;
        public ElementTextWrapper textG;
        public ElementTextWrapper textB;
        public ElementTextWrapper textA;

        public ElementScrollBar<?> svH;
        public ElementScrollBar<?> svS;
        public ElementScrollBar<?> svV;
        public ElementTextWrapper textH;
        public ElementTextWrapper textS;
        public ElementTextWrapper textV;
        public ElementToggle togEnchanted;

        public boolean showRGB;
        public int age;
        public float renderTick = 0F;
        public float lastProg = 0F;

        public ViewSetColouriser(@Nonnull WindowSetColouriser parent)
        {
            super(parent, "hats.gui.window.hat.colorizer");

            padding = new ElementPadding(this, parentFragment.parentElement.getMinWidth(), parentFragment.parentElement.getMinHeight());
            padding.constraints().top(this, Constraint.Property.Type.TOP, 0).right(this, Constraint.Property.Type.RIGHT, 0);
            elements.add(padding);

            svR = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svR.setId("colouriserR");
            svR.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.colouriser[0] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.colouriser[0]);
            elements.add(svR);

            textR = new ElementTextWrapper(this);
            textR.setText("R");
            elements.add(textR);

            svG = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svG.setId("colouriserG");
            svG.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.colouriser[1] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.colouriser[1]);
            elements.add(svG);

            textG = new ElementTextWrapper(this);
            textG.setText("G");
            elements.add(textG);

            svB = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svB.setId("colouriserB");
            svB.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.colouriser[2] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.colouriser[2]);
            elements.add(svB);

            textB = new ElementTextWrapper(this);
            textB.setText("B");
            elements.add(textB);

            svA = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svA.setId("colouriserA");
            svA.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.colouriser[3] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.colouriser[3]);
            elements.add(svA);

            textA = new ElementTextWrapper(this);
            textA.setText("A");
            elements.add(textA);

            svH = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svH.setId("hsbiserH");
            svH.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.hsbiser[0] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.hsbiser[0]);
            elements.add(svH);

            textH = new ElementTextWrapper(this);
            textH.setText("H");
            elements.add(textH);

            svS = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svS.setId("hsbiserS");
            svS.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.hsbiser[1] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.hsbiser[1]);
            elements.add(svS);

            textS = new ElementTextWrapper(this);
            textS.setText("S");
            elements.add(textS);

            svV = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.1F); //100%?
            svV.setId("hsbiserB");
            svV.setCallback(scrollbar -> {
                parent.parentElement.hatLevel.hsbiser[2] = 1F - scrollbar.scrollProg;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            }).setScrollProg(1F - parent.parentElement.hatLevel.hsbiser[2]);
            elements.add(svV);

            textV = new ElementTextWrapper(this);
            textV.setText("B"); //this is not a typo, yes it's a B
            elements.add(textV);

            togEnchanted = new ElementToggle<>(this, "hats.gui.button.glint", btn -> {
                parent.parentElement.hatLevel.enchanted = btn.toggleState;
                parent.parent.setNewHat(parent.parentElement.hatOrigin.setModifier(parent.parentElement.hatLevel), true);
            });
            togEnchanted.setToggled(parent.parentElement.hatLevel.enchanted);
            togEnchanted.setId("toggleEnchant");
            elements.add(togEnchanted);

            toggleHSBtoRGB = new ElementButtonTextured<>(this, WindowHatOptions.ViewHatOptions.TEX_COLOURISE, btn -> {
                showRGB = !showRGB;
                if(showRGB)
                {
                    btn.setTooltip(I18n.format("hats.gui.button.hsb"));
                }
                else
                {
                    btn.setTooltip(I18n.format("hats.gui.button.rgb"));
                }
                updateScrollBars();
            });
            toggleHSBtoRGB.setTooltip(I18n.format("gui.ok"));
            toggleHSBtoRGB.setSize(20, 20);
            toggleHSBtoRGB.constraints().right(padding, Constraint.Property.Type.LEFT, 3).top(padding, Constraint.Property.Type.TOP, 0);
            elements.add(toggleHSBtoRGB);

            reset = new ElementButtonTextured<>(this, TEX_RESET, btn -> {
                if(Screen.hasShiftDown())
                {
                    if(showRGB)
                    {
                        svR.setScrollProg(1F);
                        svG.setScrollProg(1F);
                        svB.setScrollProg(1F);
                        svA.setScrollProg(1F);
                    }
                    else
                    {
                        svH.setScrollProg(1F);
                        svS.setScrollProg(1F);
                        svV.setScrollProg(1F);
                        togEnchanted.setToggled(false);
                        togEnchanted.callback.accept(togEnchanted);
                    }
                }
                else
                {
                    svR.setScrollProg(1F);
                    svG.setScrollProg(1F);
                    svB.setScrollProg(1F);
                    svA.setScrollProg(1F);

                    svH.setScrollProg(1F);
                    svS.setScrollProg(1F);
                    svV.setScrollProg(1F);
                    togEnchanted.setToggled(false);
                    togEnchanted.callback.accept(togEnchanted);
                }
            });
            reset.setTooltip(I18n.format("hats.gui.button.reset"));
            reset.setSize(20, 20);
            reset.constraints().right(padding, Constraint.Property.Type.LEFT, 3).bottom(padding, Constraint.Property.Type.BOTTOM, 0);
            elements.add(reset);

            updateScrollBars();
        }

        public void updateScrollBars()
        {
            int edgePadding = 3;
            if(showRGB)
            {
                svR.constraints().top(padding, Constraint.Property.Type.TOP, 1).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textR.constraints().bottom(svR, Constraint.Property.Type.BOTTOM, 0).left(svR, Constraint.Property.Type.LEFT, 1);
                svG.constraints().top(svR, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textG.constraints().bottom(svG, Constraint.Property.Type.BOTTOM, 0).left(svG, Constraint.Property.Type.LEFT, 1);
                svB.constraints().top(svG, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textB.constraints().bottom(svB, Constraint.Property.Type.BOTTOM, 0).left(svB, Constraint.Property.Type.LEFT, 1);
                svA.constraints().top(svB, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textA.constraints().bottom(svA, Constraint.Property.Type.BOTTOM, 0).left(svA, Constraint.Property.Type.LEFT, 1);

                //Put these guys off screen
                svH.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textH.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                svS.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textS.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                svV.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textV.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                togEnchanted.constraints().top(this, Constraint.Property.Type.TOP, 8000);
            }
            else
            {
                svH.constraints().top(padding, Constraint.Property.Type.TOP, 1).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textH.constraints().bottom(svH, Constraint.Property.Type.BOTTOM, 0).left(svH, Constraint.Property.Type.LEFT, 1);
                svS.constraints().top(svH, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textS.constraints().bottom(svS, Constraint.Property.Type.BOTTOM, 0).left(svS, Constraint.Property.Type.LEFT, 1);
                svV.constraints().top(svS, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);
                textV.constraints().bottom(svV, Constraint.Property.Type.BOTTOM, 0).left(svV, Constraint.Property.Type.LEFT, 1);
                togEnchanted.constraints().top(svV, Constraint.Property.Type.BOTTOM, 4).right(padding, Constraint.Property.Type.LEFT, 26).left(this, Constraint.Property.Type.LEFT, edgePadding);

                //Put these guys off screen
                svR.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textR.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                svG.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textG.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                svB.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textB.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
                svA.constraints().top(this, Constraint.Property.Type.TOP, 8000);
                textA.constraints().bottom(this, Constraint.Property.Type.TOP, 8000);
            }

            this.resize(getWorkspace().getMinecraft(), this.width, this.height);
        }

        @Override
        public void tick()
        {
            super.tick();

            age++;
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
            float singProg = hatsListPadding * prog;
            float doubProg = (hatsListPadding * 2) * prog;

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
            int targetX = parentFragment.parent.windowHatsList.getCurrentView().list.getLeft() + hatsListPadding;
            int targetWidth = parentFragment.parent.windowHatsList.getCurrentView().list.width - hatsListPadding - hatsListPadding;

            float prog = 1F;
            if(age <= Hats.configClient.guiAnimationTime)
            {
                prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((age + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            }

            if(lastProg < 1F)
            {
                parentFragment.posX = (int)(parentFragment.parentElement.getLeft() + (targetX - parentFragment.parentElement.getLeft()) * prog);
                parentFragment.width = (int)(parentFragment.parentElement.width + (targetWidth - parentFragment.parentElement.width) * prog);
                parentFragment.resize(getWorkspace().getMinecraft(), parentFragment.width, parentFragment.height);
            }

            reset.posX = (int)(toggleHSBtoRGB.posX + (24 * (1F - prog)));

            renderTick = partialTick;
            stack.push();

            stack.translate(0F, 0F, 375F);

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();

            int hatViewWidth = parentFragment.parentElement.getWidth();
            int hatViewLeft = parentFragment.parentElement.getLeft();
            int targetElementX = parentFragment.parent.windowHatsList.getCurrentView().list.getRight() - hatsListPadding - parentFragment.parentElement.getMinWidth();
            int hatViewTop = parentFragment.parentElement.getTop();

            Fragment<?> fragment = parentFragment.parentElement.parentFragment;
            parentFragment.parentElement.parentFragment = this;

            parentFragment.parentElement.parentFragment.setScissor();

            //We're using RenderSystem instead of MatrixStack because of the entity render
            RenderSystem.translatef(0F, 0F, 40F);

            parentFragment.parentElement.setLeft((int)(hatViewLeft + (targetElementX - hatViewLeft) * prog));
            parentFragment.parentElement.setWidth((int)(parentFragment.parentElement.width + (parentFragment.parentElement.getMinWidth() - parentFragment.parentElement.width) * prog));
            parentFragment.parentElement.setTop(hatViewTop);

            parentFragment.parentElement.render(stack, mouseX, mouseY, partialTick);

            parentFragment.parentElement.parentFragment = fragment;
            parentFragment.parentElement.setTop(hatViewTop);
            parentFragment.parentElement.setWidth(hatViewWidth);
            parentFragment.parentElement.setLeft(hatViewLeft);

            resetScissorToParent();

            lastProg = prog;
        }

        @Override
        public void setScissor()
        {
            RenderHelper.startGlScissor(getLeft() - 21, getTop() - 6, width + 21 + 6, height + 12);
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
