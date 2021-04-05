package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.file.Files;
import java.util.HashSet;


public class PacketHatsList extends AbstractPacket
{
    public HashSet<String> hatNames = new HashSet<>();
    public boolean requested;

    public PacketHatsList(){}

    public PacketHatsList(HashSet<String> heads, boolean requested)
    {
        this.hatNames = heads;
        this.requested = requested;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(hatNames.size());
        hatNames.forEach(buf::writeString);

        buf.writeBoolean(requested);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            hatNames.add(readString(buf));
        }

        requested = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context) //received by server
    {
        context.enqueueWork(() -> {
            if(requested)
            {
                //Send each hat
                for(String s : hatNames)
                {
                    HatInfo info = HatResourceHandler.getInfoFromFullName(s);
                    if(info != null && info.project.saveFile != null)
                    {
                        try
                        {
                            final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

                            String origin = "Server";
                            String md5 = IOUtil.getMD5Checksum(info.project.saveFile);
                            byte[] data = Files.readAllBytes(info.project.saveFile.toPath());

                            String fileName = info.project.saveFile.getName();
                            int fileSize = data.length;

                            int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

                            int packetCount = 0;
                            int offset = 0;
                            while(fileSize > 0)
                            {
                                byte[] fileBytes = new byte[Math.min(fileSize, maxFile)];
                                int index = 0;
                                while(index < fileBytes.length) //from index 0 to 30999
                                {
                                    fileBytes[index] = data[index + offset];
                                    index++;
                                }

                                Hats.channel.sendTo(new PacketHatFragment(fileName, packetsToSend, packetCount, fileBytes, origin, md5), context.getSender());

                                packetCount++;
                                fileSize -= fileBytes.length;
                                offset += index;
                            }

                        }
                        catch(Exception e)
                        {
                            Hats.LOGGER.error("Error creating hat as packet fragment: {}", s);
                            e.printStackTrace();
                        }
                    }
                }
            }
            else
            {
                HashSet<String> ourHatNames = HatResourceHandler.compileHatNames();
                hatNames.removeIf(ourHatNames::remove);

                Hats.channel.sendTo(new PacketHatsListResponse(hatNames, ourHatNames), context.getSender());
            }
        });
    }
}
