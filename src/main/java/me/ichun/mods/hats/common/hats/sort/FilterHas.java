package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class FilterHas extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "filterHas";
    }

    @Override
    public boolean isFilter()
    {
        return true;
    }

    @Override
    public void sort(ArrayList hats)
    {
        ArrayList<HatsSavedData.HatPart> discovered = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> undiscovered = new ArrayList<>();

        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            if(hat.count <= 0)
            {
                undiscovered.add(hat);
            }
            else
            {
                discovered.add(hat);
            }
        }

        hats.clear();

        if(isInverse)
        {
            hats.addAll(undiscovered);
        }
        else
        {
            hats.addAll(discovered);
        }
    }
}
