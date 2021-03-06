package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.client.gui.window.element.ElementHatsScrollView;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.hats.sort.SortHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.IWindows;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;

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
        isNotUnique();

        this.parentElement = parentElement;

        setView(new ViewSetAccessory(this));
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

    public static class ViewSetAccessory extends View<WindowSetAccessory>
    {
        public int age;
        public float renderTick = 0F;
        public float lastProg = 0F;

        public Window<?> windowDummy;

        public ElementHatsScrollView list;
        public HashSet<String> conflicts = new HashSet<>();

        public ArrayList<HatsSavedData.HatPart> hatParts;

        public ViewSetAccessory(@Nonnull WindowSetAccessory parent)
        {
            super(parent, "hats.gui.window.hat.personaliser");

            int padding = 0;

            hatParts = new ArrayList<>(parent.parentElement.hatLevel.hatParts);
            SortHandler.sort(Hats.configClient.filterSorters, hatParts, true);

            int maxHeight = parentFragment.parent.windowHatsList.height - (parentFragment.parent.windowHatsList.borderSize.get() * 2) - 12;
            int idealHeight = (hatParts.size() * 73) + 3; //(70 + 3 padding each) + end padding

            ElementScrollBar<?> sv = null;
            if(idealHeight > maxHeight) //needs a scroll bar
            {
                sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
                sv.constraints().top(this, Constraint.Property.Type.TOP, padding)
                        .bottom(this, Constraint.Property.Type.BOTTOM, padding) // 10 + 20 + 10, bottom + button height + padding
                        .right(this, Constraint.Property.Type.RIGHT, padding);
                elements.add(sv);
            }

            list = new ElementHatsScrollView(this);
            list.constraints().top(this, Constraint.Property.Type.TOP, padding + 1)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding + 1)
                    .left(this, Constraint.Property.Type.LEFT, padding + 1)
                    .right(this, Constraint.Property.Type.RIGHT, padding + 1);
            if(sv != null)
            {
                list.setScrollVertical(sv);
                list.constraints().right(sv, Constraint.Property.Type.LEFT, 2 + 1);
            }
            elements.add(list);

            for(int i = 0; i < hatParts.size(); i++)
            {
                HatsSavedData.HatPart level = hatParts.get(i);
                boolean isShowing = level.isShowing;

                HatInfo info = HatResourceHandler.getInfo(parent.parentElement.hatOrigin);
                HatInfo accessoryInfo = null;
                if(info != null)
                {
                    accessoryInfo = info.getInfoFor(level.name);
                    if(accessoryInfo != null)
                    {
                        if(isShowing)
                        {
                            conflicts.addAll(accessoryInfo.accessoryLayer);
                        }
                    }
                }

                final HatInfo accessoryInfoFinal = accessoryInfo;
                ElementHatRender<?> hat = new ElementHatRender<ElementHatRender<?>>(list, parent.parentElement.hatOrigin, level, btn -> {
                    if(btn.hatLevel.isNew)
                    {
                        btn.hatLevel.isNew = false;
                    }

                    btn.hatLevel.isShowing = btn.toggleState;
                    parent.parent.setNewHat(btn.hatOrigin.setModifier(btn.hatLevel), true);

                    if(accessoryInfoFinal != null && !accessoryInfoFinal.accessoryLayer.isEmpty())
                    {
                        if(btn.toggleState)
                        {
                            conflicts.addAll(accessoryInfoFinal.accessoryLayer);
                        }
                        else
                        {
                            conflicts.removeAll(accessoryInfoFinal.accessoryLayer);
                        }
                        updateConflicts();
                    }
                }
                ){
                    @Override
                    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
                    {
                        boolean isShowing = hatLevel.isShowing;
                        hatLevel.isShowing = true;
                        super.render(stack, mouseX, mouseY, partialTick);
                        hatLevel.isShowing = isShowing;
                    }

                    @Override
                    public boolean mouseReleased(double mouseX, double mouseY, int button) //we extended ElementRightClickable but we're not really much of that anymore
                    {
                        //        boolean flag = super.mouseReleased(mouseX, mouseY, button); // unsets dragging;
                        //copied out mouseReleased so we don't call ElementClickable's
                        this.setDragging(false);
                        boolean flag = getListener() != null && getListener().mouseReleased(mouseX, mouseY, button);

                        parentFragment.setListener(null); //we're a one time click, stop focusing on us
                        if(!(disabled || hasConflict) && isMouseOver(mouseX, mouseY))
                        {
                            if(button == 0 || button == 1 && !toggleState)
                            {
                                trigger();
                            }
                            if(button == 1 || isOverHamburger(mouseX, mouseY))
                            {
                                if(renderMinecraftStyle() > 0)
                                {
                                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                                }

                                spawnOptionsButtons();
                            }
                        }
                        return flag;
                    }

                    @Override
                    public void onClickRelease()
                    {
                        toggleState = !toggleState;
                    }
                };
                hat.setToggled(isShowing);
                hat.setSize(50, 70);
                list.addElement(hat);
            }

            if(list.elements.isEmpty())
            {
                ElementTextWrapper text = new ElementTextWrapper(this)
                {
                    @Override
                    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
                    {
                        stack.push();
                        stack.translate(0F, 0F, 375F);
                        super.render(stack, mouseX, mouseY, partialTick);
                        stack.pop();
                    }
                };
                text.setConstraint(Constraint.matchParent(text, this, 3));
                text.setText(I18n.format("hats.gui.window.hat.noHat"));
                elements.add(text);
            }

            updateConflicts();
        }

        public void updateConflicts()
        {
            for(Element<?> element : list.elements)
            {
                ElementHatRender<?> hat = (ElementHatRender<?>)element;
                if(!hat.toggleState) //not toggled
                {
                    HatInfo info = HatResourceHandler.getInfo(hat.hatOrigin.createCopy().setModifier(hat.hatLevel));
                    if(info != null)
                    {
                        HatInfo accessoryInfo = info.getInfoFor(hat.hatLevel.name);
                        if(accessoryInfo != null)
                        {
                            boolean hasConflict = false;
                            for(String s : accessoryInfo.accessoryLayer)
                            {
                                if(conflicts.contains(s))
                                {
                                    hasConflict = true;
                                    break;
                                }
                            }
                            hat.hasConflict = hasConflict;
                        }
                    }
                }
            }
        }

        @Override
        public void init()
        {
            super.init();

            if(windowDummy == null)
            {
                windowDummy = new Window<IWindows>(parentFragment.parent)
                {
                    @Override
                    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) //just be invisible and block things
                    {
                    }
                };

                windowDummy.disableBringToFront();
                windowDummy.disableDocking();
                windowDummy.disableDockStacking();
                windowDummy.disableUndocking();
                windowDummy.disableDrag();
                windowDummy.disableDragResize();
                windowDummy.disableTitle();
                windowDummy.isNotUnique();

                parentFragment.parent.addWindow(windowDummy);
                windowDummy.init();
            }
        }

        @Override
        public void onClose()
        {
            super.onClose();

            if(windowDummy != null)
            {
                parentFragment.parent.removeWindow(windowDummy);
            }
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
            int singProg = (int)(hatsListPadding * prog);
            int doubProg = (int)((hatsListPadding * 2) * prog);

            posX -= singProg;
            width += doubProg;
            posY -= singProg;
            height += doubProg;

            if(renderMinecraftStyle() > 0)
            {
                RenderSystem.enableAlphaTest();
                //draw the corners
                bindTexture(resourceTabs());

                RenderHelper.startDrawBatch();

                //fill space
                RenderHelper.drawBatch(stack, getLeft() + 4, getTop() + 4, width - 8, height - 8, 0, 4D/256D, 24D/256D, 36D/256D, 60D/256D); //fill space

                //draw borders
                RenderHelper.drawBatch(stack, getLeft(), getTop() + 4, 4, height - 8, 0, 0D/256D, 4D/256D, 36D/256D, 60D/256D); //left border
                RenderHelper.drawBatch(stack, getLeft() + 4, getTop(), width - 8, 4, 0, 4D/256D, 24D/256D, 32D/256D, 36D/256D); //top border
                RenderHelper.drawBatch(stack, getRight() - 4, getTop() + 4, 4, height - 8, 0, 24D/256D, 28D/256D, 36D/256D, 60D/256D); //right border
                RenderHelper.drawBatch(stack, getLeft() + 4, getBottom() - 4, width - 8, 4, 0, 4D/256D, 24D/256D, 124D/256D, 128D/256D); //bottom left

                //draw corners
                RenderHelper.drawBatch(stack, getLeft(), getTop(), 4, 4, 0, 0D/256D, 4D/256D, 32D/256D, 36D/256D); //top left
                RenderHelper.drawBatch(stack, getRight() - 4, getTop(), 4, 4, 0, 24D/256D, 28D/256D, 32D/256D, 36D/256D); //top right
                RenderHelper.drawBatch(stack, getLeft(), getBottom() - 4, 4, 4, 0, 0D/256D, 4D/256D, 124D/256D, 128D/256D); //bottom left
                RenderHelper.drawBatch(stack, getRight() - 4, getBottom() - 4, 4, 4, 0, 24D/256D, 28D/256D, 124D/256D, 128D/256D); //bottom left

                RenderHelper.endDrawBatch();
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
            int hatViewLeft = parentFragment.parentElement.getLeft();
            int targetElementX = parentFragment.parent.windowHatsList.getCurrentView().list.getLeft() + hatsListPadding;
            int hatViewTop = parentFragment.parentElement.getTop();

            parentFragment.parentElement.setLeft((int)(hatViewLeft + (targetElementX - hatViewLeft) * prog));
            parentFragment.parentElement.setWidth((int)(parentFragment.parentElement.width + (parentFragment.parentElement.getMinWidth() - parentFragment.parentElement.width) * prog));
            parentFragment.parentElement.setTop(hatViewTop);

            //This is in relation of the new parentElementPosition
            int maxHeight = parentFragment.parent.windowHatsList.height - (parentFragment.parent.windowHatsList.borderSize.get() * 4);
            int idealHeight = (hatParts.size() * 73) + 5; //70 + 3 padding each + 2x padding (2 each)
            int targetHeight = Math.min(idealHeight, maxHeight);

            int targetY = parentFragment.getTop();
            if(targetY + idealHeight > parentFragment.parent.windowHatsList.getBottom() - parentFragment.parent.windowHatsList.borderSize.get() * 2)
            {
                targetY -= (targetY + idealHeight) - (parentFragment.parent.windowHatsList.getBottom() - parentFragment.parent.windowHatsList.borderSize.get() * 2);

                if(targetY < parentFragment.parent.windowHatsList.getTop() + parentFragment.parent.windowHatsList.borderSize.get() * 2)
                {
                    targetY = parentFragment.parent.windowHatsList.getTop() + parentFragment.parent.windowHatsList.borderSize.get() * 2;
                }
            }
            int targetX = parentFragment.parentElement.getRight() + (hatsListPadding * 4);
            int targetWidth = 60 + (idealHeight > maxHeight ? 14 : 0); //50 width + 2x list padding and border (5 x 2) + padding to scroll (4) + scrolll (14) + 2x padding (6)

            if(lastProg < 1F)
            {
                parentFragment.setTop((int)(parentFragment.getTop() + (targetY - parentFragment.getTop()) * prog));
                parentFragment.posX = (int)(parentFragment.parentElement.getLeft() + (targetX - parentFragment.parentElement.getLeft()) * prog);
                parentFragment.width = (int)(parentFragment.parentElement.width + (targetWidth - parentFragment.parentElement.width) * prog);

                parentFragment.height = (int)(parentFragment.parentElement.height + (targetHeight - parentFragment.parentElement.height) * prog);
                parentFragment.resize(getWorkspace().getMinecraft(), parentFragment.width, parentFragment.height);
            }

            renderTick = partialTick;

            //We're using RenderSystem instead of MatrixStack because of the entity render
            RenderSystem.translatef(0F, 0F, 30F);
            stack.push();

            stack.translate(0F, 0F, 10F);

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();

            Fragment<?> fragment = parentFragment.parentElement.parentFragment;
            parentFragment.parentElement.parentFragment = this;

            parentFragment.parentElement.parentFragment.setScissor();

            parentFragment.parentElement.setLeft((int)(hatViewLeft + (targetElementX - hatViewLeft) * prog));
            parentFragment.parentElement.setTop(hatViewTop);

            //RENDER THE BACKGROUND FIRST
            int bgPadding = 6;
            int singProg = (int)(bgPadding * prog);
            int doubProg = (int)((bgPadding * 2) * prog);

            parentFragment.parentElement.posX -= singProg;
            parentFragment.parentElement.width += doubProg;
            parentFragment.parentElement.posY -= singProg;
            parentFragment.parentElement.height += doubProg;

            windowDummy.pos(parentFragment.parentElement.getLeft(), parentFragment.parentElement.getTop());
            windowDummy.size(parentFragment.parentElement.getWidth(), parentFragment.parentElement.getHeight());

            if(renderMinecraftStyle() > 0)
            {
                int width = parentFragment.parentElement.width;
                int height = parentFragment.parentElement.height;

                RenderSystem.enableAlphaTest();
                //draw the corners
                bindTexture(resourceTabs());

                //fill space
                RenderHelper.startDrawBatch();

                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getTop() + 4, width - 8, height - 8, 0, 4D/256D, 24D/256D, 36D/256D, 60D/256D); //fill space

                //draw borders
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getTop() + 4, 4, height - 8, 0, 0D/256D, 4D/256D, 36D/256D, 60D/256D); //left border
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getTop(), width - 8, 4, 0, 4D/256D, 24D/256D, 32D/256D, 36D/256D); //top border
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getTop() + 4, 4, height - 8, 0, 24D/256D, 28D/256D, 36D/256D, 60D/256D); //right border
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft() + 4, parentFragment.parentElement.getBottom() - 4, width - 8, 4, 0, 4D/256D, 24D/256D, 124D/256D, 128D/256D); //bottom left

                //draw corners
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getTop(), 4, 4, 0, 0D/256D, 4D/256D, 32D/256D, 36D/256D); //top left
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getTop(), 4, 4, 0, 24D/256D, 28D/256D, 32D/256D, 36D/256D); //top right
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getLeft(), parentFragment.parentElement.getBottom() - 4, 4, 4, 0, 0D/256D, 4D/256D, 124D/256D, 128D/256D); //bottom left
                RenderHelper.drawBatch(stack, parentFragment.parentElement.getRight() - 4, parentFragment.parentElement.getBottom() - 4, 4, 4, 0, 24D/256D, 28D/256D, 124D/256D, 128D/256D); //bottom left

                RenderHelper.endDrawBatch();
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
            //END RENDER THE BACKGROUND

            //We're using RenderSystem instead of MatrixStack because of the entity render
            RenderSystem.translatef(0F, 0F, 20F);

            parentFragment.parentElement.render(stack, mouseX, mouseY, partialTick);

            parentFragment.parentElement.parentFragment = fragment;
            parentFragment.parentElement.setWidth(hatViewWidth);
            parentFragment.parentElement.setLeft(hatViewLeft);
            parentFragment.parentElement.setTop(hatViewTop);

            resetScissorToParent();

            lastProg = prog;
        }

        @Override
        public void setScissor()
        {
            int hatRend = 50 + 12 + 3 + 6; //element + borders + padding + our border
            RenderHelper.startGlScissor(getLeft() - hatRend, getTop() - 6 - 70, width + hatRend + 6, height + 12 + 140);
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
