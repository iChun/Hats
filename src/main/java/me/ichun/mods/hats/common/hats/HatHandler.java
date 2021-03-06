package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class HatHandler
{
    public static final EnumMap<EnumRarity, ArrayList<HatPool>> HAT_POOLS = new EnumMap<>(EnumRarity.class);
    public static final Random RAND = new Random();
    public static final String NBT_HAT_KEY = "HatsTag";
    public static final String NBT_HAT_SET_KEY = "HatSet";

    public static void allocateHatPools()
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

            for(HatInfo.Peripheral peripheral : hatInfo.peripherals)
            {
                RAND.setSeed(Math.abs((Hats.configServer.randSeed + entry.getKey() + peripheral.name).hashCode()) * 420744333L); //Chat contributed random

                peripheral.rarity = getRarityForChance(RAND.nextDouble());
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

            ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(getRarityForChance(RAND.nextDouble()), k -> new ArrayList<>());
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

        ArrayList<HatInfo.Peripheral> spawningPeripherals = new ArrayList<>();
        for(HatInfo.Peripheral peripheral : hatInfo.peripherals)
        {
            RAND.setSeed(Math.abs((Hats.configServer.randSeed + peripheral.name).hashCode()) * 53579997854L); //Chat contributed random
            double periphChance = Hats.configServer.rarityIndividual.get(peripheral.rarity.ordinal());
            if(isBoss)
            {
                periphChance += Hats.configServer.bossRarityBonus;
            }
            if(RAND.nextDouble() < periphChance) //spawn the peripheral
            {
                spawningPeripherals.add(peripheral);
            }
        }

        for(HatInfo.Peripheral peripheral : spawningPeripherals)
        {
            if(peripheral.parent != null && !isParentInList(spawningPeripherals, peripheral.parent))
            {
                continue; //we ain't spawning you, go find your parent first!
            }

            sb.append(":");
            sb.append(peripheral.name);
        }

        tag.putString(NBT_HAT_SET_KEY, sb.toString());

        ent.getPersistentData().put(NBT_HAT_KEY, tag);
    }

    public static void denyHat(LivingEntity ent)
    {
        CompoundNBT tag = ent.getPersistentData().getCompound(NBT_HAT_KEY);

        tag.putString(NBT_HAT_SET_KEY, "");

        ent.getPersistentData().put(NBT_HAT_KEY, tag);
    }

    public static String getHatDetails(LivingEntity ent)
    {
        CompoundNBT tag = ent.getPersistentData().getCompound(NBT_HAT_KEY);

        return tag.getString(NBT_HAT_SET_KEY);
    }

    private static boolean isParentInList(ArrayList<HatInfo.Peripheral> peripherals, String parent)
    {
        for(HatInfo.Peripheral peripheral : peripherals)
        {
            if(parent.equals(peripheral.parent))
            {
                return true;
            }
        }
        return false;
    }
}
