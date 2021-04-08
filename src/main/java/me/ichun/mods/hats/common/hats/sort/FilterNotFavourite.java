package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FilterNotFavourite extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "filterNotFavourite";
    }

    @Override
    public boolean isFilter()
    {
        return true;
    }

    @Override
    public void sort(List hats)
    {
        ArrayList<HatsSavedData.HatPart> notFavourite = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> favourite = new ArrayList<>();

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

        if(isInverse)
        {
            hats.addAll(notFavourite);
        }
        else
        {
            hats.addAll(favourite);
        }
    }
}
