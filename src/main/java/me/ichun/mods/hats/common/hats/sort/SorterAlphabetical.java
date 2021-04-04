package me.ichun.mods.hats.common.hats.sort;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class SorterAlphabetical extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "sorterAlphabetical";
    }

    @Override
    public void sort(List hats)
    {
        Collections.sort(hats);
        if(isInverse)
        {
            Collections.reverse(hats);
        }
    }
}
