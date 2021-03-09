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
    public static final String NBT_HAT_KEY = "HatsTag";
    public static final String NBT_HAT_SET_KEY = "HatSet";

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
                if(ent.getType().getRegistryName().equals(new ResourceLocation(disabledName)))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean hasBeenRandomlyAllocated(LivingEntity ent)
    {
        return ent.getPersistentData().getCompound(NBT_HAT_KEY).contains(NBT_HAT_SET_KEY);
    }

    public static void assignHat(LivingEntity ent)
    {
        CompoundNBT tag = ent.getPersistentData().getCompound(NBT_HAT_KEY);

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

        StringBuilder sb = new StringBuilder();

        HatInfo hatInfo = pool.getRandomHat();
        sb.append(hatInfo.name);

        ArrayList<HatInfo.Accessory> spawningAccessories = new ArrayList<>();
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
            }
        }

        for(HatInfo.Accessory accessory : spawningAccessories)
        {
            if(accessory.parent != null && !isParentInList(spawningAccessories, accessory.parent))
            {
                continue; //we ain't spawning you, go find your parent first!
            }

            sb.append(":");
            sb.append(accessory.name);
        }

        tag.putString(NBT_HAT_SET_KEY, sb.toString());

        ent.getPersistentData().put(NBT_HAT_KEY, tag);
    }

    public static void assignSpecificHat(LivingEntity ent, String s)
    {
        CompoundNBT tag = ent.getPersistentData().getCompound(NBT_HAT_KEY);

        tag.putString(NBT_HAT_SET_KEY, s);

        ent.getPersistentData().put(NBT_HAT_KEY, tag);
    }

    public static void assignNoHat(LivingEntity ent)
    {
        assignSpecificHat(ent, "");
    }

    public static String getHatDetails(LivingEntity ent)
    {
        CompoundNBT tag = ent.getPersistentData().getCompound(NBT_HAT_KEY);

        return tag.getString(NBT_HAT_SET_KEY);
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

    public static void addHat(ServerPlayerEntity player, String hatDetails)
    {
        List<String> strings = HatResourceHandler.COLON_SPLITTER.splitToList(hatDetails);
        if(!strings.isEmpty())
        {
            HatInfo info = HatResourceHandler.getAndSetAccessories(hatDetails);
            if(info != null)
            {
                boolean foundBase = false; //if stays false, this is a new hat.

                String hatName = strings.get(0);
                HatsSavedData.HatPart hatBase = null;
                HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.computeIfAbsent(player.getGameProfile().getId(), k -> new HatsSavedData.PlayerHatData(player.getGameProfile().getId()));
                for(HatsSavedData.HatPart hatPart : playerHatData.hatParts)
                {
                    if(hatName.equals(hatPart.name))
                    {
                        hatBase = hatPart;
                        hatBase.count++;
                        foundBase = true;
                        break;
                    }
                }

                if(hatBase == null)
                {
                    playerHatData.hatParts.add(hatBase = new HatsSavedData.HatPart(hatName));//this already sets the count to 1.
                }

                ArrayList<String> newAccessoriesName = new ArrayList<>();

                boolean newAccessory = false;
                for(int i = 1; i < strings.size(); i++)
                {
                    String accessoryName = strings.get(i);
                    boolean foundAccessory = false;
                    for(HatsSavedData.HatPart accessory : hatBase.hatParts)
                    {
                        if(accessoryName.equals(accessory.name))
                        {
                            accessory.count++;
                            foundAccessory = true;
                            break;
                        }
                    }

                    if(!foundAccessory)
                    {
                        newAccessory = true;
                        hatBase.hatParts.add(new HatsSavedData.HatPart(accessoryName));
                        newAccessoriesName.add(accessoryName);
                    }
                }

                if(!foundBase || newAccessory) //there's something new
                {
                    StringBuilder sb = new StringBuilder(hatName);
                    ArrayList<String> names = new ArrayList<>();
                    names.add(info.getDisplayName());

                    for(String s : newAccessoriesName)
                    {
                        for(HatInfo.Accessory accessory : info.accessories)
                        {
                            if(accessory.name.equals(s)) //oh hey we found it.
                            {
                                sb.append(":");
                                sb.append(s);

                                names.add("- " + accessory.getDisplayName());
                            }
                        }
                    }

                    Hats.channel.sendTo(new PacketNewHatPart(!foundBase, sb.toString(), names), player);
                }

                Hats.channel.sendTo(new PacketUpdateHats(hatBase.write(new CompoundNBT()), false), player);

                saveData.markDirty();
            }
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

        if(playerHatData != null)
        {
            return playerHatData.write(new CompoundNBT());
        }

        return new CompoundNBT();
    }
}
