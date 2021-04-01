package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class FilterContributor extends HatSorter
{
    @Override
    @Nonnull
    public String type()
    {
        return "filterContributor";
    }

    @Override
    public boolean isFilter()
    {
        return true;
    }

    @Override
    public void sort(ArrayList hats)
    {
        ArrayList<HatsSavedData.HatPart> contrib = new ArrayList<>();
        ArrayList<HatsSavedData.HatPart> nonContrib = new ArrayList<>();

        for(Object o : hats)
        {
            HatsSavedData.HatPart hat = (HatsSavedData.HatPart)o;
            HatInfo info = HatResourceHandler.getInfo(hat);
            if(info != null)
            {
                if(info.contributorUUID != null)
                {
                    contrib.add(hat);
                }
                else
                {
                    nonContrib.add(hat);
                }
            }
        }

        hats.clear();

        if(isInverse)
        {
            hats.addAll(contrib);
        }
        else
        {
            hats.addAll(nonContrib);
        }
    }
}
