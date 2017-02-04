package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;

public class HelperPolarBear extends RenderOnEntityHelper {

    @Override
    public Class helperForClass() {
        return HelperPolarBear.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatPolarBear == 1;
    }

    public float getRotatePointVert(EntityLivingBase ent)
    {
        return 17/16F;
    }

    public float getRotatePointHori(EntityLivingBase ent)
    {
        return 16/16F;
    }

    public float getHatScale(EntityLivingBase ent)
    {
        return 10F/16F;
    }
}
