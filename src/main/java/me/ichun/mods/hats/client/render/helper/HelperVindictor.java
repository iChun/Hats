package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityVindicator;

public class HelperVindictor extends HelperVillager {
    @Override
    public Class helperForClass()
    {
        return EntityVindicator.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatVindictor == 1;
    }

}
