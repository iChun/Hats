package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.hats.EnumRarity;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

public class SorterRarity extends HatSorter
{
    @Nonnull
    @Override
    public String type()
    {
        return "sorterRarity";
    }

    @Override
    public void sort(ArrayList hats)
    {
        EnumMap<EnumRarity, ArrayList<HatsSavedData.HatPart>> hatsByRarity = new EnumMap<>(EnumRarity.class);
        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            HatInfo info = HatResourceHandler.getInfo(hat);
            hatsByRarity.computeIfAbsent(info.getRarity(), k -> new ArrayList<>()).add(hat);
        }

        hats.clear();

        EnumRarity[] rarities = EnumRarity.values();
        for(EnumRarity rarity : rarities)
        {
            hats.add(hatsByRarity.get(rarity));
        }
        Collections.reverse(hats);
    }
}
