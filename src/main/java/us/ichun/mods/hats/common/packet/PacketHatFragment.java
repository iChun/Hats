package us.ichun.mods.hats.common.packet;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketHatFragment extends AbstractPacket
{
    public String hatName;
    public byte packetTotal;
    public byte packetNumber;
    public int fileSize;
    public byte[] data;

    public PacketHatFragment(){}

    public PacketHatFragment(String name, int pktTotal, int pktNum, int fSize, byte[] dataArray)
    {
        hatName = name;
        packetTotal = (byte)pktTotal;
        packetNumber = (byte)pktNum;
        fileSize = fSize;
        data = dataArray;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, hatName);
        buffer.writeByte(packetTotal);
        buffer.writeByte(packetNumber);
        buffer.writeInt(fileSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        hatName = ByteBufUtils.readUTF8String(buffer);
        packetTotal = buffer.readByte();
        packetNumber = buffer.readByte();
        fileSize = buffer.readInt();

        data = new byte[fileSize];

        buffer.readBytes(data);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        HatHandler.receiveHatData(hatName, packetTotal, packetNumber, data, side.isServer() ? player : null, side.isServer());
    }
}
