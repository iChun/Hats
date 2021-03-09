package me.ichun.mods.hats.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementRightClickable;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class ElementHatRender<T extends ElementHatRender>  extends ElementRightClickable<T>
{
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");

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
        if(toggleState)
        {
            toggleState = false;
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        super.render(stack, mouseX, mouseY, partialTick);
        if(renderMinecraftStyle())
        {
            renderMinecraftStyleButton(stack, getLeft(), getTop(), width, height, parentFragment.isDragging() && parentFragment.getListener() == this || toggleState ? ButtonState.CLICK : hover ? ButtonState.HOVER : ButtonState.IDLE);
        }
        else
        {
            fill(stack, getTheme().elementButtonBorder, 0);
            int[] colour;
            if(parentFragment.isDragging() && parentFragment.getListener() == this)
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

            LivingEntity livingEnt = ((WorkspaceHats)getWorkspace()).hatEntity;

            String originalHat = HatHandler.getHatDetails(livingEnt);

            HatHandler.assignSpecificHat(livingEnt, info != null ? info.name : "");

            InventoryScreen.drawEntityOnScreen(getLeft() + (getWidth() / 2), (int)(getBottom() + livingEnt.getEyeHeight() * 32F), Math.max(50 - (int)(livingEnt.getWidth() * 10), 10), 20, -10, livingEnt);

            HatHandler.assignSpecificHat(livingEnt, originalHat);

            resetScissorToParent();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderHelper.drawColour(stack, 0, 0, 0, 80, getRight() - 10, getTop(), 10, height, 0);

        RenderSystem.disableBlend();

        String hatName = info != null ? info.getDisplayName() : "";

        float scale = 0.5F;
        String s = reString(hatName, (int)((height - 4) / scale));

        stack.push();
        stack.translate(getRight() - 5, getTop() + (height * scale), 0F);
        stack.rotate(Vector3f.ZP.rotationDegrees(-90F));
        stack.translate(-(height * scale) + 3, -(getFontRenderer().FONT_HEIGHT) * scale + 2, 100F);
        stack.scale(scale, scale, scale);

        //draw the text
        if(renderMinecraftStyle())
        {
            getFontRenderer().drawString(stack, s, 0, 0, getMinecraftFontColour());
        }
        else
        {
            getFontRenderer().drawString(stack, s, 0, 0, Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim));
        }

        stack.pop();

        if(!(Minecraft.getInstance().player.isCreative() && !Hats.configServer.enableCreativeModeHadHunting)) //doesn't use an inventory
        {
            s = "x" + FORMATTER.format(hatDetails.count);

            stack.push();
            stack.translate(getLeft() + 3, getBottom() - (getFontRenderer().FONT_HEIGHT) * scale - 1, 100F);
            stack.scale(scale, scale, scale);

            //draw the text
            if(renderMinecraftStyle())
            {
                getFontRenderer().drawString(stack, s, 0, 0, getMinecraftFontColour());
            }
            else
            {
                getFontRenderer().drawString(stack, s, 0, 0, Theme.getAsHex(toggleState ? getTheme().font : getTheme().fontDim));
            }

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
