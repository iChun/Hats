package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateHats extends AbstractPacket
{
    public CompoundNBT nbt;
    public boolean isFullInventory;

    public PacketUpdateHats(){}

    public PacketUpdateHats(CompoundNBT tag, boolean isFullInventory)
    {
        this.nbt = tag;
        this.isFullInventory = isFullInventory;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeCompoundTag(nbt);
        buf.writeBoolean(isFullInventory);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        nbt = buf.readCompoundTag();
        isFullInventory = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(isFullInventory)
            {
                //Create a full PlayerHatData
                HatsSavedData.PlayerHatData hatData = new HatsSavedData.PlayerHatData();
                hatData.read(nbt);

                Hats.eventHandlerClient.hatsInventory = hatData;
            }
            else
            {
                HatsSavedData.HatPart hatPart = new HatsSavedData.HatPart();
                hatPart.read(nbt);

                Hats.eventHandlerClient.updateHatInventory(hatPart);
            }

            refreshHats();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshHats()
    {
        if(Minecraft.getInstance().currentScreen instanceof WorkspaceHats)
        {
            ((WorkspaceHats)Minecraft.getInstance().currentScreen).refreshHats();
        }
    }
}
