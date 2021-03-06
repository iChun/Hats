package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPing extends AbstractPacket
{
    public PacketPing(){}

    @Override
    public void writeTo(PacketBuffer buf)
    {
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> Hats.eventHandlerClient.serverHasMod = true);
    }
}
