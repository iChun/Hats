package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;


public class PacketRequestHeadInfos extends AbstractPacket
{
    public HashSet<String> missingClasses = new HashSet<>();

    public PacketRequestHeadInfos(){}

    public PacketRequestHeadInfos(HashSet<String> missing)
    {
        missingClasses = missing;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(missingClasses.size());
        missingClasses.forEach(buf::writeString);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            missingClasses.add(readString(buf));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            HashSet<String> headInfos = new HashSet<>();

            HeadHandler.MODEL_OFFSET_HELPERS_JSON.forEach((clz, s) -> {
                if(missingClasses.contains(clz.getName()))
                {
                    headInfos.add(s);
                }
            });

            Hats.channel.sendTo(new PacketHeadInfos(headInfos), context.getSender());
        });
    }
}
