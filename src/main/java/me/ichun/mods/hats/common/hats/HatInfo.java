package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HatInfo
{
    public final String name;
    public final Project project;
    public final ArrayList<Peripheral> peripherals = new ArrayList<>();

    public String forcedPool;
    public EnumRarity forcedRarity;

    public UUID contributorUUID;

    public HatInfo(String name, Project project)
    {
        this.name = name;
        this.project = project;

        findMeta();
        findPeripherals();
    }

    private void findMeta()
    {
        for(String note : this.project.notes)
        {
            if(note.startsWith("hats-rarity:"))
            {
                try
                {
                    forcedRarity = EnumRarity.valueOf(note.substring("hats-rarity:".length()).trim().toUpperCase(Locale.ROOT));
                }
                catch(IllegalArgumentException e)
                {
                    Hats.LOGGER.error("Cannot find Hat Rarity of {}", name);
                }
            }
            if(note.startsWith("hats-pool:"))
            {
                forcedPool = note.substring("hats-pool:".length()).trim();
            }
            if(note.startsWith("hats-contributor-uuid:"))
            {
                contributorUUID = UUID.fromString(note.substring("hats-contributor-uuid:".length()).trim());
            }
        }
    }

    private void findPeripherals()
    {
        HashMap<String, Peripheral> peripheralHashMap = new HashMap<>();
        for(Project.Part part : project.getAllParts())
        {
            String name = null;
            String parent = null;
            for(String note : part.notes)
            {
                if(note.startsWith("hats-peripheral:"))
                {
                    name = note.substring("hats-peripheral:".length()).trim();
                }
                if(note.startsWith("hats-peripheral-parent:"))
                {
                    parent = note.substring("hats-peripheral-parent:".length()).trim();
                }
            }

            if(name != null)
            {
                final String nameFinal = name;
                Peripheral peripheral = peripheralHashMap.computeIfAbsent(name, k -> new Peripheral(nameFinal));

                if(parent != null)
                {
                    peripheral.setParent(parent);
                }

                peripheral.parts.add(part);
            }
        }

        peripherals.addAll(peripheralHashMap.values());
    }

    public static class Peripheral
    {
        public @Nonnull final String name;
        public @Nullable String parent;
        public final HashSet<Project.Part> parts = new HashSet<>();

        public Peripheral(String name)
        {
            this.name = name;
        }

        public void setParent(String s)
        {
            this.parent = s;
        }
    }
}
