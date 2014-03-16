package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.Hats;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;
import ichun.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

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
    public void writeTo(ByteBuf buffer, Side side)
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
        }
        ByteBufUtils.writeUTF8String(buffer, "#endPacket");
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        String name = ByteBufUtils.readUTF8String(buffer);
        while(!name.equalsIgnoreCase("#endPacket"))
        {
            String hatName = ByteBufUtils.readUTF8String(buffer);
            int r = buffer.readInt();
            int g = buffer.readInt();
            int b = buffer.readInt();

            Hats.proxy.tickHandlerClient.playerWornHats.put(name, new HatInfo(hatName, r, g, b));

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
            }

            name = ByteBufUtils.readUTF8String(buffer);
        }

    }
}
