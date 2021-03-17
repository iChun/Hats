package me.ichun.mods.hats.common.entity;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketEntityHatEntityDetails;
import me.ichun.mods.hats.common.packet.PacketRehatify;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityHat extends Entity
{
    public @Nonnull HatsSavedData.HatPart hatPart;
    public UUID lastInteractedEntity;
    public boolean leftEntity;
    public int age = 0;

    public EntitySize hatSize = EntitySize.fixed(0.1F, 0.1F);
    public float[] hatDims = new float[] { 0F, 0F, 0F, 0F, 0F, 0F };

    public int knockback = 0;
    public boolean wasCollided = false;

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

    public void setKnockbackStrength(int i)
    {
        this.knockback = i;
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

    public void setThrowableHeading(double motionX, double motionY, double motionZ, float magnitude, float recoil)
    {
        float var9 = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= var9;
        motionY /= var9;
        motionZ /= var9;
        motionX += this.rand.nextGaussian() * 0.0075D * (double)recoil;
        motionY += this.rand.nextGaussian() * 0.0075D * (double)recoil;
        motionZ += this.rand.nextGaussian() * 0.0075D * (double)recoil;
        motionX *= magnitude;
        motionY *= magnitude;
        motionZ *= magnitude;
        setMotion(motionX, motionY, motionZ);
        float var10 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(motionY, var10) * 180.0D / Math.PI);
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
        if(age > Hats.configServer.hatEntityLifespan)
        {
            setDead();
            return;
        }

        if(ticksExisted == 1)
        {
            calculateNewHatSize();
        }

        //Remember the prevs
        prevPosX = getPosX();
        prevPosY = getPosY();
        prevPosZ = getPosZ();

        lastRotX = rotX;
        lastRotY = rotY;

        wasCollided = collidedHorizontally || collidedVertically;

        //taken from ItemEntity
        float f = this.getEyeHeight();
        if (this.isInWater() && this.func_233571_b_(FluidTags.WATER) > (double)f) {
            this.applyFloatMotion();
        } else if (this.isInLava() && this.func_233571_b_(FluidTags.LAVA) > (double)f) {
            this.applyLavaMotion();
        }

        Vector3d posEye = getPositionVec().add(0F, getEyeHeight(), 0F);
        if(!world.isRemote)
        {
            //Check to see if the origin entity is still colliding with us
            List<Entity> list = world.getEntitiesInAABBexcluding(this, getBoundingBox(), null);
            if(!leftEntity)
            {
                leftEntity = true;
                for(Entity entity : list)
                {
                    if(entity.getUniqueID().equals(lastInteractedEntity))
                    {
                        leftEntity = false;
                    }
                }
            }

            //Check for other colliding entities that we can use
            LivingEntity collidedEnt = null;
            for(Entity entity1 : list)
            {
                if(canPutOnHat(entity1))
                {
                    collidedEnt = (LivingEntity)entity1;
                    break;
                }
            }

            //No colliding entities, ray trace our motion
            if(collidedEnt != null)
            {
                EntityRayTraceResult entResult = ProjectileHelper.rayTraceEntities(this.world, this, posEye, posEye.add(getMotion()), this.getBoundingBox().expand(this.getMotion()).grow(1.0D), this::canPutOnHat);
                if(entResult != null)
                {
                    collidedEnt = (LivingEntity)entResult.getEntity(); //TODO knockback bonk
                }
            }

            //we found one!
            if(collidedEnt != null) //TODO the player-related configs.
            {
                LivingEntity collidedEntFinal = collidedEnt;
                HatsSavedData.HatPart hatPart = HatHandler.getHatPart(collidedEnt);
                HatsSavedData.HatPart oriHat = hatPart.createCopy();

                hatPart.copy(this.hatPart);
                HashMap<Integer, HatsSavedData.HatPart> entIdToHat = new HashMap<>();
                entIdToHat.put(collidedEnt.getEntityId(), hatPart);

                Hats.channel.sendTo(new PacketRehatify(collidedEnt.getEntityId()), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> collidedEntFinal));

                Hats.channel.sendTo(new PacketEntityHatDetails(entIdToHat), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> collidedEntFinal));

                if(!oriHat.name.isEmpty() && oriHat.count > 0)
                {
                    setLastInteracted(collidedEnt);

                    this.hatPart.copy(oriHat);
                    Hats.channel.sendTo(new PacketEntityHatEntityDetails(this.getEntityId(), this.hatPart.write(new CompoundNBT())), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this));

                    setMotion(new Vector3d(rand.nextGaussian() * 0.2F, 0.2F + rand.nextFloat() * 0.2F, rand.nextGaussian() * 0.2F));

                    ((ServerChunkProvider)getEntityWorld().getChunkProvider()).sendToAllTracking(this, new SEntityVelocityPacket(this));
                }
                else
                {
                    setDead();
                    return;
                }
            }
        }

        //Move entity
        this.move(MoverType.SELF, getMotion());

        if(!wasCollided && (collidedHorizontally || collidedVertically))
        {
            if(!world.isRemote)
            {
                EntityHelper.playSound(this, Hats.Sounds.BONK.get(), SoundCategory.AMBIENT, 0.5F, 0.85F + (rand.nextFloat() * 2F - 1F) * 0.075F);
            }
            else
            {
                rotFactorX += (rand.nextFloat() * 2F - 1F) * 45F;
                rotFactorY += (rand.nextFloat() * 2F - 1F) * 45F;
            }
        }

        setMotion(getMotion().add(0F, -0.02F, 0F));//gravity

        //air resistance the motion
        if(onGround)
        {
            setMotion(getMotion().mul(0.8D,0.8D,0.8D));
            rotFactorX *= 0.8F;
            rotFactorY *= 0.8F;
        }
        else
        {
            setMotion(getMotion().mul(0.98D,0.98D,0.98D));
            rotFactorX *= 0.98F;
            rotFactorY *= 0.98F;
        }

        rotX += rotFactorX;
        rotY += rotFactorY;
    }

    private boolean canPutOnHat(Entity e)
    {
        return e instanceof LivingEntity && HatHandler.canWearHat((LivingEntity)e) && !(e.getUniqueID().equals(lastInteractedEntity) && !leftEntity) && !(e instanceof PlayerEntity && !Hats.configServer.hatLauncherReplacesPlayerHat);
    }

    private void applyFloatMotion() {
        Vector3d vector3d = this.getMotion();
        this.setMotion(vector3d.x * (double)0.99F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.99F);
    }

    private void applyLavaMotion() {
        Vector3d vector3d = this.getMotion();
        this.setMotion(vector3d.x * (double)0.95F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.95F);
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
