package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;

public class HelperZombieVillager extends RenderOnEntityHelper {
    @Override
    public Class helperForClass() {
        return EntityZombieVillager.class;
    }

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatZombie == 1;
    }

    @Override
    public float getRotatePointVert(EntityLivingBase ent)
    {
        return ent.isSneaking() ? ent == Minecraft.getMinecraft().player ? 23F/16F : 21F/16F : 24.15F/16F ;
    }

    @Override
    public float getOffsetPointVert(EntityLivingBase ent)
    {
        return 10/16F;
    }
}
