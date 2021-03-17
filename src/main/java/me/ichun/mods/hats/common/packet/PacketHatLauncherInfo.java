package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketHatLauncherInfo extends AbstractPacket
{
    public int entId;
    public CompoundNBT partInfo;

    public PacketHatLauncherInfo(){}

    public PacketHatLauncherInfo(int id, CompoundNBT part)
    {
        entId = id;
        partInfo = part;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entId);
        buf.writeCompoundTag(partInfo);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        entId = buf.readInt();
        partInfo = buf.readCompoundTag();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer()) // server
            {
                Entity entity = context.getSender().world.getEntityByID(entId);
                if(entity instanceof ServerPlayerEntity)
                {
                    ServerPlayerEntity player = (ServerPlayerEntity)entity;
                    HatsSavedData.HatPart receivedPart = new HatsSavedData.HatPart();
                    receivedPart.read(partInfo);

                    ItemStack is = player.getHeldItem(receivedPart.count == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
                    if(is.getItem() instanceof ItemHatLauncher)
                    {
                        HatsSavedData.HatPart part = HatHandler.getHatPart(is).createCopy();
                        part.count = receivedPart.count;
                        Hats.channel.sendTo(new PacketHatLauncherInfo(entId, part.write(new CompoundNBT())), context.getSender());
                    }
                }
            }
            else
            {
                handleClient();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient()
    {
        Entity ent = Minecraft.getInstance().world.getEntityByID(entId);
        if(ent instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity)ent;
            HatsSavedData.HatPart receivedPart = new HatsSavedData.HatPart();
            receivedPart.read(partInfo);

            ItemStack is = player.getHeldItem(receivedPart.count == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if(is.getItem() instanceof ItemHatLauncher)
            {
                HatsSavedData.HatPart part = HatHandler.getHatPart(is);
                part.read(partInfo); //red the info from server
                part.count = 1;
                part.isShowing = true;
            }
        }
    }
}
