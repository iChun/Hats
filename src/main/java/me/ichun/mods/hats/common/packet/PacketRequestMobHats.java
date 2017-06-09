package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

public class PacketRequestMobHats extends AbstractPacket
{
    public ArrayList<Integer> entIds;

    public PacketRequestMobHats(){}

    public PacketRequestMobHats(ArrayList<Integer> list)
    {
        entIds = new ArrayList<>(list);
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        for(Integer i : entIds)
        {
            buffer.writeInt(i);
        }
        buffer.writeInt(-2);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        entIds = new ArrayList<>();
        int id = buffer.readInt();
        while(id != -2)
        {
            entIds.add(id);
            id = buffer.readInt();
        }
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        for(Integer id : entIds)
        {
            Entity ent = player.worldObj.getEntityByID(id);
            if(ent instanceof EntityLivingBase)
            {
                String hatName = Hats.eventHandlerServer.mobHats.get(ent);
                if(hatName != null)
                {
                    ids.add(id);
                    names.add(hatName.trim());
                }
            }
        }
        ids.add(-2);

        return new PacketMobHatsList(ids, names);
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
