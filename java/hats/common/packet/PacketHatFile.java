package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.core.HatHandler;
import ichun.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class PacketHatFile extends AbstractPacket
{
    public String hatName;
    public File hatLocation;

    public PacketHatFile(){}

    public PacketHatFile(String name, File file)
    {
        hatName = name;
        hatLocation = file;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        try
        {
            int fileSize = (int)hatLocation.length();
            FileInputStream fis = new FileInputStream(hatLocation);

            ByteBufUtils.writeUTF8String(buffer, hatName);
            buffer.writeInt(fileSize);

            byte[] fileBytes = new byte[fileSize];
            fis.read(fileBytes);
            buffer.writeBytes(fileBytes);

            fis.close();
        }
        catch(Exception e)
        {
        }
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        HatHandler.receiveHatData(buffer, side.isServer() ? player : null, side.isServer());
    }
}
