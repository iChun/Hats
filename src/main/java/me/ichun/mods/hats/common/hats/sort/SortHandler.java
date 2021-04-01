package me.ichun.mods.hats.common.hats.sort;

import java.util.ArrayList;
import java.util.HashMap;

public class SortHandler
{
    public static HashMap<String, Class<? extends HatSorter>> SORTERS = new HashMap<String, Class<? extends HatSorter>>() {{
        put("filterContributor", FilterContributor.class);
        put("filterHas", FilterHas.class);
        put("filterUndiscovered", FilterUndiscovered.class);

        put("sorterAlphabetical", SorterAlphabetical.class); //This is the only sorter that doesn't create a new category to sort.
        put("sorterCount", SorterCount.class);
        put("sorterDiscovered", SorterDiscovered.class);
        put("sorterRarity", SorterRarity.class);
    }};


    public static void sort(ArrayList<HatSorter> sorters, ArrayList<?> hats)
    {
        for(HatSorter sorter : sorters)
        {
            sorter.sortRecursive(hats);
        }
    }
}
