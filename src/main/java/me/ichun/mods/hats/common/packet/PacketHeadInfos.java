package me.ichun.mods.hats.common.packet;

import com.google.gson.JsonSyntaxException;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;


public class PacketHeadInfos extends AbstractPacket
{
    public HashSet<String> headInfos = new HashSet<>();

    public PacketHeadInfos(){}

    public PacketHeadInfos(HashSet<String> heads)
    {
        headInfos = heads;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(headInfos.size());
        headInfos.forEach(buf::writeString);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            headInfos.add(readString(buf));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            for(String json : headInfos)
            {
                try
                {
                    HeadInfo info = HeadHandler.GSON.fromJson(json, HeadInfo.class);
                    if(info != null && info.forClass != null && HeadHandler.readHeadInfoJson(json)) //Valid enough, I guess.
                    {
                        //The HeadInfo is valid, let's save it.
                        String className = info.forClass.substring(info.forClass.lastIndexOf(".") + 1);

                        File file = new File(HeadHandler.getHeadsDir().resolve("FromServer").toFile(), className);
                        file.mkdirs();
                        File file2 = new File(file, className);
                        FileUtils.writeStringToFile(file2, json, StandardCharsets.UTF_8); //Save it, we will load it up next time.

                        Hats.LOGGER.info("Received HeadInfo from server for class: {}", info.forClass);
                    }
                    else
                    {
                        Hats.LOGGER.warn("Received erroneous HeadInfo from server: {}", json);
                    }
                }
                catch(ClassNotFoundException | JsonSyntaxException | IOException e)
                {
                    Hats.LOGGER.warn("Received erroneous HeadInfo from server: {}", json);
                    e.printStackTrace();
                }
            }
        });
    }
}
