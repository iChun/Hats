package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;

public class PacketRequestEntityHatDetails extends AbstractPacket
{
    public Integer[] entIds;

    public PacketRequestEntityHatDetails(){}

    public PacketRequestEntityHatDetails(Integer[] entIds)
    {
        this.entIds = entIds;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entIds.length);
        for(int entId : entIds)
        {
            buf.writeInt(entId);
        }
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        entIds = new Integer[buf.readInt()];
        for(int i = 0; i < entIds.length; i++)
        {
            entIds[i] = buf.readInt();
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            HashMap<Integer, HatsSavedData.HatPart> entIdToHat = new HashMap<>();

            for(int entId : entIds)
            {
                Entity ent = context.getSender().getServerWorld().getEntityByID(entId);
                if(ent instanceof LivingEntity && ent.isAlive())
                {
                    HatsSavedData.HatPart hatPart = HatHandler.getHatPart((LivingEntity)ent);
                    if(hatPart.isAHat())
                    {
                        entIdToHat.put(entId, hatPart);
                    }
                }
            }

            Hats.channel.sendTo(new PacketEntityHatDetails(entIdToHat), context.getSender());
        });
    }
}
