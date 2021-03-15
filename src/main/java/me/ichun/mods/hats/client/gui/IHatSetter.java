package me.ichun.mods.hats.client.gui;

import me.ichun.mods.hats.common.world.HatsSavedData;
import net.minecraft.client.gui.INestedGuiEventHandler;

public interface IHatSetter extends INestedGuiEventHandler
{
    default void onNewHatSet(HatsSavedData.HatPart newHat)
    {
        getEventListeners().stream().filter(child -> child instanceof IHatSetter).forEach(child -> ((IHatSetter)child).onNewHatSet(newHat));
    }
}
