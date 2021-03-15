package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketHatCustomisation extends AbstractPacket
{
    public ArrayList<HatsSavedData.HatPart> hats;
    public boolean updateHat;
    public HatsSavedData.HatPart newHat;

    public PacketHatCustomisation(){}

    public PacketHatCustomisation(ArrayList<HatsSavedData.HatPart> hats, boolean updateHat, HatsSavedData.HatPart newHat)
    {
        this.hats = new ArrayList<>(hats);
        this.updateHat = updateHat;
        this.newHat = newHat;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBoolean(updateHat);

        buf.writeInt(hats.size());

        for(HatsSavedData.HatPart hat : hats)
        {
            buf.writeCompoundTag(hat.write(new CompoundNBT()));
        }
        buf.writeCompoundTag(newHat.write(new CompoundNBT()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        updateHat = buf.readBoolean();

        hats = new ArrayList<>();
        int count = buf.readInt();

        for(int i = 0; i < count; i++)
        {
            HatsSavedData.HatPart part = new HatsSavedData.HatPart();
            part.read(buf.readCompoundTag());

            if(!part.name.isEmpty())
            {
                hats.add(part);
            }
        }

        newHat = new HatsSavedData.HatPart();
        newHat.read(buf.readCompoundTag());
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            HatHandler.setPlayerHatCustomisation(context.getSender(), hats, updateHat ? newHat : null);
        });
    }
}
