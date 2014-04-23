package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.core.HatHandler;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketRequestHat extends AbstractPacket
{
    public String hatName;

    public PacketRequestHat(){}

    public PacketRequestHat(String name)
    {
        this.hatName = name;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, hatName);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        hatName = ByteBufUtils.readUTF8String(buffer);

        HatHandler.sendHat(hatName, side.isServer() ? player : null);
    }
}
