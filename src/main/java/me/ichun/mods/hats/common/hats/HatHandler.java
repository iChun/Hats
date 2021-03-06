package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;

import java.util.*;

public class HatHandler
{
    public static final EnumMap<EnumRarity, ArrayList<HatPool>> HAT_POOLS = new EnumMap<>(EnumRarity.class);
    public static final Random RAND = new Random();

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

            pool.addHatToPool(hatName);

            if(hatInfo.forcedRarity != null)
            {
                pool.forcedRarity = hatInfo.forcedRarity;
            }
        }

        EnumRarity[] rarities = EnumRarity.values();
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

            double chance = RAND.nextDouble();
            for(int i = 0; i < Hats.configServer.rarityMeasure.size(); i++)
            {
                if(chance < Hats.configServer.rarityMeasure.get(i))
                {
                    ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(rarities[i], k -> new ArrayList<>());
                    hats.add(pool);
                    break;
                }
            }
        }
    }

    public static void serverStarting()
    {
        allocateHatPools();
    }
}
