package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityVex;

public class HelperVex extends HelperSkeleton {
    @Override
    public Class helperForClass()
    {
        return EntityVex.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatVex == 1;
    }
}
