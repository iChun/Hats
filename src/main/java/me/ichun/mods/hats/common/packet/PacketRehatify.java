package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRehatify extends AbstractPacket //Hat per entity, not actual hat entity details.
{
    public int entId;

    public PacketRehatify(){}

    public PacketRehatify(int entId)
    {
        this.entId = entId;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entId);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        entId = buf.readInt();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(this::executeClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void executeClient()
    {
        Entity ent = Minecraft.getInstance().world.getEntityByID(entId);
        if(ent instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity)ent;
            EntityHelper.playSound(living, Hats.Sounds.POOF.get(), living.getSoundCategory(), 1.0F, 1F + (living.getRNG().nextFloat() * 2F - 1F) * 0.15F);

            for(int i = 0; i < 20; ++i) {
                double d0 = living.getRNG().nextGaussian() * 0.02D;
                double d1 = living.getRNG().nextGaussian() * 0.02D;
                double d2 = living.getRNG().nextGaussian() * 0.02D;
                living.getEntityWorld().addParticle(ParticleTypes.POOF, living.getPosXRandom(1.0D), living.getPosYRandom(), living.getPosZRandom(1.0D), d0, d1, d2);
            }

            for(int i = 0; i < 10; ++i) {
                double d0 = living.getRNG().nextGaussian() * 0.03D;
                double d1 = living.getRNG().nextGaussian() * 0.03D;
                double d2 = living.getRNG().nextGaussian() * 0.03D;
                living.getEntityWorld().addParticle(ParticleTypes.FIREWORK, living.getPosXRandom(1.0D), living.getPosYRandom(), living.getPosZRandom(1.0D), d0, d1, d2);
            }
        }
    }
}
