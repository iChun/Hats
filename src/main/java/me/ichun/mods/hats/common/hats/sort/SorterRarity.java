package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.hats.EnumRarity;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class SorterRarity extends HatSorter
{
    @Nonnull
    @Override
    public String type()
    {
        return "sorterRarity";
    }

    @Override
    public void sort(List hats)
    {
        ArrayList unsorted = new ArrayList();
        EnumMap<EnumRarity, ArrayList<HatsSavedData.HatPart>> hatsByRarity = new EnumMap<>(EnumRarity.class);
        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            HatInfo info = HatResourceHandler.getInfo(hat);
            if(info != null)
            {
                hatsByRarity.computeIfAbsent(info.getRarity(), k -> new ArrayList<>()).add(hat);
            }
            else //Most likely an accessory
            {
                unsorted.add(hat);
            }
        }

        hats.clear();

        EnumRarity[] rarities = EnumRarity.values();
        for(EnumRarity rarity : rarities)
        {
            if(hatsByRarity.containsKey(rarity))
            {
                hats.add(hatsByRarity.get(rarity));
            }
        }
        if(isInverse)
        {
            Collections.reverse(hats);
        }
        hats.add(unsorted);
    }
}
