package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatInfo;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class PacketMobHatsList extends AbstractPacket
{
    public ArrayList<Integer> mobIds = new ArrayList<>();
    public ArrayList<String> hatNames = new ArrayList<>();

    public PacketMobHatsList(){}

    public PacketMobHatsList(ArrayList<Integer> ids, ArrayList<String> names)
    {
        mobIds = ids;
        hatNames = names;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        try
        {
            for(int i = 0; i < mobIds.size(); i++)
            {
                buffer.writeInt(mobIds.get(i));
                if(mobIds.get(i) != -2)
                {
                    ByteBufUtils.writeUTF8String(buffer, hatNames.get(i));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        buffer.writeInt(-2);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        int id = buffer.readInt();
        while(id != -2)
        {
            mobIds.add(id);
            hatNames.add(ByteBufUtils.readUTF8String(buffer));
            id = buffer.readInt();
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient(side, player);
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        for(int i = 0; i < Math.min(mobIds.size(), hatNames.size()); i++)
        {
            Entity ent = Minecraft.getMinecraft().world.getEntityByID(mobIds.get(i));
            if(ent != null && ent instanceof EntityLivingBase)
            {
                HatInfo hatInfo = new HatInfo(hatNames.get(i));
                EntityHat hat = new EntityHat(ent.world, (EntityLivingBase)ent, hatInfo);
                Hats.eventHandlerClient.mobHats.put(ent.getEntityId(), hat);
                ent.world.spawnEntity(hat);
            }
        }
    }
}
