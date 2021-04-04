package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SorterDiscovered extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "sorterDiscovered";
    }

    @Override
    public void sort(List hats)
    {
        ArrayList<HatsSavedData.HatPart> discovered = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> undiscovered = new ArrayList<>();

        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            if(hat.count == 0 && hat.hsbiser[2] == 1F) //this is how we mark undiscovered hats.... with 0 brightness (so they appear black)
            {
                undiscovered.add(hat);
            }
            else
            {
                discovered.add(hat);
            }
        }

        hats.clear();

        //sorter, not a filter, add the entire array list.
        if(isInverse)
        {
            hats.add(undiscovered);
            hats.add(discovered);
        }
        else
        {
            hats.add(discovered);
            hats.add(undiscovered);
        }
    }
}
