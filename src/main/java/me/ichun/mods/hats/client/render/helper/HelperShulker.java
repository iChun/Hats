package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;

public class HelperShulker extends RenderOnEntityHelper
{
    @Override
    public Class helperForClass()
    {
        return EntityShulker.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatShulker == 1;
    }

    @Override
    public float getOffsetPointVert(EntityLivingBase ent)
    {
        return ((EntityShulker) ent).getPeekTick()==0 ? 1F : 20F/16F ;
    }

    public float getHatScale(EntityLivingBase ent)
    {
        return 1F;
    }
}
