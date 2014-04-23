package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;

public class PacketPlayerHatSelection extends AbstractPacket
{

    public String hatName;
    public int r;
    public int g;
    public int b;

    public PacketPlayerHatSelection(){}

    public PacketPlayerHatSelection(String name, int R, int G, int B)
    {
        this.hatName = name;
        this.r = R;
        this.g = G;
        this.b = B;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        //should always be clientside
        ByteBufUtils.writeUTF8String(buffer, hatName);
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        //should always be serverside
        hatName = ByteBufUtils.readUTF8String(buffer);
        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();

        Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo(hatName, r, g, b));

        if(HatHandler.hasHat(hatName))
        {
            Hats.proxy.saveData(DimensionManager.getWorld(0));

            Hats.proxy.sendPlayerListOfWornHats(player, false);
        }
        else
        {
            HatHandler.requestHat(hatName, player);
        }
    }
}
