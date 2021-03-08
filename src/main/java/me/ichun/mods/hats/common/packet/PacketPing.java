package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashSet;

public class PacketPing extends AbstractPacket
{
    public HashSet<String> classes = new HashSet<>();

    public PacketPing(){}

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(HeadHandler.MODEL_OFFSET_HELPERS_JSON.size());
        HeadHandler.MODEL_OFFSET_HELPERS_JSON.forEach((clz, s) -> buf.writeString(clz.getName()));
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            classes.add(readString(buf));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            Hats.eventHandlerClient.serverHasMod = true;

            if(ServerLifecycleHooks.getCurrentServer() == null || !ServerLifecycleHooks.getCurrentServer().isSinglePlayer()) //we're not on single player, check if we have the headinfos the server has.
            {
                HeadHandler.MODEL_OFFSET_HELPERS_JSON.forEach((clz, s) -> classes.remove(clz.getName()));

                if(!classes.isEmpty()) //we don't have these HeadInfos
                {
                    Hats.channel.sendToServer(new PacketRequestHeadInfos(classes));
                }
            }
        });
    }
}
