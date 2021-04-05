package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;

public class PacketEntityHatDetails extends AbstractPacket //Hat per entity, not actual hat entity details.
{
    public HashMap<Integer, HatsSavedData.HatPart> entIdToHat;

    public PacketEntityHatDetails(){}

    public PacketEntityHatDetails(HashMap<Integer, HatsSavedData.HatPart> entIdToHat)
    {
        this.entIdToHat = entIdToHat;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entIdToHat.size());

        entIdToHat.forEach((i, s) -> {
            buf.writeInt(i);
            buf.writeCompoundTag(s.write(new CompoundNBT()));
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFrom(PacketBuffer buf)
    {
        entIdToHat = new HashMap<>();
        int count = buf.readInt();

        for(int i = 0; i < count; i++)
        {
            HatsSavedData.HatPart part = new HatsSavedData.HatPart();
            entIdToHat.put(buf.readInt(), part);
            part.read(buf.readCompoundTag());
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(this::executeClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void executeClient()
    {
        if(Minecraft.getInstance().world != null)
        {
            entIdToHat.forEach((id, hatDetails) -> {
                Entity ent = Minecraft.getInstance().world.getEntityByID(id);
                if(ent instanceof LivingEntity)
                {
                    HatHandler.assignSpecificHat((LivingEntity)ent, hatDetails);

                    if(ent == Minecraft.getInstance().player && Minecraft.getInstance().currentScreen instanceof WorkspaceHats)
                    {
                        ((WorkspaceHats)Minecraft.getInstance().currentScreen).refreshHats();
                    }
                }
            });
        }
    }
}
