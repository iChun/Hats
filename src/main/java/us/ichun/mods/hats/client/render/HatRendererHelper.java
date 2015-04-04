package us.ichun.mods.hats.client.render;

import us.ichun.mods.hats.client.core.ClientProxy;
import us.ichun.mods.hats.client.core.HatInfoClient;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.core.HatHandler;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelTabula;

@SideOnly(Side.CLIENT)
public class HatRendererHelper
{
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    //TODO GlStateManager
    public static void renderHat(HatInfoClient info, float alpha, float hatScale, float mobRenderScaleX, float mobRenderScaleY, float mobRenderScaleZ, float renderYawOffset, float rotationYaw, float rotationPitch, float rotationRoll, float rotatePointVert, float rotatePointHori, float rotatePointSide, float offsetVert, float offsetHori, float offsetSide, boolean isPlayer, boolean bindTexture, float renderTick)
    {
        if(info == null)//hat names are lower case
        {
            return;
        }
        ModelTabula model = ClientProxy.models.get(info.hatName);

        if(model != null)
        {
            if(Hats.proxy.tickHandlerClient.currentHatRenders >= Hats.config.maxHatRenders && !isPlayer)
            {
                return;
            }

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.001F);

            GL11.glPushMatrix();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glScalef(1.001F, 1.001F, 1.001F);
            GL11.glScalef(mobRenderScaleX, mobRenderScaleY, mobRenderScaleZ);

            GL11.glRotatef(renderYawOffset, 0.0F, -1.0F, 0.0F);

            GL11.glTranslatef(-rotatePointSide, 0.0F, rotatePointHori);

            GL11.glRotatef(renderYawOffset, 0.0F, 1.0F, 0.0F);

            GL11.glTranslatef(0.0F, rotatePointVert, 0.0F);

            GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);

            GL11.glRotatef(rotationYaw, 0.0F, -1.0F, 0.0F);
            GL11.glRotatef(rotationPitch, -1.0F, 0.0F, 0.0F);
            GL11.glRotatef(rotationRoll, 0.0F, 0.0F, 1.0F);

            GL11.glTranslatef(0.0F, 1F, 0.0F);

            GL11.glTranslatef(offsetSide, offsetVert, -offsetHori);

            GL11.glTranslatef(0.0F, -1F, 0.0F);
            GL11.glScalef(hatScale, hatScale, hatScale);
            GL11.glTranslatef(0.0F, 1F, 0.0F);

            if(bindTexture)
            {
                if(info.recolour > 0)
                {
                    float diffR = info.colourR - info.prevColourR;
                    float diffG = info.colourG - info.prevColourG;
                    float diffB = info.colourB - info.prevColourB;
                    float diffA = info.alpha - info.prevAlpha;

                    diffR *= (float)(info.recolour - renderTick) / 20F;
                    diffG *= (float)(info.recolour - renderTick) / 20F;
                    diffB *= (float)(info.recolour - renderTick) / 20F;
                    diffA *= (float)(info.recolour - renderTick) / 20F;

                    GL11.glColor4f((float)(info.colourR - diffR) / 255.0F, (float)(info.colourG - diffG) / 255.0F, (float)(info.colourB - diffB) / 255.0F, MathHelper.clamp_float(alpha * ((float)(info.alpha - diffA) / 255.0F), 0.0F, 1.0F));
                }
                else
                {
                    GL11.glColor4f((float)info.colourR / 255.0F, (float)info.colourG / 255.0F, (float)info.colourB / 255.0F, MathHelper.clamp_float(alpha * ((float)info.alpha / 255.0F), 0.0F, 1.0F));
                }
            }
            else
            {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, MathHelper.clamp_float(alpha, 0.0F, 1.0F));
            }

            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            model.render(0.0625F, bindTexture, true);

            GL11.glDisable(GL11.GL_BLEND);

            GL11.glPopMatrix();

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

            Hats.proxy.tickHandlerClient.currentHatRenders++;
        }
        else if(!HatHandler.reloadingHats)
        {
            if(!Hats.proxy.tickHandlerClient.requestedHats.contains(info.hatName))
            {
                HatHandler.requestHat(info.hatName, null);
                Hats.proxy.tickHandlerClient.requestedHats.add(info.hatName);
            }
        }
    }
}
