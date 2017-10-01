package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRequestHat extends AbstractPacket
{
    public String hatName;

    public PacketRequestHat(){}

    public PacketRequestHat(String name)
    {
        this.hatName = name;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, hatName);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        hatName = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        HatHandler.sendHat(hatName, side.isServer() ? player : null);
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }
}
