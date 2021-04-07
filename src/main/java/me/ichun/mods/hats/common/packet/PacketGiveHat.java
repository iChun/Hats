package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.EnumRarity;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatPool;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketGiveHat extends AbstractPacket
{
    public PacketGiveHat(){}

    @Override
    public void writeTo(PacketBuffer buf){}

    @Override
    public void readFrom(PacketBuffer buf){}

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();

            ArrayList<HatsSavedData.HatPart> playerInventory = HatHandler.getPlayerInventory(sender);
            if(playerInventory.isEmpty())
            {
                EnumRarity[] rarities = EnumRarity.values();
                for(EnumRarity rarity : rarities)
                {
                    ArrayList<HatPool> hatPools = HatHandler.HAT_POOLS.get(rarity);
                    if(hatPools != null && !hatPools.isEmpty())
                    {
                        HatHandler.RAND.setSeed(Math.abs((Hats.configServer.randSeed + sender.getUniqueID().toString()).hashCode()) * 425480085L); //Chat contributed random
                        HatPool pool = hatPools.get(HatHandler.RAND.nextInt(hatPools.size()));

                        HatsSavedData.HatPart hatPart = new HatsSavedData.HatPart();

                        HatInfo hatInfo = pool.getRandomHat();
                        hatPart.name = hatInfo.name;
                        hatPart.count = 1;
                        hatPart.isShowing = true;

                        HatHandler.addHat(sender, hatPart);

                        break;
                    }
                }
            }
        });
    }
}
