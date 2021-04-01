package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;

public class SorterAlphabetical extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "sorterAlphabetical";
    }

    @Override
    public void sort(ArrayList hats)
    {
        Collections.sort(hats);
        if(isInverse)
        {
            Collections.reverse(hats);
        }
    }
}
