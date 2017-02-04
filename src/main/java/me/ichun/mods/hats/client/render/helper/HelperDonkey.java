package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityDonkey;

public class HelperDonkey extends HelperHorse {

    @Override
    public Class helperForClass()
    {
        return EntityDonkey.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatDonkey == 1;
    }
}
