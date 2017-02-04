package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;

public class HelperGuardian extends RenderOnEntityHelper{
    @Override
    public Class helperForClass() {
        return HelperGuardian.class;
    }

    @Override
    public float getOffsetPointVert(EntityLivingBase ent)
    {
        return -4F/16F;
    }

    @Override
    public float getRotatePointVert(EntityLivingBase ent)
    {
        return 20F/16F;
    }

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        return 20F/16F;
    }
}
