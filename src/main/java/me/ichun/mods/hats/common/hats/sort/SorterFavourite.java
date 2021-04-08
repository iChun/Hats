package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SorterFavourite extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "sorterFavourite";
    }

    @Override
    public void sort(List hats)
    {
        ArrayList<HatsSavedData.HatPart> favourite = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> notFavourite = new ArrayList<>();

        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            if(hat.hasFavourite())
            {
                favourite.add(hat);
            }
            else
            {
                notFavourite.add(hat);
            }
        }

        hats.clear();

        //sorter, not a filter, add the entire array list.
        if(isInverse)
        {
            hats.add(notFavourite);
            hats.add(favourite);
        }
        else
        {
            hats.add(favourite);
            hats.add(notFavourite);
        }
    }
}
