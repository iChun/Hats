package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.toast.NewHatPartToast;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketNewHatPart extends AbstractPacket
{
    public boolean newHat;
    public HatsSavedData.HatPart details;
    public ArrayList<String> names;

    public PacketNewHatPart(){}

    public PacketNewHatPart(boolean newHat, HatsSavedData.HatPart details, ArrayList<String> names)
    {
        this.newHat = newHat;
        this.details = details;
        this.names = names;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBoolean(newHat);
        buf.writeCompoundTag(details.write(new CompoundNBT()));

        buf.writeInt(names.size());
        for(String name : names)
        {
            buf.writeString(name);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        newHat = buf.readBoolean();
        HatsSavedData.HatPart part = new HatsSavedData.HatPart();
        part.read(buf.readCompoundTag());
        details = part;

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
