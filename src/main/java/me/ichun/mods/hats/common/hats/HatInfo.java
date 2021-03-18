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
    private int worth = -1;

    public String forcedPool;
    public int forcedWorth = -1;
    public EnumRarity forcedRarity;

    public UUID contributorUUID;

    public String accessoryFor;
    public ArrayList<String> accessoryLayer = new ArrayList<>();
    public String accessoryParent;

    @OnlyIn(Dist.CLIENT)
    public ModelHat model;

    public float[] colouriser = new float[] { 1F, 1F, 1F, 1F }; //hatpart is invert of this
    public boolean hidden = false; //true to disable rendering

    public HatInfo(@Nonnull String name, @Nonnull Project project) //TODO head top/eye analyser for HeadInfo
    {
        this.name = name;
        this.project = project;

        findMeta();
    }

    public String getDisplayName()
    {
        return (contributorUUID != null ? TextFormatting.AQUA : getRarity().getColour()).toString() + name;
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

    public int getWorth()
    {
        if(worth < 0)
        {
            int value = Hats.configServer.tokensByRarity.get(getRarity());
            if(accessoryFor != null)
            {
                value = (int)Math.ceil(Hats.configServer.accessoryCostMultiplier * value);
            }
            worth = value;
        }
        return worth;
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
    public void render(MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn, boolean cull)
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

            getModel().render(stack, bufferIn.getBuffer(cull ? RenderType.getEntityTranslucentCull(project.getNativeImageResourceLocation()) : RenderType.getEntityTranslucent(project.getNativeImageResourceLocation())), packedLightIn, packedOverlayIn, colouriser[0], colouriser[1], colouriser[2], colouriser[3]);

            for(HatInfo accessory : accessories)
            {
                accessory.render(stack, bufferIn, packedLightIn, packedOverlayIn, cull);
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
            else if(note.startsWith("hats-pool:"))
            {
                forcedPool = note.substring("hats-pool:".length()).trim();
            }
            else if(note.startsWith("hats-worth:"))
            {
                try
                {
                    forcedWorth = Integer.parseInt(note.substring("hats-worth:".length()).trim());
                }
                catch(NumberFormatException e)
                {
                    Hats.LOGGER.error("Error parsing forced worth of hat: \"{}\" from project: {}", note, this.project.saveFile);
                }
            }
            else if(note.startsWith("hats-contributor-uuid:"))
            {
                contributorUUID = UUID.fromString(note.substring("hats-contributor-uuid:".length()).trim());
            }
            else if(note.startsWith("hats-contributor-mini-me:"))
            {
                if(contributorUUID == null) contributorUUID = EntityHelper.UUID_EXAMPLE;
            }
            else if(note.startsWith("hats-accessory:"))
            {
                accessoryFor = note.substring("hats-accessory:".length()).trim();
            }
            else if(note.startsWith("hats-accessory-layer:"))
            {
                accessoryLayer.add(note.substring("hats-accessory-layer:".length()).trim());
            }
            else if(note.startsWith("hats-accessory-parent:"))
            {
                accessoryParent = note.substring("hats-accessory-parent:".length()).trim();
            }
            else if(note.startsWith("hats-accessory-hide-parent-part:"))
            {
                hideParent.add(note.substring("hats-accessory-hide-parent-part:".length()).trim());
            }
            else if(note.startsWith("hat"))
            {
                Hats.LOGGER.warn("We found a hats meta we don't understand: {} in Project: {}", note, project.saveFile);
            }
        }
    }

    public void accessorise(ArrayList<HatInfo> newAccessories)
    {
        //take the accessories if the parent is null or is our name. The base calls this first, and will take all the null ones. We then pass on the list to the children to nab as they need.
        accessories.addAll(newAccessories.stream().filter(info -> info.accessoryParent == null || info.accessoryParent.equals(name)).collect(Collectors.toList()));
        newAccessories.removeAll(accessories);

        if(forcedWorth >= 0)
        {
            worth = forcedWorth;
        }

        for(HatInfo accessory : accessories)
        {
            accessory.accessorise(newAccessories);
        }
    }

    public String getDisplayNameFor(String accessoryName)
    {
        HatInfo info = getInfoFor(accessoryName);
        return info != null ? info.getDisplayName() : null;
    }

    public HatInfo getInfoFor(String accessoryName)
    {
        if(accessoryName.equals(name))
        {
            return this;
        }
        else
        {
            for(HatInfo accessory : accessories)
            {
                HatInfo s = accessory.getInfoFor(accessoryName);
                if(s != null)
                {
                    return s;
                }
            }
        }
        return null;
    }

    public int getWorthFor(String accessoryName, int bonus)
    {
        if(accessoryName.equals(name))
        {
            return getWorth() + bonus;
        }
        else
        {
            for(HatInfo accessory : accessories)
            {
                int s = accessory.getWorthFor(accessoryName, getWorth() + bonus);
                if(s > 0)
                {
                    return s;
                }
            }
        }
        return -1;
    }

    public HatsSavedData.HatPart getAsHatPart(int count)
    {
        HatsSavedData.HatPart part = new HatsSavedData.HatPart(name);
        part.isShowing = true;
        part.count = count;
        for(HatInfo accessory : accessories)
        {
            HatsSavedData.HatPart childPart = accessory.getAsHatPart(count);
            childPart.isShowing = false; //the function sets is as true
            part.hatParts.add(childPart);
        }
        return part;
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

            HatHandler.RAND.setSeed(Math.abs((Hats.configServer.randSeed + ent.getUniqueID() + accessory.getFullName()).hashCode()) * 53579997854L); //Chat contributed random
            if(!ent.canChangeDimension()) //TODO Old isNonBoss(). Needs a new config or maybe set it in the JSON
            {
                accChance += Hats.configServer.bossRarityBonus;
            }

            if(HatHandler.RAND.nextDouble() < accChance) //spawn the accessory
            {
                spawningAccessories.add(accessory);
                if(!accessory.accessoryLayer.isEmpty()) //look for conflicts for accessories  that already got to spawn
                {
                    for(String layer : accessory.accessoryLayer)
                    {
                        conflicts.computeIfAbsent(layer, k -> new ArrayList<>()).add(accessory);
                    }
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
            accToSpawn.isShowing = true;
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

        hidden = part.count < 0 || !part.isShowing;
        for(int i = 0; i < part.colouriser.length; i++)
        {
            colouriser[i] = 1F - part.colouriser[i];
        }
    }

    public float[] getDimensions() //gets y min + y-max + width
    {
        if(hidden)
        {
            return new float[] { 0F, 0F, 0F, 0F, 0F, 0F };
        }

        float x1 = 10000;
        float x2 = -10000;
        float z1 = 10000;
        float z2 = -10000;

        float y1 = 10000;
        float y2 = -10000;


        for(Project.Part part : project.getAllParts())
        {
            for(Project.Part.Box box : part.boxes)
            {
                if(part.rotPX + box.posX < x1) //lowest point
                {
                    x1 = part.rotPX + box.posX;
                }
                if(part.rotPX + box.posX + box.dimX > x2) //highest point
                {
                    x2 = part.rotPX + box.posX + box.dimX;
                }
                if(part.rotPZ + box.posZ < z1) //lowest point
                {
                    z1 = part.rotPZ + box.posZ;
                }
                if(part.rotPZ + box.posZ + box.dimZ > z2) //highest point
                {
                    z2 = part.rotPZ + box.posZ + box.dimZ;
                }

                if(part.rotPY + box.posY < y1) //lowest point
                {
                    y1 = part.rotPY + box.posY;
                }
                if(part.rotPY + box.posY + box.dimY > y2) //highest point
                {
                    y2 = part.rotPY + box.posY + box.dimY;
                }
            }
        }

        float[] dims = new float[] { x1, x2, y1, y2, z1, z2 };

        for(HatInfo accessory : accessories)
        {
            float[] accDims = accessory.getDimensions();

            for(int i = 0; i < accDims.length; i++)
            {
                if(i % 2 == 0)
                {
                    if(accDims[i] < dims[i])
                    {
                        dims[i] = accDims[i];
                    }
                }
                else
                {
                    if(accDims[i] > dims[i])
                    {
                        dims[i] = accDims[i];
                    }
                }
            }
        }

        return dims;
    }
}
