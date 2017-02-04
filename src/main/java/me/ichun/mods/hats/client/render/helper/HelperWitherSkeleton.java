package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.monster.EntityWitherSkeleton;

public class HelperWitherSkeleton extends HelperSkeleton{
    @Override
    public Class helperForClass()
    {
        return EntityWitherSkeleton.class;
    }
}
