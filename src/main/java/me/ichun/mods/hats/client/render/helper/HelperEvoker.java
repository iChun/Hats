package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEvoker;

public class HelperEvoker extends HelperVillager {

    @Override
    public Class helperForClass()
    {
        return EntityEvoker.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatEvoker == 1;
    }
}
