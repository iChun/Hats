package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.passive.EntitySkeletonHorse;

public class HelperSkeletonHorse extends HelperHorse{

    @Override
    public Class helperForClass()
    {
        return EntitySkeletonHorse.class;
    }

}
