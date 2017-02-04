package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityMule;

public class HelperMule extends HelperHorse {
    @Override
    public Class helperForClass()
    {
        return EntityMule.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatMule == 1;
    }

}
