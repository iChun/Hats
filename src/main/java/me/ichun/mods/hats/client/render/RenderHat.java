package me.ichun.mods.hats.client.render;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.gui.GuiHatSelection;
import me.ichun.mods.hats.client.render.helper.HelperGeneric;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class RenderHat extends Render<EntityHat>
{

    public RenderHat(RenderManager manager)
    {
        super(manager);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntityHat hat, double par2, double par4, double par6, float par8, float par9)
    {
        if(hat.info != null && !hat.info.hatName.equalsIgnoreCase("") && !hat.renderingParent.isPlayerSleeping() && hat.renderingParent.isEntityAlive() && !hat.renderingParent.isChild() && (Hats.config.renderHats == 1 || Hats.config.renderHats == 13131) && hat.render)
        {
            boolean firstPerson = (hat.parent == Minecraft.getMinecraft().getRenderViewEntity() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && Minecraft.getMinecraft().getRenderManager().playerViewY == 180.0F));

            if((Hats.config.renderInFirstPerson == 1 && firstPerson || !firstPerson) && !hat.renderingParent.isInvisible())
            {
                boolean isPlayer = hat.parent instanceof EntityPlayer;
                if(!isPlayer && Hats.eventHandlerClient.mobHats.get(hat.parent.getEntityId()) != hat)
                {
                    hat.setDead();
                    return;
                }

                boolean noHelper = false;
                RenderOnEntityHelper helper = HatHandler.getRenderHelper(hat.renderingParent.getClass());

                if(helper instanceof HelperGeneric)
                {
                    ((HelperGeneric)helper).update(hat.renderingParent);
                }

                float alpha = 1.0F;
                if(helper == null)
                {
                    noHelper = true;
                    if(hat.parent instanceof EntityPlayer)
                    {
                        helper = HatHandler.getRenderHelper(EntityPlayer.class);
                        alpha = 0.0F;
                    }
                    else
                    {
                        return;
                    }
                }

                float renderTick = par9;
                helper.renderTick = renderTick;

                FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
                FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);

                GlStateManager.pushMatrix();
                GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
                Render rend = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(hat.renderingParent);
                ObfHelper.invokePreRenderCallback((RenderLivingBase)rend, rend.getClass(), hat.renderingParent, renderTick);
                GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
                GlStateManager.popMatrix();

                float prevScaleX = buffer1.get(0) / buffer.get(0);
                float prevScaleY = buffer1.get(5) / buffer.get(5);
                float prevScaleZ = buffer1.get(8) / buffer.get(8);

                int passesNeeded = helper.passesNeeded();

                if(iChunUtil.hasMorphMod() && hat.parent instanceof EntityPlayer && Hats.config.renderHats != 13131)
                {
                    EntityPlayer player = (EntityPlayer)hat.parent;
                    float morphProgress = MorphApi.getApiImpl().morphProgress(player.getName(), Side.CLIENT);
                    if(MorphApi.getApiImpl().hasMorph(player.getName(), Side.CLIENT) && morphProgress < 1.0F)
                    {
                        float prog = MathHelper.clamp((((morphProgress * MorphApi.getApiImpl().timeToCompleteMorph() + renderTick) / MorphApi.getApiImpl().timeToCompleteMorph()) - 1F/8F) / (6F/8F), 0.0F, 1.0F);
                        EntityLivingBase prevMorph = MorphApi.getApiImpl().getPrevMorphEntity(player.getEntityWorld(), player.getName(), Side.CLIENT);

                        if(prevMorph != null)
                        {
                            RenderOnEntityHelper helper1 = HatHandler.getRenderHelper(prevMorph.getClass());
                            if(helper1 != null)
                            {
                                FloatBuffer bufferr = GLAllocation.createDirectFloatBuffer(16);
                                FloatBuffer bufferr1 = GLAllocation.createDirectFloatBuffer(16);

                                GlStateManager.pushMatrix();
                                GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, bufferr);
                                Render rend1 = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(prevMorph);
                                ObfHelper.invokePreRenderCallback((RenderLivingBase)rend1, rend1.getClass(), prevMorph, renderTick);
                                GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, bufferr1);
                                GlStateManager.popMatrix();

                                float prevScaleeX = bufferr1.get(0) / bufferr.get(0);
                                float prevScaleeY = bufferr1.get(5) / bufferr.get(5);
                                float prevScaleeZ = bufferr1.get(8) / bufferr.get(8);

                                prevScaleX = prevScaleeX + (prevScaleX - prevScaleeX) * prog;
                                prevScaleY = prevScaleeY + (prevScaleY - prevScaleeY) * prog;
                                prevScaleZ = prevScaleeZ + (prevScaleZ - prevScaleeZ) * prog;

                                if(passesNeeded < helper1.passesNeeded())
                                {
                                    passesNeeded = helper1.passesNeeded();
                                }
                                if(alpha == 0.0F)
                                {
                                    alpha = 1.0F - prog;
                                }
                            }
                            else if(noHelper)
                            {
                                alpha = 0.0F;
                            }
                            else
                            {
                                alpha = alpha == 0.0F ? 1.0F - prog : prog;
                            }
                        }
                        else
                        {
                            alpha = alpha == 0.0F ? 1.0F - prog : prog;
                        }
                    }
                }

                if(alpha == 0.0F)
                {
                    return;
                }

                GlStateManager.pushMatrix();

                if(isPlayer && hat.parent == Minecraft.getMinecraft().getRenderViewEntity() && hat.parent.isSneaking())
                {
                    GlStateManager.translate(0.0F, -0.075F, 0.0F);
                }

                GlStateManager.translate(par2, par4, par6);
                //                GlStateManager.translate(0.0F, -hat.parent.yOffset, 0.0F);

                if(Hats.config.renderHats == 1)
                {
                    GlStateManager.translate(0.0F, (float)-(hat.lastTickPosY - hat.parent.lastTickPosY) + (float)((hat.parent.getEntityBoundingBox().minY/* + hat.parent.yOffset*/) - (hat.parent.posY)), 0.0F);
                    int i = hat.renderingParent.getBrightnessForRender();
                    int j = i % 65536;
                    int k = i / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
                }
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                for(int i = 0; i < passesNeeded; i++)
                {
                    if(i < helper.passesNeeded())
                    {
                        helper.currentPass = i;
                    }
                    else
                    {
                        helper.currentPass = 0;
                    }

                    float hatScale = helper.getHatScale(hat.renderingParent);
                    float renderYaw = EntityHelper.interpolateRotation(helper.getPrevRenderYaw(hat.renderingParent), helper.getRenderYaw(hat.renderingParent), renderTick);
                    float rotationYaw = EntityHelper.interpolateRotation(helper.getPrevRotationYaw(hat.renderingParent), helper.getRotationYaw(hat.renderingParent), renderTick);
                    float rotationPitch = EntityHelper.interpolateRotation(helper.getPrevRotationPitch(hat.renderingParent), helper.getRotationPitch(hat.renderingParent), renderTick);
                    float rotationRoll = EntityHelper.interpolateRotation(helper.getPrevRotationRoll(hat.renderingParent), helper.getRotationRoll(hat.renderingParent), renderTick);
                    float posVert = helper.getRotatePointVert(hat.renderingParent);
                    float posHori = helper.getRotatePointHori(hat.renderingParent);
                    float posSide = helper.getRotatePointSide(hat.renderingParent);
                    float offVert = helper.getOffsetPointVert(hat.renderingParent);
                    float offHori = helper.getOffsetPointHori(hat.renderingParent);
                    float offSide = helper.getOffsetPointSide(hat.renderingParent);

                    boolean renderHatSkin = true;
                    boolean renderSkin = false;
                    ResourceLocation skinLoc = DefaultPlayerSkin.getDefaultSkinLegacy();
                    float skinAlpha = alpha;

                    if(iChunUtil.hasMorphMod() && hat.parent instanceof EntityPlayer && Hats.config.renderHats != 13131)
                    {
                        EntityPlayer player = (EntityPlayer)hat.parent;

                        float realProg = MorphApi.getApiImpl().morphProgress(player.getName(), Side.CLIENT);
                        if(MorphApi.getApiImpl().hasMorph(player.getName(), Side.CLIENT) && realProg < 1.0F)
                        {
                            float prog = MathHelper.clamp((((realProg * MorphApi.getApiImpl().timeToCompleteMorph() + renderTick) / MorphApi.getApiImpl().timeToCompleteMorph()) - 1F/8F) / (6F/8F), 0.0F, 1.0F);
                            EntityLivingBase prevMorph = MorphApi.getApiImpl().getPrevMorphEntity(player.getEntityWorld(), player.getName(), Side.CLIENT);

                            if(prevMorph != null)
                            {
                                RenderOnEntityHelper helper1 = HatHandler.getRenderHelper(prevMorph.getClass());

                                if(helper1 != null)
                                {
                                    if(i < helper1.passesNeeded())
                                    {
                                        helper1.currentPass = i;
                                    }
                                    else
                                    {
                                        helper1.currentPass = 0;
                                    }

                                    float ahatScale = helper1.getHatScale(prevMorph);
                                    float arenderYaw = EntityHelper.interpolateRotation(helper1.getPrevRenderYaw(prevMorph), helper1.getRenderYaw(prevMorph), renderTick);
                                    float arotationYaw = EntityHelper.interpolateRotation(helper1.getPrevRotationYaw(prevMorph), helper1.getRotationYaw(prevMorph), renderTick);
                                    float arotationPitch = EntityHelper.interpolateRotation(helper1.getPrevRotationPitch(prevMorph), helper1.getRotationPitch(prevMorph), renderTick);
                                    float arotationRoll = helper1.getRotationRoll(prevMorph);
                                    float aposVert = helper1.getRotatePointVert(prevMorph);
                                    float aposHori = helper1.getRotatePointHori(prevMorph);
                                    float aposSide = helper1.getRotatePointSide(prevMorph);
                                    float aoffVert = helper1.getOffsetPointVert(prevMorph);
                                    float aoffHori = helper1.getOffsetPointHori(prevMorph);
                                    float aoffSide = helper1.getOffsetPointSide(prevMorph);

                                    hatScale = ahatScale + (hatScale - ahatScale) * prog;
                                    renderYaw = arenderYaw + (renderYaw - arenderYaw) * prog;
                                    rotationYaw = arotationYaw + (rotationYaw - arotationYaw) * prog;
                                    rotationPitch = arotationPitch + (rotationPitch - arotationPitch) * prog;
                                    rotationRoll = arotationRoll + (rotationRoll - arotationRoll) * prog;
                                    posVert = aposVert + (posVert - aposVert) * prog;
                                    posHori = aposHori + (posHori - aposHori) * prog;
                                    posSide = aposSide + (posSide - aposSide) * prog;
                                    offVert = aoffVert + (offVert - aoffVert) * prog;
                                    offHori = aoffHori + (offHori - aoffHori) * prog;
                                    offSide = aoffSide + (offSide - aoffSide) * prog;

                                    renderSkin = true;
                                    skinLoc = MorphApi.getApiImpl().getMorphSkinTexture();

                                    if(alpha == 1.0F)
                                    {
                                        if(realProg <= 1F/8F)
                                        {
                                            skinAlpha = MathHelper.clamp(((realProg * 80F + renderTick) / 80F) / (1F/8F), 0.0F, 1.0F);
                                        }
                                        else if(realProg > 7F/8F)
                                        {
                                            skinAlpha = MathHelper.clamp(1.0F - ((((realProg * 80F + renderTick) / 80F) - 7F/8F) / (1F/8F)), 0.0F, 1.0F);
                                        }
                                        else
                                        {
                                            renderHatSkin = false;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(renderHatSkin)
                    {
                        HatRendererHelper.renderHat(hat.info, alpha, hatScale, prevScaleX, prevScaleY, prevScaleZ, renderYaw, rotationYaw, rotationPitch, rotationRoll, posVert, posHori, posSide, offVert, offHori, offSide, isPlayer, true, renderTick);
                    }

                    if(renderSkin)
                    {
                        Minecraft.getMinecraft().getTextureManager().bindTexture(skinLoc);
                        HatRendererHelper.renderHat(hat.info, skinAlpha, hatScale, prevScaleX, prevScaleY, prevScaleZ, renderYaw, rotationYaw, rotationPitch, rotationRoll, posVert, posHori, posSide, offVert, offHori, offSide, isPlayer, false, renderTick);
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHat entity)
    {
        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    public static class RenderFactory implements IRenderFactory<EntityHat>
    {
        @Override
        public Render<EntityHat> createRenderFor(RenderManager manager)
        {
            return new RenderHat(manager);
        }
    }
}
