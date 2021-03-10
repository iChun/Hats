package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowGreyout;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class WindowHalfGreyout extends WindowGreyout<WorkspaceHats>
{
    public WindowHalfGreyout(WorkspaceHats parent, Window<?> attached)
    {
        super(parent, attached);
    }

    @Override
    public void renderBackground(MatrixStack stack) //I know, ew.
    {
        //taken from AbstractGui.fillGradient
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f matrix = stack.getLast().getMatrix();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        //draw the original bits
        int z = parent.getBlitOffset();
        int colorA = 0xc0101010;
        int colorB = 0xd0101010;

        float x1 = parent.windowHatsList.getRight() - (int)(parent.windowHatsList.getWidth() / 2F);
        float x2 = 0;
        float y1 = width;
        float y2 = height;

        float f = (float)(colorA >> 24 & 255) / 255.0F;
        float f1 = (float)(colorA >> 16 & 255) / 255.0F;
        float f2 = (float)(colorA >> 8 & 255) / 255.0F;
        float f3 = (float)(colorA & 255) / 255.0F;
        float f4 = (float)(colorB >> 24 & 255) / 255.0F;
        float f5 = (float)(colorB >> 16 & 255) / 255.0F;
        float f6 = (float)(colorB >> 8 & 255) / 255.0F;
        float f7 = (float)(colorB & 255) / 255.0F;

        builder.pos(matrix, x2, y1, (float)z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, x1, y1, (float)z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, x1, y2, (float)z).color(f5, f6, f7, f4).endVertex();
        builder.pos(matrix, x2, y2, (float)z).color(f5, f6, f7, f4).endVertex();

        x1 = parent.windowHatsList.getLeft() - 20;
        x2 = parent.windowHatsList.getRight() - (int)(parent.windowHatsList.getWidth() / 2F);
        y1 = 0;
        y2 = height;

        f = (float)(colorA >> 24 & 255) / 255.0F;
        f1 = (float)(colorA >> 16 & 255) / 255.0F;
        f2 = (float)(colorA >> 8 & 255) / 255.0F;
        f3 = (float)(colorA & 255) / 255.0F;
        f4 = (float)(colorB >> 24 & 255) / 255.0F;
        f5 = (float)(colorB >> 16 & 255) / 255.0F;
        f6 = (float)(colorB >> 8 & 255) / 255.0F;
        f7 = (float)(colorB & 255) / 255.0F;
        builder.pos(matrix, x2, y1, (float)z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, x1, y1, (float)z).color(f1, f2, f3, 0).endVertex();
        builder.pos(matrix, x1, y2, (float)z).color(f5, f6, f7, 0).endVertex();
        builder.pos(matrix, x2, y2, (float)z).color(f5, f6, f7, f4).endVertex();

        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(stack);

        if(!parent.getEventListeners().contains(attachedWindow))
        {
            parent.removeWindow(this);
            if(closeConsumer != null)
            {
                closeConsumer.accept(this);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(mouseX > width / 2F && isMouseOver(mouseX, mouseY)) //only the second half of the screen
        {
            parent.removeWindow(attachedWindow);
            parent.removeWindow(this);
            return true;
        }
        return false;
    }
}
