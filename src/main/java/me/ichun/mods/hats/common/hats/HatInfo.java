package me.ichun.mods.hats.common.hats;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.model.ModelHat;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class HatInfo
        implements Comparable<HatInfo>
{
    public final @Nonnull String name;
    public final @Nonnull Project project;
    public final ArrayList<HatInfo> accessories = new ArrayList<>();
    public final ArrayList<String> hideParent = new ArrayList<>();

    private EnumRarity rarity; //Config synching should set this for the client

    public String forcedPool;
    public EnumRarity forcedRarity;

    public UUID contributorUUID;

    public String accessoryFor;
    public String accessoryLayer;
    public String accessoryParent;

    @OnlyIn(Dist.CLIENT)
    public ModelHat model;

    public float[] colouriser = new float[] { 1F, 1F, 1F, 1F }; //hatpart is invert of this
    public boolean hidden = false; //true to disable rendering

    public HatInfo(@Nonnull String name, @Nonnull Project project)
    {
        this.name = name;
        this.project = project;

        findMeta();
    }

    public String getDisplayName()
    {
        return (contributorUUID != null ? TextFormatting.AQUA : rarity != null ? rarity.getColour() : TextFormatting.WHITE).toString() + name;
    }

    public EnumRarity getRarity()
    {
        if(rarity == null)
        {
            if(forcedRarity != null)
            {
                rarity = forcedRarity;
            }
            else
            {
                HatHandler.RAND.setSeed(Math.abs((Hats.configServer.randSeed + getFullName()).hashCode()) * 154041013L); //Chat contributed random

                rarity = HatHandler.getRarityForChance(HatHandler.RAND.nextDouble());
            }
        }

        return rarity;
    }

    public void setRarity(EnumRarity rarity)
    {
        this.rarity = rarity;
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

    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn)
    {
        if(!hidden)
        {
            for(Project.Part part : project.getAllParts())
            {
                part.showModel = true;
            }

            for(HatInfo accessory : accessories)
            {
                if(!accessory.hidden && !accessory.hideParent.isEmpty())
                {
                    for(String s : accessory.hideParent)
                    {
                        for(Project.Part part : project.getAllParts())
                        {
                            if(part.name.equals(s))
                            {
                                part.showModel = false;
                            }
                        }
                    }
                }
            }

            getModel().render(stack, bufferIn.getBuffer(RenderType.getEntityTranslucentCull(project.getNativeImageResourceLocation())), packedLightIn, packedOverlayIn, colouriser[0], colouriser[1], colouriser[2], colouriser[3]);

            for(HatInfo accessory : accessories)
            {
                accessory.render(stack, bufferIn, packedLightIn, packedOverlayIn);
            }
        }
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
            if(note.startsWith("hats-accessory:"))
            {
                accessoryFor = note.substring("hats-accessory:".length()).trim();
            }
            if(note.startsWith("hats-accessory-layer:"))
            {
                accessoryLayer = note.substring("hats-accessory-layer:".length()).trim();
            }
            if(note.startsWith("hats-accessory-parent:"))
            {
                accessoryParent = note.substring("hats-accessory-parent:".length()).trim();
            }
            if(note.startsWith("hats-accessory-hide-parent-part:"))
            {
                hideParent.add(note.substring("hats-accessory-hide-parent-part:".length()).trim());
            }
        }
    }

    public void accessorise(ArrayList<HatInfo> newAccessories)
    {
        //take the accessories if the parent is null or is our name. The base calls this first, and will take all the null ones. We then pass on the list to the children to nab as they need.
        accessories.addAll(newAccessories.stream().filter(info -> info.accessoryParent == null || info.accessoryParent.equals(name)).collect(Collectors.toList()));
        newAccessories.removeAll(accessories);

        for(HatInfo accessory : accessories)
        {
            accessory.accessorise(newAccessories);
        }
    }

    public String getDisplayNameFor(String accessoryName)
    {
        if(accessoryName.equals(name))
        {
            return getDisplayName();
        }
        else
        {
            for(HatInfo accessory : accessories)
            {
                String s = accessory.getDisplayNameFor(accessoryName);
                if(s != null)
                {
                    return s;
                }
            }
        }
        return null;
    }

    public HatsSavedData.HatPart getAsHatPart(int count)
    {
        HatsSavedData.HatPart part = new HatsSavedData.HatPart(name);
        part.count = count;
        for(HatInfo accessory : accessories)
        {
            HatsSavedData.HatPart childPart = accessory.getAsHatPart(count);
            part.hatParts.add(childPart);
        }
        return part;
    }

    public void setAccessoriesState(ArrayList<HatsSavedData.HatPart> enabled)
    {
        for(HatInfo accessory : accessories)
        {
            boolean show = false;

            for(HatsSavedData.HatPart enabledAccessory : enabled)
            {
                if(accessory.name.equals(enabledAccessory.name) && enabledAccessory.count > 0)
                {
                    show = true;
                    break;
                }
            }

            accessory.hidden = show;
        }
    }

    @Override
    public int compareTo(HatInfo o)
    {
        return name.compareTo(o.name);
    }

    public String getFullName()
    {
        StringBuilder sb = new StringBuilder();
        if(accessoryFor != null)
        {
            sb.append(accessoryFor);
            sb.append("_");
        }
        if(accessoryParent != null)
        {
            sb.append(accessoryParent);
            sb.append("_");
        }
        sb.append(name);

        return sb.toString();
    }

    public void assignAccessoriesToPart(HatsSavedData.HatPart hatPart, LivingEntity ent)
    {
        ArrayList<HatInfo> spawningAccessories = new ArrayList<>();
        HashMap<String, ArrayList<HatInfo>> conflicts = new HashMap<>();
        for(HatInfo accessory : accessories)
        {
            double accChance = Hats.configServer.rarityIndividual.get(accessory.getRarity().ordinal()); //calling getRarity sets the accessory's rarity.

            HatHandler.RAND.setSeed(Math.abs((Hats.configServer.randSeed + ent.getUniqueID() + getFullName()).hashCode()) * 53579997854L); //Chat contributed random
            if(!ent.isNonBoss())
            {
                accChance += Hats.configServer.bossRarityBonus;
            }

            if(HatHandler.RAND.nextDouble() < accChance) //spawn the accessory
            {
                spawningAccessories.add(accessory);
                if(accessory.accessoryLayer != null) //look for conflicts for accessories  that already got to spawn
                {
                    conflicts.computeIfAbsent(accessory.accessoryLayer, k -> new ArrayList<>()).add(accessory);
                }
            }
        }

        //if there are no conflicts, remove
        conflicts.entrySet().removeIf(entry -> entry.getValue().size() <= 1);

        for(ArrayList<HatInfo> conflictAccessories : conflicts.values())
        {
            while(conflictAccessories.size() > 1)
            {
                HatInfo acc = conflictAccessories.get(ent.getRNG().nextInt(conflictAccessories.size()));// YOU LOST THE COIN TOSS
                spawningAccessories.remove(acc);
                conflictAccessories.remove(acc);
            }
        }

        for(HatInfo accessory : spawningAccessories)
        {
            HatsSavedData.HatPart accToSpawn = new HatsSavedData.HatPart(accessory.name);
            hatPart.hatParts.add(accToSpawn);

            accessory.assignAccessoriesToPart(accToSpawn, ent);
        }
    }

    public void matchPart(HatsSavedData.HatPart part)
    {
        for(HatInfo accessory : accessories)
        {
            accessory.hidden = true;
            for(HatsSavedData.HatPart hatPart : part.hatParts)
            {
                if(hatPart.name.equals(accessory.name))
                {
                    accessory.matchPart(hatPart);
                }
            }
        }

        hidden = part.count < 0;
        for(int i = 0; i < part.colouriser.length; i++)
        {
            colouriser[i] = 1F - part.colouriser[i];
        }
    }
}
