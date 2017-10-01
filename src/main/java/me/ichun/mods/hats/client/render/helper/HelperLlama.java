package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HelperLlama extends HelperHorse
{

	@Override
	public Class helperForClass()
	{
		return EntityLlama.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatHorse == 1;
	}

	@Override
	public float getPrevRotationYaw(EntityLivingBase living)
	{
		return living.prevRenderYawOffset + 180F;
	}

	@Override
	public float getRotationYaw(EntityLivingBase living)
	{
		return living.renderYawOffset + 180F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getPrevRotationPitch(EntityLivingBase living)
	{
		return (float)Math.toDegrees(((AbstractHorse)living).getRearingAmount(1.0F) * ((float)Math.PI / 4F));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getRotationPitch(EntityLivingBase living)
	{
		return (float)Math.toDegrees(((AbstractHorse)living).getRearingAmount(1.0F) * ((float)Math.PI / 4F));
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 14F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return -3.5F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 7.9F/16F;
	}

	@Override
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 0.0F;
	}

	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		return 11.75F/8F;
	}


}
