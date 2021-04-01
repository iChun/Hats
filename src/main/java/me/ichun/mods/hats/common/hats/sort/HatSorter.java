package me.ichun.mods.hats.common.hats.sort;

import me.ichun.mods.hats.common.world.HatsSavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public abstract class HatSorter
{
    public boolean isInverse;

    @Nonnull
    public abstract String type();

    public boolean isFilter() { return false; }

    public final void sortRecursive(ArrayList<?> hats)
    {
        if(!hats.isEmpty())
        {
            Object o = hats.get(0);

            if(o instanceof ArrayList)
            {
                sortRecursive((ArrayList<?>)o);
            }
            else if(o instanceof HatsSavedData.HatPart)
            {
                sort(hats);
            }
        }
    }

    public abstract void sort(ArrayList hats);
}
