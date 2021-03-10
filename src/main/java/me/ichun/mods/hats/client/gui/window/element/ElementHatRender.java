package me.ichun.mods.hats.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.WindowHatOptions;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementRightClickable;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ElementHatRender<T extends ElementHatRender>  extends ElementRightClickable<T>
{
    public static final String HAMBURGER = "\u2261";//"≡";

    public HatsSavedData.HatPart hatDetails;
    public boolean toggleState;

    public ElementHatRender(@Nonnull Fragment parent, HatsSavedData.HatPart hatDeets, Consumer<T> callback, Consumer<T> rmbCallback)
    {
        super(parent, callback, rmbCallback);
        this.hatDetails = hatDeets;
    }

    public <T extends ElementHatRender<?>> T setToggled(boolean flag)
    {
        toggleState = flag;
        return (T)this;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) //we extended ElementRightClickable but we're not really much of that anymore
    {
        boolean flag = super.mouseReleased(mouseX, mouseY, button); // unsets dragging;
        parentFragment.setListener(null); //we're a one time click, stop focusing on us
        if(!disabled && isMouseOver(mouseX, mouseY))
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
        return isMouseBetween(mouseX, getLeft(), getLeft() + 3 + getFontRenderer().getStringWidth(HAMBURGER) + 2) && isMouseBetween(mouseY, getTop(), getTop() + getFontRenderer().FONT_HEIGHT + 2);
    }

    public void spawnOptionsButtons()
    {
        //Spawn the window and set focus to it.
        WindowHatOptions windowHatOptions = new WindowHatOptions((WorkspaceHats)getWorkspace(), this);
        windowHatOptions.setPosX(getLeft() - 21);
        windowHatOptions.setPosY(getTop());
        windowHatOptions.setWidth(getWidth() + 21);
        windowHatOptions.setHeight(getHeight());
        if(windowHatOptions.getWorkspace().hasInit())
        {
            windowHatOptions.init();
        }
        windowHatOptions.getWorkspace().addWindow(windowHatOptions);
        windowHatOptions.getWorkspace().setListener(windowHatOptions);
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
    public void onRightClickRelease()
    {
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) //TODO disable hat render if no count.
    {
        super.render(stack, mouseX, mouseY, partialTick);
        if(renderMinecraftStyle())
        {
            renderMinecraftStyleButton(stack, getLeft(), getTop(), width, height, disabled || parentFragment.isDragging() && parentFragment.getListener() == this || toggleState ? ButtonState.CLICK : hover ? ButtonState.HOVER : ButtonState.IDLE);
        }
        else
        {
            fill(stack, getTheme().elementButtonBorder, 0);
            int[] colour;
            if(disabled)
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

        HatInfo info = HatResourceHandler.getAndSetAccessories(hatDetails.name);
        if(bottom - top > 0)
        {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            setScissor();

            RenderSystem.pushMatrix();
            RenderSystem.translatef(0F, 0F, 300F);

            LivingEntity livingEnt = ((WorkspaceHats)getWorkspace()).hatEntity;

            String originalHat = HatHandler.getHatDetails(livingEnt);

            HatHandler.assignSpecificHat(livingEnt, info != null ? info.name : "");

            InventoryScreen.drawEntityOnScreen(getLeft() + (getWidth() / 2), (int)(getBottom() + livingEnt.getEyeHeight() * 32F), Math.max(50 - (int)(livingEnt.getWidth() * 10), 10), 20, -10, livingEnt);

            HatHandler.assignSpecificHat(livingEnt, originalHat);

            RenderSystem.popMatrix();

            resetScissorToParent();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderHelper.drawColour(stack, 0, 0, 0, 80, getRight() - 10, getTop() + 1, 9, height - 2, 0);

        RenderSystem.disableBlend();

        String hatName = info != null ? info.getDisplayName() : "";

        float scale = 0.5F;
        String s = reString(hatName, (int)((height - 4) / scale));

        stack.push();
        stack.translate(getRight() - 5, getTop() + (height * scale), 0F);
        stack.rotate(Vector3f.ZP.rotationDegrees(-90F));
        stack.translate(-(height * scale) + 3, -(getFontRenderer().FONT_HEIGHT) * scale + 2, 375F);
        stack.scale(scale, scale, scale);

        //draw the text
        getFontRenderer().drawString(stack, s, 0, 0, renderMinecraftStyle() ? getMinecraftFontColour() : Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim));

        stack.pop();

        if(((WorkspaceHats)getWorkspace()).usePlayerInventory())
        {
            s = "x" + WorkspaceHats.FORMATTER.format(hatDetails.count);

            stack.push();
            stack.translate(getLeft() + 3, getBottom() - (getFontRenderer().FONT_HEIGHT) * scale - 1, 375F);
            stack.scale(scale, scale, scale);

            //draw the text
            getFontRenderer().drawString(stack, s, 0, 0, renderMinecraftStyle() ? getMinecraftFontColour() : Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim));

            stack.pop();
        }

        if(hover) //only if we're hovering
        {
            stack.push();

            stack.translate(0F, 0F, 375F);

            boolean isHoveringHamburger = isOverHamburger(mouseX, mouseY);

            getFontRenderer().drawString(stack, HAMBURGER, getLeft() + 3, getTop() + 2, renderMinecraftStyle() ? isHoveringHamburger ? 16777120 : 14737632 : Theme.getAsHex(isHoveringHamburger ? getTheme().font : getTheme().fontDim));

            stack.pop();
        }
    }

    @Override
    public void setScissor()
    {
        int top = Math.max(getTop() + 1, parentFragment.getTop());
        int bottom = Math.min(getBottom() - 1, parentFragment.getBottom());
        RenderHelper.startGlScissor(getLeft() + 1, top, width - 2, bottom - top);
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
