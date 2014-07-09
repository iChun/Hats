package hats.client.render;

import hats.api.RenderOnEntityHelper;
import hats.client.gui.GuiHatSelection;
import hats.client.render.helper.HelperGeneric;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.entity.EntityHat;
import ichun.common.core.EntityHelperBase;
import ichun.common.core.util.ObfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class RenderHat extends Render 
{

	public RenderHat()
	{
		shadowSize = 0.0F;
	}

    public void renderHat(EntityHat hat, double par2, double par4, double par6, float par8, float par9)
    {
    	if(hat.info != null && !hat.info.hatName.equalsIgnoreCase("") && !hat.renderingParent.isPlayerSleeping() && hat.renderingParent.isEntityAlive() && !hat.renderingParent.isChild() && (Hats.config.getSessionInt("renderHats") == 1 || Hats.config.getSessionInt("renderHats") == 13131) && hat.render)
    	{
    		boolean firstPerson = (hat.parent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F));

    		if((Hats.config.getInt("renderInFirstPerson") == 1 && firstPerson || !firstPerson) && !hat.renderingParent.isInvisible())
    		{
    			boolean isPlayer = hat.parent instanceof EntityPlayer;
    			if(!isPlayer && Hats.proxy.tickHandlerClient.mobHats.get(hat.parent.getEntityId()) != hat)
    			{
    				hat.setDead();
    				return;
    			}
    			
    			RenderOnEntityHelper helper = HatHandler.getRenderHelper(hat.renderingParent.getClass());

                if(helper instanceof HelperGeneric)
                {
                    ((HelperGeneric)helper).update(hat.renderingParent);
                }

    			float alpha = 1.0F;
    			if(helper == null)
    			{
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
    			
	    		FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
	    		FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);

	    		GL11.glPushMatrix();
	    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
	    		Render rend = RenderManager.instance.getEntityRenderObject(hat.renderingParent);
	    		ObfHelper.invokePreRenderCallback(rend, rend.getClass(), hat.renderingParent, renderTick);
	    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
	    		GL11.glPopMatrix();

	    		float prevScaleX = buffer1.get(0) / buffer.get(0);
	    		float prevScaleY = buffer1.get(5) / buffer.get(5);
	    		float prevScaleZ = buffer1.get(8) / buffer.get(8);
    			
    			int passesNeeded = helper.passesNeeded();
    			
    			if(Hats.hasMorphMod && hat.parent instanceof EntityPlayer && Hats.config.getSessionInt("renderHats") != 13131)
    			{
    				EntityPlayer player = (EntityPlayer)hat.parent;
    				if(morph.api.Api.hasMorph(player.getCommandSenderName(), true) && morph.api.Api.morphProgress(player.getCommandSenderName(), true) < 1.0F)
    				{
    					float prog = MathHelper.clamp_float((((morph.api.Api.morphProgress(player.getCommandSenderName(), true) * 80F + renderTick) / 80F) - 1F/8F) / (6F/8F), 0.0F, 1.0F);
    					EntityLivingBase prevMorph = morph.api.Api.getPrevMorphEntity(player.getCommandSenderName(), true);
    					
    					if(prevMorph != null)
    					{
	    	    			RenderOnEntityHelper helper1 = HatHandler.getRenderHelper(prevMorph.getClass());
	    	    			if(helper1 != null)
	    	    			{
		    		    		FloatBuffer bufferr = GLAllocation.createDirectFloatBuffer(16);
		    		    		FloatBuffer bufferr1 = GLAllocation.createDirectFloatBuffer(16);
		
		    		    		GL11.glPushMatrix();
		    		    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bufferr);
		    		    		Render rend1 = RenderManager.instance.getEntityRenderObject(prevMorph);
                                ObfHelper.invokePreRenderCallback(rend1, rend1.getClass(), prevMorph, renderTick);
		    		    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bufferr1);
		    		    		GL11.glPopMatrix();
		
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
    			
	    		GL11.glPushMatrix();

    			if(isPlayer && hat.parent == Minecraft.getMinecraft().renderViewEntity && hat.parent.isSneaking())
    			{
    				GL11.glTranslatef(0.0F, -0.075F, 0.0F);
    			}
    			
		        GL11.glTranslated(par2, par4, par6);
	            GL11.glTranslatef(0.0F, -hat.parent.yOffset, 0.0F);

	            if(Hats.config.getSessionInt("renderHats") == 1)
	            {
		            GL11.glTranslatef(0.0F, (float)-(hat.lastTickPosY - hat.parent.lastTickPosY) + (float)((hat.parent.boundingBox.minY + hat.parent.yOffset) - (hat.parent.posY)), 0.0F);
		            int i = hat.renderingParent.getBrightnessForRender(renderTick);
		            int j = i % 65536;
		            int k = i / 65536;
		            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	            }
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            
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
	    			float renderYaw = EntityHelperBase.interpolateRotation(helper.getPrevRenderYaw(hat.renderingParent), helper.getRenderYaw(hat.renderingParent), renderTick);
	    			float rotationYaw = EntityHelperBase.interpolateRotation(helper.getPrevRotationYaw(hat.renderingParent), helper.getRotationYaw(hat.renderingParent), renderTick);
	    			float rotationPitch = EntityHelperBase.interpolateRotation(helper.getPrevRotationPitch(hat.renderingParent), helper.getRotationPitch(hat.renderingParent), renderTick);
	    			float rotationRoll = EntityHelperBase.interpolateRotation(helper.getPrevRotationRoll(hat.renderingParent), helper.getRotationRoll(hat.renderingParent), renderTick);
	    			float posVert = helper.getRotatePointVert(hat.renderingParent);
	    			float posHori = helper.getRotatePointHori(hat.renderingParent);
	    			float posSide = helper.getRotatePointSide(hat.renderingParent);
	    			float offVert = helper.getOffsetPointVert(hat.renderingParent);
	    			float offHori = helper.getOffsetPointHori(hat.renderingParent);
	    			float offSide = helper.getOffsetPointSide(hat.renderingParent);
	    			
	    			boolean renderHatSkin = true;
	    			boolean renderSkin = false;
	    			ResourceLocation skinLoc = AbstractClientPlayer.locationStevePng;
	    			float skinAlpha = alpha;
	    			
	    			if(Hats.hasMorphMod && hat.parent instanceof EntityPlayer && Hats.config.getSessionInt("renderHats") != 13131)
	    			{
	    				EntityPlayer player = (EntityPlayer)hat.parent;
	    				if(morph.api.Api.hasMorph(player.getCommandSenderName(), true) && morph.api.Api.morphProgress(player.getCommandSenderName(), true) < 1.0F)
	    				{
	    					float realProg = morph.api.Api.morphProgress(player.getCommandSenderName(), true);
	    					float prog = MathHelper.clamp_float((((realProg * 80F + renderTick) / 80F) - 1F/8F) / (6F/8F), 0.0F, 1.0F);
	    					EntityLivingBase prevMorph = morph.api.Api.getPrevMorphEntity(player.getCommandSenderName(), true);
	    					
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
			    	    			float arenderYaw = EntityHelperBase.interpolateRotation(helper1.getPrevRenderYaw(prevMorph), helper1.getRenderYaw(prevMorph), renderTick);
			    	    			float arotationYaw = EntityHelperBase.interpolateRotation(helper1.getPrevRotationYaw(prevMorph), helper1.getRotationYaw(prevMorph), renderTick);
			    	    			float arotationPitch = EntityHelperBase.interpolateRotation(helper1.getPrevRotationPitch(prevMorph), helper1.getRotationPitch(prevMorph), renderTick);
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
			    	    			skinLoc = morph.api.Api.getMorphSkinTexture();
			    	    			
			    	    			if(alpha == 1.0F)
			    	    			{
			    	    				if(realProg <= 1F/8F)
			    	    				{
			    	    					skinAlpha = MathHelper.clamp_float(((realProg * 80F + renderTick) / 80F) / (1F/8F), 0.0F, 1.0F);
			    	    				}
			    	    				else if(realProg > 7F/8F)
			    	    				{
			    	    					skinAlpha = MathHelper.clamp_float(1.0F - ((((realProg * 80F + renderTick) / 80F) - 7F/8F) / (1F/8F)), 0.0F, 1.0F);
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
				
				GL11.glPopMatrix();
    		}
    	}
    }
	
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderHat((EntityHat)par1Entity, par2, par4, par6, par8, par9);
    }
    
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) 
	{
		return AbstractClientPlayer.locationStevePng;
	}

}
