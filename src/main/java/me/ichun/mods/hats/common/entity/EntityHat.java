package me.ichun.mods.hats.common.entity;

import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EntityHat extends Entity
{
    public @Nonnull HatsSavedData.HatPart hatPart;
    public UUID lastInteractedEntity; //TODO save this
    public boolean leftEntity;
    public int age = 0;

    public EntitySize hatSize = EntitySize.fixed(0.1F, 0.1F);
    public float[] hatDims = new float[] { 0F, 0F, 0F };

    public float lastRotX;
    public float lastRotY;

    public float rotX;
    public float rotY;

    public float rotFactorX;
    public float rotFactorY;

    public EntityHat(EntityType<?> type, World world)
    {
        super(type, world);
        hatPart = new HatsSavedData.HatPart(); //uninitialised

        rotFactorX = (rand.nextFloat() * 2F - 1F) * 45F;
        rotFactorY = (rand.nextFloat() * 2F - 1F) * 45F;
    }

    public EntityHat setHatPart(HatsSavedData.HatPart part)
    {
        hatPart = part;
        calculateNewHatSize();
        return this;
    }

    public EntityHat setLastInteracted(LivingEntity living)
    {
        lastInteractedEntity = living.getUniqueID();
        leftEntity = false;
        age = 0;
        return this;
    }

    public void calculateNewHatSize()
    {
        HatInfo info = HatResourceHandler.getInfoAndSetToPart(hatPart);
        if(info != null)
        {
            hatDims = info.getDimensions();
            hatSize = EntitySize.fixed(Math.max(Math.abs(hatDims[1] - hatDims[0]), Math.abs(hatDims[5] - hatDims[4])) / 16F, Math.abs(hatDims[3] - hatDims[2]) / 16F); //y2 - y1;
        }
        else
        {
            hatSize = EntitySize.fixed(0F, 0F); // no hat info... set size to 0
        }
        recalculateSize();
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return hatSize;
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;
        if(age > 100)
        {
            setDead();
            return;
        }

        if(ticksExisted == 1)
        {
            calculateNewHatSize();
        }

        lastRotX = rotX;
        lastRotY = rotY;

        //Call it air resistance if you must
        rotFactorX *= 0.98F;
        rotFactorY *= 0.98F;

        rotX += rotFactorX;
        rotY += rotFactorY;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getBoundingBox().getAverageEdgeLength();
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight() * 2D; //render twice as far?
        return distance < d0 * d0;
    }

    @Override
    protected void registerData(){} //we don't use data watcher

    @Override
    protected void readAdditional(CompoundNBT tag)
    {
        hatPart = new HatsSavedData.HatPart();
        hatPart.read(tag.getCompound("hatPart"));
        lastInteractedEntity = tag.getUniqueId("lastInteractedEntity");
        leftEntity = tag.getBoolean("leftEntity");
        age = tag.getInt("age");
    }

    @Override
    protected void writeAdditional(CompoundNBT tag)
    {
        tag.put("hatPart", hatPart.write(new CompoundNBT()));
        tag.putUniqueId("lastInteractedEntity", lastInteractedEntity);
        tag.putBoolean("leftEntity", leftEntity);
        tag.putInt("age", age);
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
