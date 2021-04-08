package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SortHandler
{
    public static HashMap<String, Class<? extends HatSorter>> SORTERS = new HashMap<String, Class<? extends HatSorter>>() {{
        put("filterContributor", FilterContributor.class);
        put("filterHas", FilterHas.class);
        put("filterHasAccessories", FilterHasAccessories.class);
        put("filterNotFavourite", FilterNotFavourite.class);
        put("filterUndiscovered", FilterUndiscovered.class);

        put("sorterAlphabetical", SorterAlphabetical.class); //This is the only sorter that doesn't create a new category to sort.
        put("sorterCount", SorterCount.class);
        put("sorterDiscovered", SorterDiscovered.class);
        put("sorterFavourite", SorterFavourite.class);
        put("sorterRarity", SorterRarity.class);
    }};


    public static void sort(ArrayList<HatSorter> sorters, List<?> hats, boolean allowFilter)
    {
        for(HatSorter sorter : sorters)
        {
            if(!allowFilter && sorter.isFilter())
            {
                continue;
            }
            sorter.sortRecursive(hats);
        }

        ArrayList newHats = new ArrayList();
        digForHats(newHats, hats);

        hats.clear();
        hats.addAll(newHats);
    }

    private static void digForHats(ArrayList<HatsSavedData.HatPart> finalHatList, List<?> listOfHats)
    {
        for(Object listOfHat : listOfHats)
        {
            if(listOfHat instanceof List)
            {
                digForHats(finalHatList, (List<?>)listOfHat);
            }
            else if(listOfHat instanceof HatsSavedData.HatPart)
            {
                finalHatList.add((HatsSavedData.HatPart)listOfHat);
            }
        }
    }
}
