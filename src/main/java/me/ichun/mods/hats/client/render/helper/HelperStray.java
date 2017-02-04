package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityStray;

public class HelperStray extends HelperSkeleton {
    @Override
    public Class helperForClass()
    {
        return EntityStray.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatStray== 1;
    }
}
