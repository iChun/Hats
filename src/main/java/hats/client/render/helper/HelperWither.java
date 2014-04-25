package hats.client.render.helper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.MathHelper;

public class HelperWither extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityWither.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.getInt("hatWither") == 1;
	}

	@SideOnly(Side.CLIENT)
	public float getPrevRotationYaw(EntityLivingBase living)
	{
		if(currentPass == 0)
		{
			return living.prevRotationYawHead;			
		}
		else
		{
			return ((EntityWither)living).func_82207_a(currentPass - 1);
		}
	}

	@SideOnly(Side.CLIENT)
	public float getRotationYaw(EntityLivingBase living)
	{
		if(currentPass == 0)
		{
			return living.rotationYawHead;
		}
		else
		{
			return ((EntityWither)living).func_82207_a(currentPass - 1);
		}
	}

	@SideOnly(Side.CLIENT)
	public float getPrevRotationPitch(EntityLivingBase living)
	{
		if(currentPass == 0)
		{
			return living.prevRotationPitch;
		}
		else
		{
			return ((EntityWither)living).func_82210_r(currentPass - 1);
		}
	}

	@SideOnly(Side.CLIENT)
	public float getRotationPitch(EntityLivingBase living)
	{
		if(currentPass == 0)
		{
			return living.rotationPitch;
		}
		else
		{
			return ((EntityWither)living).func_82210_r(currentPass - 1);
		}
	}

	public float getRotatePointVert(EntityLivingBase ent)
	{
		if(currentPass == 0)
		{
			return 24.13F/16F;
		}
		else
		{
			return 20.13F/16F;
		}
	}

	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 0.0F;
	}

	public float getRotatePointSide(EntityLivingBase ent)
	{
		if(currentPass == 0)
		{
			return 0.0F;
		}
		else if(currentPass == 1)
		{
			return 8F/16F;
		}
		else
		{
			return -10F/16F;
		}
	}

	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 4F/16F;
	}

	public float getOffsetPointHori(EntityLivingBase ent)
	{
		if(currentPass == 0)
		{
			return 0F;
		}
		else
		{
			return 1F/16F;
		}
	}
	
	public float getOffsetPointSide(EntityLivingBase ent)
	{
		if(currentPass == 0)
		{
			return 0.0F;
		}
		else if(currentPass == 1)
		{
			return 1F/16F;
		}
		else
		{
			return 1F/16F;
		}
	}

	public float getHatScale(EntityLivingBase ent)
	{
		if(currentPass == 0)
		{
			return 1.0F;
		}
		else
		{
			return 6F/8F;
		}
	}

	public int passesNeeded()
	{
		return 3;
	}
}
