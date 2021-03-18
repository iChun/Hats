package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketHatLauncherCustomisation extends AbstractPacket
{
    public HatsSavedData.HatPart newHat;

    public PacketHatLauncherCustomisation(){}

    public PacketHatLauncherCustomisation(HatsSavedData.HatPart newHat)
    {
        this.newHat = newHat;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeCompoundTag(newHat.write(new CompoundNBT()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        newHat = new HatsSavedData.HatPart();
        newHat.read(buf.readCompoundTag());
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            HatHandler.setHatLauncherCustomisation(context.getSender(), newHat);
        });
    }
}
