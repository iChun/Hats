package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;

public class HelperShulker extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityShulker.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatShulker == 1;
	}

    @Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 0F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 12F/16F;
	}

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        EntityShulker shulker = (EntityShulker)ent;
        float peek = shulker.getClientPeekAmount(1F);
        if(peek <= 0.0F)
        {
            return 0F;
        }
        return 0.75F;
    }
}
