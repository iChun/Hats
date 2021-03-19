package me.ichun.mods.hats.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketRehatify;
import me.ichun.mods.hats.common.world.HatsSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.*;
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
                        .then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
                            .executes(context -> setHat(context.getSource(), EntityArgument.getEntities(context, "targets"), NBTCompoundTagArgument.getNbt(context, "nbt"), true))
                        )
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
//            .then(Commands.literal("setCount")
//                .then(Commands.argument("player", EntityArgument.player()))
//            )
//            .then(Commands.literal("remove")
//                .then(Commands.argument("player", EntityArgument.player()))
//            )
//            .then(Commands.literal("clear")
//                .then(Commands.argument("player", EntityArgument.player()))
//            )
//            .then(Commands.literal("setToken")
//                .then(Commands.argument("player", EntityArgument.player()))
//            )
        );
    }

    public static int setHat(CommandSource source, Collection<? extends Entity> targets, CompoundNBT nbt, boolean silent)
    {
        ArrayList<Entity> entities = targets.stream().filter(e -> e instanceof LivingEntity).collect(Collectors.toCollection(ArrayList::new));
        HatsSavedData.HatPart newPart = new HatsSavedData.HatPart();
        newPart.read(nbt);
        if(HatResourceHandler.HATS.containsKey(newPart.name))
        {
            newPart.count = 1;
            newPart.isShowing = true;
        }

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

    public static int getHat(CommandSource source, Entity entity)
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
        return 1;
    }
}
