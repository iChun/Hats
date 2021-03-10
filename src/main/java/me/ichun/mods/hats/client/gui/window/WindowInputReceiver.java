package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class WindowInputReceiver extends Window<WorkspaceHats>
{
    //headingFIELD is the direction we're heading to. lastFIELD and FIELD is just for rendering.
    public float lastX, x, headingX;
    public float lastY, y, headingY;
    public float lastDriftYaw, driftYaw, headingDriftYaw;
    public float lastDriftPitch, driftPitch, headingDriftPitch;
    public float lastCamDist, camDist, headingCamDist;

    public WindowInputReceiver(WorkspaceHats parent)
    {
        super(parent);
        size(parent.getWidth(), parent.getHeight());
        setConstraint(Constraint.matchParent(this, parent, 0));
        borderSize = () -> 0;
        titleSize = () -> 0;

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableDragResize();
        disableTitle();
    }

    @Override
    public void init()
    {
        constraint.apply();
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        constraint.apply();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(!parent.fallback)
        {
            Hats.eventHandlerClient.guiX = lastX + (x - lastX) * partialTick;
            Hats.eventHandlerClient.guiY = lastY + (y - lastY) * partialTick;
            Hats.eventHandlerClient.guiYaw = lastDriftYaw + (driftYaw - lastDriftYaw) * partialTick;
            Hats.eventHandlerClient.guiPitch = lastDriftPitch + (driftPitch - lastDriftPitch) * partialTick;
            Hats.eventHandlerClient.guiDist = lastCamDist + (camDist - lastCamDist) * partialTick;
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        correct();

        lastX = x;
        lastY = y;
        lastDriftYaw = driftYaw;
        lastDriftPitch = driftPitch;
        lastCamDist = camDist;

        float mag = 0.4F;
        x += (headingX - x) * mag;
        y += (headingY - y) * mag;
        driftYaw += (headingDriftYaw - driftYaw) * mag;
        driftPitch += (headingDriftPitch - driftPitch) * mag;
        camDist += (headingCamDist - camDist) * mag;
    }

    public void correct()
    {
        driftPitch = driftPitch % 360F;
        driftYaw = driftYaw % 360F;

        if(parent.fallback)
        {
            if(headingCamDist < -5F)
            {
                headingCamDist = -5F;
            }
            else if(headingCamDist > 1F)
            {
                headingCamDist = 1F;
            }
        }
        else
        {
            if(headingCamDist < -1.2F)
            {
                headingCamDist = -1.2F;
            }
            else if(headingCamDist > 4F)
            {
                headingCamDist = 4F;
            }
        }

        if(headingX < -4F)
        {
            headingX = -4F;
        }
        else if(headingX > 4F)
        {
            headingX = 4F;
        }

        if(headingY < -4F)
        {
            headingY = -4F;
        }
        else if(headingY > 4F)
        {
            headingY = 4F;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isMouseOver(mouseX, mouseY) && mouseX < getWidth() / 2D) //Only on the left side of the screen
        {
            parent.setDragging(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        super.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasShiftDown()) // if middle mouse or holding shift
            {
                float mag = 0.0125F;
                headingX -= distX * mag;
                headingY += distY * mag;
            }
            else if(Screen.hasControlDown())
            {
                headingCamDist += (distX - distY) * 0.05D;
            }
            else
            {
                float mag = 0.5F;
                headingDriftYaw += distX * mag;
                headingDriftPitch += distY * mag;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            headingCamDist -= amount * 0.05D;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }
}
