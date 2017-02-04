package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.passive.EntityZombieHorse;

public class HelperZombieHorse extends HelperHorse {

    @Override
    public Class helperForClass()
    {
        return EntityZombieHorse.class;
    }

}
