package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatInfo;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;

import java.util.ArrayList;

public class PacketWornHatList extends AbstractPacket
{
    public ArrayList<String> playerNames;

    public PacketWornHatList(){}

    public PacketWornHatList(ArrayList<String> names)
    {
        playerNames = names;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        for(String s : playerNames)
        {
            HatInfo hat = Hats.proxy.playerWornHats.get(s);
            if(hat == null)
            {
                hat = new HatInfo();
            }
            ByteBufUtils.writeUTF8String(buffer, s);
            ByteBufUtils.writeUTF8String(buffer, hat.hatName);
            buffer.writeInt(hat.colourR);
            buffer.writeInt(hat.colourG);
            buffer.writeInt(hat.colourB);
            buffer.writeInt(hat.alpha);
        }
        ByteBufUtils.writeUTF8String(buffer, "#endPacket");
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        String name = ByteBufUtils.readUTF8String(buffer);
        while(!name.equalsIgnoreCase("#endPacket"))
        {
            String hatName = ByteBufUtils.readUTF8String(buffer);
            int r = buffer.readInt();
            int g = buffer.readInt();
            int b = buffer.readInt();
            int a = buffer.readInt();

            Hats.proxy.tickHandlerClient.playerWornHats.put(name, new HatInfo(hatName, r, g, b, a));

            EntityHat hat = Hats.proxy.tickHandlerClient.hats.get(name);
            if(hat != null)
            {
                if(hatName.equalsIgnoreCase(hat.hatName))
                {
                    hat.reColour = 20;
                }
                hat.hatName = hatName;
                hat.setR(r);
                hat.setG(g);
                hat.setB(b);
                hat.setA(a);
            }

            name = ByteBufUtils.readUTF8String(buffer);
        }
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player){
        return null;
    }//hacky fix

    @Override
    public Side receivingSide() {
        return null;
    }
}
