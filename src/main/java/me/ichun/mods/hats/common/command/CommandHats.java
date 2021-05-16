package me.ichun.mods.hats.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketRehatify;
import me.ichun.mods.hats.common.packet.PacketUpdateHats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CommandHats
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("hats").requires(p -> p.hasPermissionLevel(2)) //set to permission level 2
                        .then(Commands.literal("setHat")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.literal("silent")
                                                .then(Commands.argument("names", HatInfosArgument.multi())
                                                        .executes(context -> setHat(context.getSource(), EntityArgument.getEntities(context, "targets"), HatInfosArgument.getHatInfos(context, "names"), true))
                                                )
                                                .then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
                                                        .executes(context -> setHat(context.getSource(), EntityArgument.getEntities(context, "targets"), NBTCompoundTagArgument.getNbt(context, "nbt"), true))
                                                )
                                        )
                                        .then(Commands.argument("names", HatInfosArgument.multi())
                                                .executes(context -> setHat(context.getSource(), EntityArgument.getEntities(context, "targets"), HatInfosArgument.getHatInfos(context, "names"), false))
                                        )
                                        .then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
                                                .executes(context -> setHat(context.getSource(), EntityArgument.getEntities(context, "targets"), NBTCompoundTagArgument.getNbt(context, "nbt"), false))
                                        )
                                )
                        )
                        .then(Commands.literal("getHat")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(context -> getHat(context.getSource(), EntityArgument.getEntity(context, "target")))
                                )
                        )
                        .then(Commands.literal("addCount")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("names", HatInfosArgument.single())
                                                .then(Commands.argument("count", IntegerArgumentType.integer(-999999999, 999999999))
                                                        .executes(context -> addCount(context.getSource(), EntityArgument.getPlayer(context, "player"), HatInfosArgument.getHatInfos(context, "names"), IntegerArgumentType.getInteger(context, "count")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("names", HatInfosArgument.single())
                                                .executes(context -> remove(context.getSource(), EntityArgument.getPlayer(context, "player"), HatInfosArgument.getHatInfos(context, "names")))
                                        )
                                )
                        )
                        .then(Commands.literal("clear")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> clear(context.getSource(), EntityArgument.getPlayer(context, "player"))))
                        )
                        .then(Commands.literal("reextract")
                                .then(Commands.literal("heads")
                                        .executes(context -> {
                                            try
                                            {
                                                int i = HeadHandler.extractFiles(true);
                                                HeadHandler.loadHeadInfos();

                                                context.getSource().sendFeedback(new TranslationTextComponent("commands.hats.reextract.success", i), true);
                                            }
                                            catch(Throwable e)
                                            {
                                                Hats.LOGGER.error("Error reextracting heads.");
                                                context.getSource().sendFeedback(new TranslationTextComponent("commands.hats.reextract.failed"), true);
                                                e.printStackTrace();
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("hats")
                                        .executes(context -> {
                                            try
                                            {
                                                int i = HatResourceHandler.extractHats(true);
                                                HatResourceHandler.loadAllHats();
                                                HatHandler.allocateHatPools();

                                                context.getSource().sendFeedback(new TranslationTextComponent("commands.hats.reextract.success", i), true);
                                            }
                                            catch(Throwable e)
                                            {
                                                Hats.LOGGER.error("Error reextracting hats.");
                                                context.getSource().sendFeedback(new TranslationTextComponent("commands.hats.reextract.failed"), true);
                                                e.printStackTrace();
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                //            .then(Commands.literal("setToken") //TODO this during the trading update
                //                .then(Commands.argument("player", EntityArgument.player()))
                //            )
        );
    }

    private static HatsSavedData.HatPart getAsPart(ArrayList<HatInfo> hatInfos, int count)
    {
        ArrayList<String> names = new ArrayList<>();
        for(int i = 1; i < hatInfos.size(); i++)
        {
            names.add(hatInfos.get(i).name);
        }

        return hatInfos.get(0).getFromList(names, count);
    }

    private static int setHat(CommandSource source, Collection<? extends Entity> targets, ArrayList<HatInfo> hatInfos, boolean silent)
    {
        return setHat(source, targets, getAsPart(hatInfos, 1), silent);
    }

    private static int setHat(CommandSource source, Collection<? extends Entity> targets, CompoundNBT nbt, boolean silent)
    {
        HatsSavedData.HatPart newPart = new HatsSavedData.HatPart();
        newPart.read(nbt);
        if(HatResourceHandler.HATS.containsKey(newPart.name))
        {
            newPart.count = 1;
            newPart.isShowing = true;
        }

        return setHat(source, targets, newPart, silent);
    }

    private static int setHat(CommandSource source, Collection<? extends Entity> targets, HatsSavedData.HatPart newPart, boolean silent)
    {
        ArrayList<Entity> entities = targets.stream().filter(e -> e instanceof LivingEntity).collect(Collectors.toCollection(ArrayList::new));
        for(Entity entity : entities)
        {
            HashMap<Integer, HatsSavedData.HatPart> entIdToHat = new HashMap<>();

            HatsSavedData.HatPart part = HatHandler.getHatPart(((LivingEntity)entity));
            part.copy(newPart);

            entIdToHat.put(entity.getEntityId(), part);

            if(!silent)
            {
                Hats.channel.sendTo(new PacketRehatify(entity.getEntityId()), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
            }

            Hats.channel.sendTo(new PacketEntityHatDetails(entIdToHat), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
        }

        source.sendFeedback(new TranslationTextComponent("commands.hats.setHat.success", entities.size()), true);

        return entities.size();
    }

    private static int getHat(CommandSource source, Entity entity)
    {
        if(entity instanceof LivingEntity || entity instanceof EntityHat)
        {
            CompoundNBT nbt = (entity instanceof EntityHat ? ((EntityHat)entity).hatPart : HatHandler.getHatPart((LivingEntity)entity)).write(new CompoundNBT());

            String nbtS = nbt.toString();
            StringTextComponent nbtStr = new StringTextComponent(nbtS);
            ClickEvent click = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbtS);
            nbtStr.setStyle(nbtStr.getStyle().setClickEvent(click).setUnderlined(true));

            source.sendFeedback(new TranslationTextComponent("commands.hats.getHat.value").appendSibling(nbtStr), true);
        }
        else
        {
            source.sendFeedback(new TranslationTextComponent("commands.hats.getHat.fail"), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addCount(CommandSource source, ServerPlayerEntity player, ArrayList<HatInfo> hatInfos, int count)
    {
        HatsSavedData.HatPart hatPart = getAsPart(hatInfos, count);
        if(hatInfos.size() > 1)
        {
            hatPart.count = 0;
        }

        HatHandler.addHat(player, hatPart);

        source.sendFeedback(new TranslationTextComponent("commands.hats.addCount.success", hatInfos.get(hatInfos.size() - 1).name, player.getName()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandSource source, ServerPlayerEntity player, ArrayList<HatInfo> hatInfos)
    {
        boolean flag = false;

        HatsSavedData.HatPart hatPart = getAsPart(hatInfos, 0);
        ArrayList<HatsSavedData.HatPart> playerInventory = HatHandler.getPlayerInventory(player);
        if(hatInfos.size() > 1) //removing accessory
        {
            for(HatsSavedData.HatPart part : playerInventory)
            {
                if(part.remove(new HatsSavedData.HatPart(hatInfos.get(hatInfos.size() - 1).name)))
                {
                    flag = true;
                    break;
                }
            }
        }
        else
        {
            if(playerInventory.removeIf(part -> hatPart.name.equals(part.name)))
            {
                flag = true;
            }
        }

        if(flag)
        {
            HatHandler.markSaveDirty();
            source.sendFeedback(new TranslationTextComponent("commands.hats.remove.success", hatInfos.get(hatInfos.size() - 1).name, player.getName()), true);
            Hats.channel.sendTo(new PacketUpdateHats(HatHandler.getPlayerHatsNBT(player), true), player);
        }
        else
        {
            source.sendFeedback(new TranslationTextComponent("commands.hats.remove.fail", hatInfos.get(hatInfos.size() - 1).name, player.getName()), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandSource source, ServerPlayerEntity player)
    {
        ArrayList<HatsSavedData.HatPart> playerInventory = HatHandler.getPlayerInventory(player);
        playerInventory.clear();
        HatHandler.markSaveDirty();
        source.sendFeedback(new TranslationTextComponent("commands.hats.clear.success",player.getName()), true);
        Hats.channel.sendTo(new PacketUpdateHats(HatHandler.getPlayerHatsNBT(player), true), player);
        return Command.SINGLE_SUCCESS;
    }
}
