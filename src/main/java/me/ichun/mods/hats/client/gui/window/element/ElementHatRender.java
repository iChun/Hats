package me.ichun.mods.hats.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.WindowHatOptions;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementClickable;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ElementHatRender<T extends ElementHatRender>  extends ElementClickable<T>
{
    public static final String HAMBURGER = "\u2261";//"≡";

    public HatsSavedData.HatPart hatOrigin;
    public HatsSavedData.HatPart hatLevel;
    public boolean toggleState;

    public boolean hasConflict;
    public boolean isViewAllHats;

    public ElementHatRender(@Nonnull Fragment parent, HatsSavedData.HatPart hatOrigin, HatsSavedData.HatPart hatLevel, Consumer<T> callback, boolean isViewAllHats)
    {
        super(parent, callback);
        this.hatOrigin = hatOrigin;
        this.hatLevel = hatLevel;
        this.isViewAllHats = isViewAllHats;

        if(((WorkspaceHats)parent.getWorkspace()).usePlayerInventory() && this.hatLevel.count <= 0 && !isViewAllHats)
        {
            this.disabled = true;
        }

    }

    public ElementHatRender(@Nonnull Fragment parent, HatsSavedData.HatPart hatOrigin, HatsSavedData.HatPart hatLevel, Consumer<T> callback)
    {
        this(parent, hatOrigin, hatLevel, callback, false);
    }

    public <T extends ElementHatRender<?>> T setToggled(boolean flag)
    {
        toggleState = flag;
        return (T)this;
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
            if(button == 0 || button == 1)
            {
                trigger();
                if(button == 1 || isOverHamburger(mouseX, mouseY))
                {
                    spawnOptionsButtons();
                }
            }
        }
        return flag;
    }

    public boolean isOverHamburger(double mouseX, double mouseY)
    {
        return isMouseBetween(mouseX, getLeft(), getLeft() + 3 + getFontRenderer().getStringWidth(HAMBURGER) + 2) && isMouseBetween(mouseY, getTop(), getTop() + getFontRenderer().FONT_HEIGHT + 2) && !(disabled || hasConflict);
    }

    public void spawnOptionsButtons()
    {
        if(Screen.hasControlDown() && this.hatLevel.hasUnlockedAccessory())
        {
            WindowHatOptions.ViewHatOptions.openPersonalizer((WorkspaceHats)getWorkspace(), this);
        }
        else if(Screen.hasShiftDown())
        {
            WindowHatOptions.ViewHatOptions.openColouriser((WorkspaceHats)getWorkspace(), this);
        }
        else
        {
            //Spawn the window and set focus to it.
            WindowHatOptions windowHatOptions = new WindowHatOptions((WorkspaceHats)getWorkspace(), this);
            windowHatOptions.setPosX(getLeft() - 21);
            windowHatOptions.setPosY(getTop());
            windowHatOptions.setWidth(21);
            windowHatOptions.setHeight(getHeight());
            if(windowHatOptions.getWorkspace().hasInit())
            {
                windowHatOptions.init();
            }
            windowHatOptions.getWorkspace().addWindow(windowHatOptions);
            windowHatOptions.getWorkspace().setListener(windowHatOptions);
        }
    }

    @Override
    public void onClickRelease()
    {
        if(!toggleState)
        {
            toggleState = true;
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        super.render(stack, mouseX, mouseY, partialTick);
        if(renderMinecraftStyle() > 0)
        {
            renderMinecraftStyleButton(stack, getLeft(), getTop(), width, height, (disabled || hasConflict) || parentFragment.isDragging() && parentFragment.getListener() == this || toggleState ? ButtonState.CLICK : hover ? ButtonState.HOVER : ButtonState.IDLE, renderMinecraftStyle());
        }
        else
        {
            fill(stack, getTheme().elementButtonBorder, 0);
            int[] colour;
            if((disabled || hasConflict))
            {
                colour = getTheme().elementButtonBackgroundInactive;
            }
            else if(parentFragment.isDragging() && parentFragment.getListener() == this)
            {
                colour = getTheme().elementButtonClick;
            }
            else if(toggleState && hover)
            {
                colour = getTheme().elementButtonToggleHover;
            }
            else if(hover)
            {
                colour = getTheme().elementButtonBackgroundHover;
            }
            else if(toggleState)
            {
                colour = getTheme().elementButtonToggle;
            }
            else
            {
                colour = getTheme().elementButtonBackgroundInactive;
            }
            fill(stack, colour, 1);
        }

        int top = Math.max(getTop() + 1, parentFragment.getTop());
        int bottom = Math.min(getBottom() - 1, parentFragment.getBottom());

        HatsSavedData.HatPart partForRender = hatOrigin.createCopy().setNoNew().setNoFavourite().setModifier(hatLevel);

        if(bottom - top > 0)
        {
            int oriRenderCount = Hats.eventHandlerClient.renderCount;
            Hats.eventHandlerClient.renderCount = -1;

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            setScissor();

            RenderSystem.pushMatrix();
            RenderSystem.translatef(0F, 0F, 300F);

            LivingEntity livingEnt = ((WorkspaceHats)getWorkspace()).hatEntity;

            HatsSavedData.HatPart originalHat = HatHandler.getHatPart(livingEnt).createCopy();

            HatHandler.assignSpecificHat(livingEnt, partForRender);

            boolean isCrouching = false;
            ItemStack helm = livingEnt.getItemStackFromSlot(EquipmentSlotType.HEAD);
            ItemStack chest = livingEnt.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if(Hats.configClient.invisibleEntityInHatSelector || ((WorkspaceHats)getWorkspace()).hatLauncher != null)
            {
                Hats.eventHandlerClient.forceRenderWhenInvisible = true;
                livingEnt.setInvisible(true);
                if(((WorkspaceHats)getWorkspace()).hatLauncher != null)
                {
                    ((PlayerEntity)livingEnt).inventory.armorInventory.set(EquipmentSlotType.HEAD.getIndex(), ItemStack.EMPTY);
                    ((PlayerEntity)livingEnt).inventory.armorInventory.set(EquipmentSlotType.CHEST.getIndex(), ItemStack.EMPTY);
                }
                if(livingEnt instanceof ClientPlayerEntity)
                {
                    isCrouching = ((ClientPlayerEntity)livingEnt).isCrouching;
                    ((ClientPlayerEntity)livingEnt).isCrouching = false;
                }
            }

            InventoryScreen.drawEntityOnScreen(getLeft() + (getWidth() / 2), (int)(getBottom() + livingEnt.getEyeHeight() * 32F), Math.max(50 - (int)(livingEnt.getWidth() * 10), 10), 20, -10, livingEnt);

            if(Hats.configClient.invisibleEntityInHatSelector || ((WorkspaceHats)getWorkspace()).hatLauncher != null)
            {
                Hats.eventHandlerClient.forceRenderWhenInvisible = false;
                livingEnt.setInvisible(false);
                if(((WorkspaceHats)getWorkspace()).hatLauncher != null)
                {
                    ((PlayerEntity)livingEnt).inventory.armorInventory.set(EquipmentSlotType.HEAD.getIndex(), helm);
                    ((PlayerEntity)livingEnt).inventory.armorInventory.set(EquipmentSlotType.CHEST.getIndex(), chest);
                }
                if(livingEnt instanceof ClientPlayerEntity)
                {
                    ((ClientPlayerEntity)livingEnt).isCrouching = isCrouching;
                }
            }

            HatHandler.assignSpecificHat(livingEnt, originalHat);

            RenderSystem.popMatrix();

            resetScissorToParent();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();

            Hats.eventHandlerClient.renderCount = oriRenderCount;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if(hasConflict)
        {
            RenderHelper.drawColour(stack, 255, 0, 0, 30, getLeft() + 1, getTop() + 1, width - 2, height - 2, 0);
        }

        if(!isViewAllHats && ((WorkspaceHats)getWorkspace()).usePlayerInventory() && hatLevel.count <= 0 || isViewAllHats && hatLevel.count <= 0 && hatLevel.hsbiser[2] == 1F)
        {
            RenderHelper.drawColour(stack, 0, 0, 0, 120, getLeft() + 1, getTop() + 1, width - 2, height - 2, 0); //greyout
        }

        RenderHelper.drawColour(stack, 0, 0, 0, 150, getRight() - 10, getTop() + 1, 9, height - 2, 0);

        HatInfo info = HatResourceHandler.getInfo(partForRender);
        String hatName = info != null ? info.getDisplayNameFor(hatLevel.name) : "";

        int topDist = height - 6;
        if(parentFragment instanceof ElementHatsScrollView && !isViewAllHats)
        {
            int renderIconX = getRight() - 9;
            if(partForRender.hasFavourite())
            {
                topDist = height - 14;

                if(!hatLevel.isFavourite)
                {
                    RenderHelper.colour(0x00ffff);
                }
                RenderHelper.drawTexture(stack, WindowHatOptions.ViewHatOptions.TEX_FAVOURITE, renderIconX, getTop() + 2, 7, 7, 0);
                RenderHelper.colour(0xffffff); //reset the colour

                renderIconX -= 10;
            }
            if(partForRender.hasNew())
            {
                topDist = height - 14;

                stack.push();

                stack.translate(0F, 0F, 375F);

                getFontRenderer().drawString(stack, "!", renderIconX + 3, getTop() + 2, renderMinecraftStyle() > 0 ? 16777120 : Theme.getAsHex(getTheme().font));

                stack.pop();
            }
        }

        float scale = 0.5F;
        String s = reString(hatName, (int)((topDist) / scale));

        stack.push();
        stack.translate(getRight() - 5, getTop() + (height * scale), 0F);
        stack.rotate(Vector3f.ZP.rotationDegrees(-90F));
        stack.translate(-(height * scale) + 3, -(getFontRenderer().FONT_HEIGHT) * scale + 2, 375F);
        stack.scale(scale, scale, scale);

        //draw the text
        getFontRenderer().drawString(stack, s, 0, 0, renderMinecraftStyle() > 0 ? getMinecraftFontColour() : Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim));

        stack.pop();

        if(!isViewAllHats)
        {
            if(((WorkspaceHats)getWorkspace()).usePlayerInventory())
            {
                s = "x" + WorkspaceHats.FORMATTER.format(hatLevel.count); // we count from the level

                stack.push();
                stack.translate(getLeft() + 3, getBottom() - (getFontRenderer().FONT_HEIGHT) * scale - 1, 375F);
                stack.scale(scale, scale, scale);

                //draw the text
                int clr = renderMinecraftStyle() > 0 ? getMinecraftFontColour() : Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim);
                if(hatLevel.count <= 0)
                {
                    clr = 0xaa0000;
                }
                getFontRenderer().drawString(stack, s, 0, 0, clr);

                stack.pop();
            }

            if(parentFragment instanceof ElementHatsScrollView && !(disabled || hasConflict))
            {
                if(hover) //only if we're hovering
                {
                    stack.push();

                    stack.translate(0F, 0F, 375F);

                    boolean isHoveringHamburger = isOverHamburger(mouseX, mouseY);

                    getFontRenderer().drawString(stack, HAMBURGER, getLeft() + 3, getTop() + 2, renderMinecraftStyle() > 0 ? isHoveringHamburger ? 16777120 : 14737632 : Theme.getAsHex(isHoveringHamburger ? getTheme().font : getTheme().fontDim));

                    stack.pop();
                }
                else if(hatLevel.hasUnlockedAccessory())
                {
                    stack.push();

                    stack.translate(0F, 0F, 375F);

                    getFontRenderer().drawString(stack, "+", getLeft() + 3, getTop() + 2, renderMinecraftStyle() > 0 ? 14737632 : Theme.getAsHex(getTheme().fontDim));

                    stack.pop();
                }
            }
        }
    }

    @Nullable
    @Override
    public String tooltip(double mouseX, double mouseY)
    {
        HatsSavedData.HatPart partForRender = hatOrigin.createCopy().setNoNew().setNoFavourite().setModifier(hatLevel);
        HatInfo info = HatResourceHandler.getInfo(partForRender);
        if(info != null)
        {
            HatInfo accessoryInfo = info.getInfoFor(hatLevel.name);
            if(accessoryInfo != null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(accessoryInfo.getDisplayName()).append("\n");
                if(!accessoryInfo.project.author.isEmpty())
                {
                    sb.append(I18n.format("hats.gui.tooltip.author", accessoryInfo.project.author)).append("\n");
                }
                sb.append(I18n.format("hats.gui.tooltip.rarity", accessoryInfo.getRarity().getColour().toString() + accessoryInfo.getRarity().toString())).append("\n");
                if(hatLevel.count == 0 && hatLevel.hsbiser[2] == 1F)
                {
                    sb.append(I18n.format("hats.gui.tooltip.notUnlocked")).append("\n");
                }
                sb.append("\n");
                if(accessoryInfo.description != null)
                {
                    sb.append(accessoryInfo.description).append("\n").append("\n");
                }
                if(Hats.eventHandlerClient.serverHasMod)
                {
                    sb.append(I18n.format("hats.gui.tooltip.worth", info.getWorthFor(accessoryInfo.name, 0)));
                }

                return sb.toString();
            }
        }

        return super.tooltip(mouseX, mouseY);
    }

    @Override
    public void setScissor()
    {
        if(parentFragment instanceof ElementHatsScrollView)
        {
            int top = Math.max(getTop() + 1, parentFragment.getTop());
            int bottom = Math.min(getBottom() - 1, parentFragment.getBottom());
            RenderHelper.startGlScissor(getLeft() + 1, top, width - 2, bottom - top);
        }
        else
        {
            RenderHelper.startGlScissor(getLeft() + 1, getTop() + 1, width - 2, height - 2);
        }
    }

    @Override
    public boolean requireScissor()
    {
        return true;
    }

    @Override
    public int getMinWidth()
    {
        return 50;
    }

    @Override
    public int getMinHeight()
    {
        return 70;
    }
}
