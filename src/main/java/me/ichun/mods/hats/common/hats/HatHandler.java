package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketNewHatPart;
import me.ichun.mods.hats.common.packet.PacketUpdateHats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;

public class HatHandler //Handles most of the server-related things.
{
    public static final EnumMap<EnumRarity, ArrayList<HatPool>> HAT_POOLS = new EnumMap<>(EnumRarity.class);
    public static final Random RAND = new Random();

    private static HatsSavedData saveData;

    public static void allocateHatPools() //TODO loading method when receiving a hat from the server.
    {
        HAT_POOLS.clear();

        HashMap<String, HatPool> poolsByName = new HashMap<>();

        for(Map.Entry<String, HatInfo> entry : HatResourceHandler.HATS.entrySet())
        {
            String hatName = entry.getKey();
            HatInfo hatInfo = entry.getValue();

            HatPool pool;
            if(hatInfo.forcedPool != null)
            {
                pool = poolsByName.computeIfAbsent(hatInfo.forcedPool, k -> new HatPool());
            }
            else
            {
                pool = poolsByName.computeIfAbsent(hatName, k -> new HatPool());
            }

            pool.addHatToPool(hatInfo);

            if(hatInfo.forcedRarity != null)
            {
                pool.forcedRarity = hatInfo.forcedRarity;
            }

            for(HatInfo.Accessory accessory : hatInfo.accessories)
            {
                RAND.setSeed(Math.abs((Hats.configServer.randSeed + entry.getKey() + accessory.name).hashCode()) * 420744333L); //Chat contributed random

                accessory.setRarity(getRarityForChance(RAND.nextDouble()));
            }
        }

        for(Map.Entry<String, HatPool> entry : poolsByName.entrySet())
        {
            HatPool pool = entry.getValue();
            if(pool.forcedRarity != null)
            {
                ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(pool.forcedRarity, k -> new ArrayList<>());
                hats.add(pool);
                continue;
            }

            RAND.setSeed(Math.abs((Hats.configServer.randSeed + entry.getKey()).hashCode()) * 154041013L); //Chat contributed random

            EnumRarity rarity = getRarityForChance(RAND.nextDouble());
            ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(rarity, k -> new ArrayList<>());
            for(HatInfo hatInfo : pool.hatsInPool)
            {
                hatInfo.rarity = rarity;
            }
            hats.add(pool);
        }
    }

    public static EnumRarity getRarityForChance(double chance)
    {
        EnumRarity[] rarities = EnumRarity.values();
        for(int i = 0; i < Hats.configServer.rarityMeasure.size(); i++)
        {
            if(chance < Hats.configServer.rarityMeasure.get(i))
            {
                return rarities[i];
            }
        }
        return EnumRarity.COMMON;
    }

    public static double getHatChance(LivingEntity ent)
    {
        double chance = Hats.configServer.hatChance;
        if(!ent.isNonBoss())
        {
            chance += Hats.configServer.bossHatChanceBonus;
        }
        return chance;
    }

    public static boolean canWearHat(LivingEntity ent)
    {
        HeadInfo info = HeadHandler.getHelper(ent.getClass());
        if(info != null && !info.noTopInfo)
        {
            for(String disabledName : Hats.configServer.disabledMobs)
            {
                if(new ResourceLocation(disabledName).equals(ent.getType().getRegistryName()))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static HatsSavedData.HatPart getHatPart(LivingEntity ent)
    {
        return ent.getCapability(HatsSavedData.HatPart.CAPABILITY_INSTANCE).orElseThrow(() -> new IllegalArgumentException("Entity " + ent.getName().getUnformattedComponentText() + " has no hat capabilities"));
    }

    public static boolean hasBeenRandomlyAllocated(LivingEntity ent)
    {
        return getHatPart(ent).count >= 0;
    }

    public static void assignHat(LivingEntity ent)
    {
        HatsSavedData.HatPart hatPart = getHatPart(ent);

        RAND.setSeed(Math.abs((Hats.configServer.randSeed + ent.getUniqueID().toString()).hashCode()) * 425480085L); //Chat contributed random

        boolean isBoss = !ent.isNonBoss();

        double chance = RAND.nextDouble();
        if(isBoss)
        {
            chance += Hats.configServer.bossRarityBonus;
        }
        EnumRarity rarity = getRarityForChance(chance);

        ArrayList<HatPool> hatPools = HAT_POOLS.get(rarity);
        HatPool pool = hatPools.get(RAND.nextInt(hatPools.size()));

        HatInfo hatInfo = pool.getRandomHat();
        hatPart.name = hatInfo.name;
        hatPart.count = 1;

        ArrayList<HatInfo.Accessory> spawningAccessories = new ArrayList<>();
        HashMap<String, ArrayList<HatInfo.Accessory>> conflicts = new HashMap<>();
        for(HatInfo.Accessory accessory : hatInfo.accessories)
        {
            RAND.setSeed(Math.abs((Hats.configServer.randSeed + ent.getUniqueID() + accessory.name).hashCode()) * 53579997854L); //Chat contributed random
            double accChance = Hats.configServer.rarityIndividual.get(accessory.rarity.ordinal());
            if(isBoss)
            {
                accChance += Hats.configServer.bossRarityBonus;
            }
            if(RAND.nextDouble() < accChance) //spawn the accessory
            {
                spawningAccessories.add(accessory);
                if(accessory.conflictLayer != null) //look for conflicts for accessories  that already got to spawn
                {
                    conflicts.computeIfAbsent(accessory.conflictLayer, k -> new ArrayList<>()).add(accessory);
                }
            }
        }

        //if there are no conflicts, remove
        conflicts.entrySet().removeIf(entry -> entry.getValue().size() <= 1);

        for(ArrayList<HatInfo.Accessory> conflictAccessories : conflicts.values())
        {
            while(conflictAccessories.size() > 1)
            {
                HatInfo.Accessory acc = conflictAccessories.get(ent.getRNG().nextInt(conflictAccessories.size()));// YOU LOST THE COIN TOSS
                spawningAccessories.remove(acc);
                conflictAccessories.remove(acc);
            }
        }

        for(HatInfo.Accessory accessory : spawningAccessories)
        {
            if(accessory.parent != null && !isParentInList(spawningAccessories, accessory.parent))
            {
                continue; //we ain't spawning you, go find your parent first!
            }

            HatsSavedData.HatPart accToSpawn = new HatsSavedData.HatPart(accessory.name);
            hatPart.hatParts.add(accToSpawn);
        }
    }

    public static void assignSpecificHat(LivingEntity ent, HatsSavedData.HatPart part)
    {
        HatsSavedData.HatPart hatPart = getHatPart(ent);
        if(part != null)
        {
            hatPart.copy(part);
        }
        else
        {
            hatPart.name = "";
            hatPart.count = 0;
        }
    }

    public static void assignNoHat(LivingEntity ent)
    {
        assignSpecificHat(ent, null);
    }

    private static boolean isParentInList(ArrayList<HatInfo.Accessory> accessories, String parent)
    {
        for(HatInfo.Accessory accessory : accessories)
        {
            if(parent.equals(accessory.name))
            {
                return true;
            }
        }
        return false;
    }

    public static void setSaveData(HatsSavedData data)
    {
        saveData = data;
    }

    public static void addHat(ServerPlayerEntity player, HatsSavedData.HatPart addedHat) //TODO test this
    {
        HatInfo info = HatResourceHandler.getAndSetAccessories(addedHat);
        if(info != null) //it's a valid hat
        {
            boolean foundBase = false; //if stays false, this is a new hat.

            String hatName = addedHat.name;
            HatsSavedData.HatPart hatBase = null;
            HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.computeIfAbsent(player.getGameProfile().getId(), k -> new HatsSavedData.PlayerHatData(player.getGameProfile().getId()));
            for(HatsSavedData.HatPart hatPart : playerHatData.hatParts)
            {
                if(hatName.equals(hatPart.name))
                {
                    hatBase = hatPart;
                    hatBase.count += addedHat.count;
                    foundBase = true;
                    break;
                }
            }

            if(hatBase == null)
            {
                playerHatData.hatParts.add(hatBase = new HatsSavedData.HatPart(hatName));//this already sets the count to 1.
            }

            ArrayList<HatsSavedData.HatPart> newAccessoriesName = new ArrayList<>();

            boolean newAccessory = false;
            for(int i = 1; i < addedHat.hatParts.size(); i++)
            {
                HatsSavedData.HatPart accessoryName = addedHat.hatParts.get(i);
                boolean foundAccessory = false;
                for(HatsSavedData.HatPart accessory : hatBase.hatParts)
                {
                    if(accessoryName.name.equals(accessory.name))
                    {
                        accessory.count += accessoryName.count;
                        foundAccessory = true;
                        break;
                    }
                }

                if(!foundAccessory)
                {
                    newAccessory = true;
                    hatBase.hatParts.add(accessoryName.createCopy());
                    newAccessoriesName.add(accessoryName);
                }
            }

            if(!foundBase || newAccessory) //there's something new
            {
                HatsSavedData.HatPart part = new HatsSavedData.HatPart(hatName);
                ArrayList<String> names = new ArrayList<>();
                names.add(info.getDisplayName());

                for(HatsSavedData.HatPart s : newAccessoriesName)
                {
                    for(HatInfo.Accessory accessory : info.accessories)
                    {
                        if(accessory.name.equals(s.name)) //oh hey we found it.
                        {
                            part.hatParts.add(s);

                            names.add("- " + accessory.getDisplayName());
                        }
                    }
                }

                Hats.channel.sendTo(new PacketNewHatPart(!foundBase, part, names), player);
            }

            Hats.channel.sendTo(new PacketUpdateHats(hatBase.write(new CompoundNBT()), false), player);

            saveData.markDirty();
        }
    }

    public static @Nonnull CompoundNBT getPlayerHatsNBT(PlayerEntity player)
    {
        if(saveData == null)
        {
            Hats.LOGGER.error("We're trying to get the save data for a player without having loaded the save! Player: {}", player.getName());
            Thread.dumpStack();
            return new CompoundNBT();
        }

        HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.get(player.getGameProfile().getId());

        if(playerHatData == null)
        {
            playerHatData = new HatsSavedData.PlayerHatData(player.getGameProfile().getId());

            ArrayList<HatPool> hatPools = HAT_POOLS.get(EnumRarity.LEGENDARY);
            for(HatPool hatPool : hatPools)
            {
                if(hatPool.forcedRarity == EnumRarity.LEGENDARY) //likely our contributors.
                {
                    for(HatInfo hatInfo : hatPool.hatsInPool)
                    {
                        if(hatInfo.contributorUUID != null && hatInfo.contributorUUID.equals(player.getGameProfile().getId())) //AMAGA WE FOUND A CONTRIBUTOR
                        {
                            playerHatData.hatParts.add(new HatsSavedData.HatPart(hatInfo.name));//this already sets the count to 1.
                        }
                    }
                }
            }

            saveData.playerHats.put(player.getGameProfile().getId(), playerHatData);
        }

        return playerHatData.write(new CompoundNBT());
    }
}
