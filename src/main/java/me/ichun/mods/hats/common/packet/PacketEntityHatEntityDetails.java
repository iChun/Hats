package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketEntityHatEntityDetails extends AbstractPacket //Hat per entity, not actual hat entity details.
{
    public int entId;
    public CompoundNBT partInfo;

    public PacketEntityHatEntityDetails(){}

    public PacketEntityHatEntityDetails(int entId, CompoundNBT partInfo)
    {
        this.entId = entId;
        this.partInfo = partInfo;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entId);
        buf.writeCompoundTag(partInfo);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        entId = buf.readInt();
        partInfo = buf.readCompoundTag();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer()) // server
            {
                Entity entity = context.getSender().world.getEntityByID(entId);
                if(entity instanceof EntityHat)
                {
                    Hats.channel.sendTo(new PacketEntityHatEntityDetails(entId, ((EntityHat)entity).hatPart.write(new CompoundNBT())), context.getSender());
                }
            }
            else
            {
                executeClient();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void executeClient()
    {
        Entity ent = Minecraft.getInstance().world.getEntityByID(entId);
        if(ent instanceof EntityHat)
        {
            EntityHat hat = (EntityHat)ent;
            hat.hatPart.read(partInfo);
            hat.calculateNewHatSize();

            hat.rotFactorX += (hat.world.rand.nextFloat() * 2F - 1F) * 45F;
            hat.rotFactorY += (hat.world.rand.nextFloat() * 2F - 1F) * 45F;
        }
    }
}
