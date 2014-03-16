package hats.common.packet;

import cpw.mods.fml.relauncher.Side;
import hats.common.Hats;
import ichun.core.network.AbstractPacket;
import ichun.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PacketRequestMobHats extends AbstractPacket
{
    public ArrayList<Integer> entIds;

    public PacketRequestMobHats(){}

    public PacketRequestMobHats(ArrayList<Integer> list)
    {
        entIds = list;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        for(Integer i : entIds)
        {
            buffer.writeInt(i);
        }
        buffer.writeInt(-2);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<String> names = new ArrayList<String>();

        int id = buffer.readInt();
        while(id != -2)
        {
            Entity ent = player.worldObj.getEntityByID(id);
            if(ent instanceof EntityLivingBase)
            {
                String hatName = Hats.proxy.tickHandlerServer.mobHats.get((EntityLivingBase)ent);
                if(hatName != null)
                {
                    ids.add(id);
                    names.add(hatName.trim());
                }
            }
            id = buffer.readInt();
        }
        ids.add(-2);

        PacketHandler.sendToPlayer(Hats.channels, new PacketMobHatsList(ids, names), player);
    }
}
