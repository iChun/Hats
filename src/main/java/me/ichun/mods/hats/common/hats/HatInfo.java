package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HatInfo
{
    public final @Nonnull String name;
    public final @Nonnull Project project;
    public final ArrayList<Accessory> accessories = new ArrayList<>();
    public EnumRarity rarity;

    public String forcedPool;
    public EnumRarity forcedRarity;

    public UUID contributorUUID;

    public HatInfo(@Nonnull String name, @Nonnull Project project)
    {
        this.name = name;
        this.project = project;

        findMeta();
        findAccessories();
    }

    public String getDisplayName()
    {
        return (contributorUUID != null ? TextFormatting.AQUA : rarity != null ? rarity.getColour() : TextFormatting.WHITE).toString() + name;
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

    private void findAccessories()
    {
        HashMap<String, Accessory> accessoryHashMap = new HashMap<>();
        for(Project.Part part : project.getAllParts())
        {
            String name = null;
            String parent = null;
            String displayName = null;
            for(String note : part.notes)
            {
                if(note.startsWith("hats-accessory:"))
                {
                    name = note.substring("hats-accessory:".length()).trim();
                }
                if(note.startsWith("hats-accessory-parent:"))
                {
                    parent = note.substring("hats-accessory-parent:".length()).trim();
                }
                if(note.startsWith("hats-accessory-name:"))
                {
                    displayName = note.substring("hats-accessory-name:".length()).trim();
                }
            }

            if(name != null)
            {
                final String nameFinal = name;
                Accessory accessory = accessoryHashMap.computeIfAbsent(name, k -> new Accessory(nameFinal));

                if(parent != null)
                {
                    accessory.setParent(parent);
                }

                if(displayName != null)
                {
                    accessory.setDisplayName(displayName);
                }

                accessory.parts.add(part);
            }
        }

        accessories.addAll(accessoryHashMap.values());
    }

    public void setAccessoriesState(ArrayList<String> enabled)
    {
        for(Accessory accessory : accessories)
        {
            accessory.show(enabled.contains(accessory.name));
        }
    }

    public static class Accessory
    {
        public @Nonnull final String name;
        public @Nullable String displayName;
        public @Nullable String parent;
        public @Nullable EnumRarity rarity; //This is only set on the server!
        public final HashSet<Project.Part> parts = new HashSet<>();

        public Accessory(String name)
        {
            this.name = name;
        }

        public void setDisplayName(String s)
        {
            this.displayName = s;
        }

        public void setParent(String s)
        {
            this.parent = s;
        }

        public void setRarity(EnumRarity rarity)
        {
            this.rarity = rarity;
        }

        public void show(boolean show)
        {
            for(Project.Part part : parts)
            {
                part.showModel = show;
            }
        }
    }
}
