package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HelperWolf extends RenderOnEntityHelper
{

    @Override
    public Class helperForClass()
    {
        return EntityWolf.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatWolf == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getPrevRotationRoll(EntityLivingBase living)
    {
        return (float)Math.toDegrees(((EntityWolf)living).getInterestedAngle(1.0F) + ((EntityWolf)living).getShakeAngle(1.0F, 0.0F));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getRotationRoll(EntityLivingBase living)
    {
        return (float)Math.toDegrees(((EntityWolf)living).getInterestedAngle(1.0F) + ((EntityWolf)living).getShakeAngle(1.0F, 0.0F));
    }

    @Override
    public float getRotatePointVert(EntityLivingBase ent)
    {
        return 10.645F/16F;
    }

    @Override
    public float getRotatePointHori(EntityLivingBase ent)
    {
        return 7F/16F;
    }

    @Override
    public float getRotatePointSide(EntityLivingBase ent)
    {
        return 0F/16F;
    }

    @Override
    public float getOffsetPointVert(EntityLivingBase ent)
    {
        return 3F/16F;
    }

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return -1F/16F;
    }

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        return 6F/8F;
    }

}
