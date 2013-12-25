package hats.client.render;

import hats.client.core.ClientProxy;
import hats.client.core.HatInfoClient;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HatRendererHelper 
{
	public static void renderHat(HatInfoClient info, float hatScale, float mobRenderScaleX, float mobRenderScaleY, float mobRenderScaleZ, float renderYawOffset, float rotationYaw, float rotationPitch, float rotatePointVert, float rotatePointHori, float offsetVert, float offsetHori, boolean isPlayer, float renderTick)
	{
    	ModelHat model = ClientProxy.models.get(info.hatName);
    	
    	if(model != null)
    	{
    		if(Hats.proxy.tickHandlerClient.currentHatRenders >= Hats.maxHatRenders && !isPlayer)
    		{
    			return;
    		}
    		
    		GL11.glPushMatrix();
    		
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glScalef(mobRenderScaleX, mobRenderScaleY, mobRenderScaleZ);

            GL11.glRotatef(renderYawOffset, 0.0F, -1.0F, 0.0F);
            
            GL11.glTranslatef(0.0F, 0.0F, rotatePointHori);
            
            GL11.glRotatef(renderYawOffset, 0.0F, 1.0F, 0.0F);
            
            GL11.glTranslatef(0.0F, rotatePointVert, 0.0F);

            GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
            
            GL11.glRotatef(rotationYaw, 0.0F, -1.0F, 0.0F);
            GL11.glRotatef(rotationPitch, -1.0F, 0.0F, 0.0F);

            GL11.glTranslatef(0.0F, 1F, 0.0F);
            
            GL11.glTranslatef(0.0F, offsetVert, -offsetHori);
            
            GL11.glTranslatef(0.0F, -1F, 0.0F);
            GL11.glScalef(hatScale, hatScale, hatScale);
            
	        if(info.recolour > 0)
	        {
	        	float diffR = info.colourR - info.prevColourR;
	        	float diffG = info.colourG - info.prevColourG;
	        	float diffB = info.colourB - info.prevColourB;
	        	
	        	diffR *= (float)(info.recolour - renderTick) / 20F;
	        	diffG *= (float)(info.recolour - renderTick) / 20F;
	        	diffB *= (float)(info.recolour - renderTick) / 20F;
	        	
	        	GL11.glColor4f((float)(info.colourR - diffR) / 255.0F, (float)(info.colourG - diffG) / 255.0F, (float)(info.colourB - diffB) / 255.0F, 1.0F);
	        }
	        else
	        {
	        	GL11.glColor4f((float)info.colourR / 255.0F, (float)info.colourG / 255.0F, (float)info.colourB / 255.0F, 1.0F);
	        }
	        
	        BufferedImage image = ClientProxy.bufferedImages.get(info.hatName);
	        
	        if (image != null)
	        {
	            if (ClientProxy.bufferedImageID.get(image) == -1)
	            {
	            	ClientProxy.bufferedImageID.put(image, TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), image));
	            }

	            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ClientProxy.bufferedImageID.get(image));
	        }

	        GL11.glScalef(-1.0F, -1.0F, 1.0F);
	        
	        model.render(0.0625F);

	        GL11.glDisable(GL11.GL_BLEND);
    		
    		GL11.glPopMatrix();
    		
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
	
	public static float getPrevRenderYaw(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish || living instanceof EntitySquid || living instanceof EntitySlime)
		{
			return living.prevRenderYawOffset;
		}
		return living.prevRenderYawOffset;
	}

	public static float getRenderYaw(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish || living instanceof EntitySquid || living instanceof EntitySlime)
		{
			return living.renderYawOffset;
		}
		return living.renderYawOffset;
	}
	
	public static float getPrevRotationYaw(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish || living instanceof EntitySquid || living instanceof EntitySlime)
		{
			return living.prevRenderYawOffset;
		}
		return living.prevRotationYawHead;
	}

	public static float getRotationYaw(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish || living instanceof EntitySquid || living instanceof EntitySlime)
		{
			return living.renderYawOffset;
		}
		return living.rotationYawHead;
	}
	
	public static float getPrevRotationPitch(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish)
		{
			return 0.0F;
		}
		else if(living instanceof EntitySquid)
		{
			return -((EntitySquid)living).prevSquidPitch;
		}
		return living.prevRotationPitch;
	}

	public static float getRotationPitch(EntityLivingBase living)
	{
		if(living instanceof EntityGhast || living instanceof EntitySilverfish)
		{
			return 0.0F;
		}
		else if(living instanceof EntitySquid)
		{
			return -((EntitySquid)living).squidPitch;
		}
		return living.rotationPitch;
	}

	public static float getRotatePointVert(EntityLivingBase ent)
	{
		if(ent instanceof EntityPlayer || ent instanceof EntityZombie || ent instanceof EntitySkeleton || ent instanceof EntityVillager)
		{
			return ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24F/16F ;
		}
		else if(ent instanceof EntityCreeper)
		{
			return 20.15F/16F;
		}
		else if(ent instanceof EntityEnderman)
		{
			return 37.1F/16F;
		}
		else if(ent instanceof EntityBlaze)
		{
			return 24F/16F;
		}
		else if(ent instanceof EntitySquid)
		{
			return 8F/16F;
		}
		else if(ent instanceof EntityPig)
		{
			return 12.35F/16F;
		}
		else if(ent instanceof EntitySpider)
		{
			return 9.2F/16F;
		}
		else if(ent instanceof EntitySheep)
		{
			return 18.2F/16F;
		}
		else if(ent instanceof EntityCow)
		{
			return 20.2F/16F;
		}
		else if(ent instanceof EntityChicken)
		{
			return 9F/16F;
		}
		else if(ent instanceof EntityGhast)
		{
			return 14.5F/16F;
		}
		else if(ent instanceof EntitySlime)
		{
			return 8F/16F;
		}
		return 0.0F;
	}
	
	public static float getRotatePointHori(EntityLivingBase ent)
	{
		if(ent instanceof EntityPig)
		{
			return 6F/16F;
		}
		else if(ent instanceof EntitySpider)
		{
			return 3F/16F;
		}
		else if(ent instanceof EntitySheep)
		{
			return 8F/16F;
		}
		else if(ent instanceof EntityCow)
		{
			return 8F/16F;
		}
		else if(ent instanceof EntityChicken)
		{
			return 4F/16F;
		}
		return 0.0F;
	}
	
	public static float getOffsetPointVert(EntityLivingBase ent)
	{
		if(ent instanceof EntityZombie)
		{
			return ((EntityZombie)ent).isVillager() ? 10F/16F : 8F/16F;
		}
		else if(ent instanceof EntityEnderman)
		{
			return ((EntityEnderman)ent).isScreaming() ? 13F/16F : 8F/16F;
		}
		else if(ent instanceof EntityPlayer || ent instanceof EntitySkeleton || ent instanceof EntityCreeper) //Normal Biped Head
		{
			return 8F/16F;
		}
		else if(ent instanceof EntityBlaze)
		{
			return 4F/16F;
		}
		else if(ent instanceof EntityVillager)
		{
			return 10F/16F;
		}
		else if(ent instanceof EntitySquid)
		{
			return 5F/16F;
		}
		else if(ent instanceof EntityPig)
		{
			return 4F/16F;
		}
		else if(ent instanceof EntitySpider)
		{
			return 3.8F/16F;
		}
		else if(ent instanceof EntitySheep || ent instanceof EntityCow)
		{
			return 4F/16F;
		}
		else if(ent instanceof EntityChicken)
		{
			return 6F/16F;
		}
		return 0.0F;
	}
	
	public static float getOffsetPointHori(EntityLivingBase ent)
	{
		if(ent instanceof EntityPig || ent instanceof EntitySpider)
		{
			return 4F/16F;
		}
		else if(ent instanceof EntitySheep)
		{
			return 3F/16F;
		}
		else if(ent instanceof EntityCow)
		{
			return 2F/16F;
		}
		return 0.0F;
	}
	
	public static float getHatScale(EntityLivingBase ent)
	{
		if(ent instanceof EntitySquid)
		{
			return 1.5F;
		}
		else if(ent instanceof EntitySheep)
		{
			if(((EntitySheep) ent).func_70894_j(1.0F) != 0.0F)
			{
				return 0.0F;
			}
			return 0.75F;
		}
		else if(ent instanceof EntityChicken)
		{
			return 0.5F;
		}
		else if(ent instanceof EntityGhast)
		{
			return 2.0F;
		}
		return 1.0F;
	}
	
    public static float interpolateRotation(float par1, float par2, float par3)
    {
        float f3;

        for (f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F)
        {
            ;
        }

        while (f3 >= 180.0F)
        {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }
	
	public static final String preRenderCallbackObf = "func_77041_b";
	public static final String preRenderCallbackDeobf = "preRenderCallback";
	
	public static void invokePreRenderCallback(Render rend, Class clz, EntityLivingBase ent, float rendTick)
	{
		try
		{
			Method m = clz.getDeclaredMethod(HatHandler.obfuscation ? preRenderCallbackObf : preRenderCallbackDeobf, EntityLivingBase.class, float.class);
			m.setAccessible(true);
			m.invoke(rend, ent, rendTick);
		}
		catch(NoSuchMethodException e)
		{
			if(clz != RendererLivingEntity.class)
			{
				invokePreRenderCallback(rend, clz.getSuperclass(), ent, rendTick);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
