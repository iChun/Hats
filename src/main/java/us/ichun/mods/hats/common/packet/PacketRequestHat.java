package us.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

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
    public void readFrom(ByteBuf buffer, Side side)
    {
        hatName = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        HatHandler.sendHat(hatName, side.isServer() ? player : null);
    }
}
