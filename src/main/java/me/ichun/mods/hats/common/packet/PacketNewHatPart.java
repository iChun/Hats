package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.toast.NewHatPartToast;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketNewHatPart extends AbstractPacket
{
    public boolean newHat;
    public String details;
    public ArrayList<String> names;

    public PacketNewHatPart(){}

    public PacketNewHatPart(boolean newHat, String details, ArrayList<String> names)
    {
        this.newHat = newHat;
        this.details = details;
        this.names = names;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBoolean(newHat);
        buf.writeString(details);

        buf.writeInt(names.size());
        for(String name : names)
        {
            buf.writeString(name);
        }
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        newHat = buf.readBoolean();
        details = readString(buf);

        names = new ArrayList<>();
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            names.add(readString(buf));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(this::handleClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Minecraft.getInstance().getToastGui().add(new NewHatPartToast(details, newHat, names));
    }
}
