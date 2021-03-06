package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class HatPool
{
    public final ArrayList<HatInfo> hatsInPool = new ArrayList<>();
    private final Random rand = new Random(); //only used for getting a hat, no need to be tied to server rand.

    public EnumRarity forcedRarity = null;

    public HatInfo getRandomHat()
    {
        if(hatsInPool.size() == 1)
        {
            return hatsInPool.get(0);
        }
        return hatsInPool.get(rand.nextInt(hatsInPool.size()));
    }

    public void addHatToPool(HatInfo s)
    {
        if(!hatsInPool.contains(s))
        {
            hatsInPool.add(s);
        }
    }

    public void forceRarity(String s)
    {
        try
        {
            forcedRarity = EnumRarity.valueOf(s.toUpperCase(Locale.ROOT));
        }
        catch(IllegalArgumentException e)
        {
            Hats.LOGGER.error("Cannot find Hat Rarity of {}", s);
        }
    }
}
