package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.client.model.ModelHat;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HatInfo
        implements Comparable<HatInfo>
{
    public final @Nonnull String name;
    public final @Nonnull Project project;
    public final ArrayList<Accessory> accessories = new ArrayList<>();
    public EnumRarity rarity; //Config synching should set this for the client

    public String forcedPool;
    public EnumRarity forcedRarity;

    public UUID contributorUUID;

    @OnlyIn(Dist.CLIENT)
    public ModelHat model;

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

    @OnlyIn(Dist.CLIENT)
    public ModelHat getModel()
    {
        if(model == null)
        {
            model = new ModelHat(this);
        }
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    public void destroy()
    {
        //Don't need to destroy the model
        project.destroy();
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
            if(note.startsWith("hats-contributor-mini-me:") && contributorUUID == null)
            {
                contributorUUID = EntityHelper.UUID_EXAMPLE;
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

    @Override
    public int compareTo(HatInfo o)
    {
        return name.compareTo(o.name);
    }

    public static class Accessory
            implements Comparable<Accessory>
    {
        public @Nonnull final String name;
        public @Nullable String displayName;
        public @Nullable String parent;
        public @Nullable EnumRarity rarity; //Config synching should set this for the client
        public final HashSet<Project.Part> parts = new HashSet<>();

        public Accessory(String name)
        {
            this.name = name;
        }

        public void setDisplayName(String s)
        {
            this.displayName = s;
        }

        public String getDisplayName()
        {
            return (rarity != null ? rarity.getColour() : TextFormatting.WHITE).toString() + (displayName != null ? displayName : name);
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

        @Override
        public int compareTo(Accessory o)
        {
            String com = displayName != null ? displayName : name;
            String their = o.displayName != null ? o.displayName : o.name;
            return com.compareTo(their);
        }
    }
}
