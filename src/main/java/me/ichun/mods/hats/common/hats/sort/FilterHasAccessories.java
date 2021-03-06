package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FilterHasAccessories extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "filterHasAccessories";
    }

    @Override
    public boolean isFilter()
    {
        return true;
    }

    @Override
    public void sort(List hats)
    {
        ArrayList<HatsSavedData.HatPart> hasAccessories = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> noAccessories = new ArrayList<>();

        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            if(!hat.hasUnlockedAccessory())
            {
                noAccessories.add(hat);
            }
            else
            {
                hasAccessories.add(hat);
            }
        }

        hats.clear();

        if(isInverse)
        {
            hats.addAll(noAccessories);
        }
        else
        {
            hats.addAll(hasAccessories);
        }
    }
}
