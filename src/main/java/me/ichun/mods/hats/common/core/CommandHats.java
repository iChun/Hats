package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collections;
import java.util.List;

public class CommandHats extends CommandBase {
	@Override
	public String getName() {
		return "hats";
	}

	@Override
    public String getUsage(ICommandSender par1ICommandSender)
    {
        return "/" + this.getName() + "           " + I18n.translateToLocalFormatted("hats.command.help");
    }

	@Override
    public List getAliases()
    {
		return Collections.singletonList("hat");
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender icommandsender, String[] astring) throws CommandException
	{
		if(astring.length > 0)
		{
			String command = astring[0];
			ITextComponent component = null;

			if(astring.length == 1 || astring.length == 2)
			{
				if("send".startsWith(command.toLowerCase()))
				{
					component = new TextComponentString(TextFormatting.RED + I18n.translateToLocal("hats.command.help.send"));
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					component = new TextComponentString(TextFormatting.RED + I18n.translateToLocal("hats.command.help.set"));
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					component = new TextComponentString(TextFormatting.RED + I18n.translateToLocal("hats.command.help.unlock"));
				}
			}
			else if(astring.length >= 3)
			{
				String playerName = astring[1];
				StringBuilder sb = new StringBuilder();

				for(int i = 2; i < astring.length; i++)
				{
					sb.append(astring[i]);
					sb.append(" ");
				}
				String hatName = HatHandler.getHatStartingWith(sb.toString().trim());
				EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
				if(player == null)
				{
					icommandsender.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("hats.command.notOnline", playerName)));
					return;
				}
				if(!HatHandler.hasHat(hatName))
				{
					icommandsender.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("hats.command.hatDoesNotExist", hatName)));
					return;
				}

				if("send".startsWith(command.toLowerCase()))
				{
					if(Hats.config.allowSendingOfHats == 0)
					{
						icommandsender.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("hats.command.serverDisabledHatSending")));
						return;
					}
					icommandsender.sendMessage(new TextComponentString(TextFormatting.GRAY + I18n.translateToLocalFormatted("hats.command.sendToPlayer", hatName, player.getName())));
					HatHandler.sendHat(hatName, player);
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.sendMessage(new TextComponentString(TextFormatting.GRAY + I18n.translateToLocalFormatted("hats.command.setPlayerHat", hatName, player.getName())));
					Hats.proxy.playerWornHats.put(player.getName(), new HatInfo(hatName.toLowerCase(), 255, 255, 255, 255));
					Hats.proxy.sendPlayerListOfWornHats(player, false, false);
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					if(Hats.config.playerHatsMode >= 4)
					{
						if(player.capabilities.isCreativeMode)
						{
							icommandsender.sendMessage(new TextComponentString(TextFormatting.GRAY + I18n.translateToLocalFormatted("hats.command.playerIsInCreative", player.getName())));
						}
						else
						{
							icommandsender.sendMessage(new TextComponentString(TextFormatting.GRAY + I18n.translateToLocalFormatted("hats.command.unlockHatForPlayer", hatName, player.getName())));
							Hats.console(I18n.translateToLocalFormatted("hats.command.adminNotify.unlockHatForPlayer", icommandsender.getCommandSenderEntity().getName(), hatName, player.getName()));
							HatHandler.unlockHat(player, hatName);
						}
					}
					else
					{
						icommandsender.sendMessage(new TextComponentString(TextFormatting.GRAY + I18n.translateToLocalFormatted("hats.command.serverIsNotOnHatHuntingMode")));
					}
				}
			}
			if(component != null)
			{
				icommandsender.sendMessage(component);
			}
        }
		else
		{
			throw new WrongUsageException(getUsageString());
		}

	}

	@Override
    public List getTabCompletions(MinecraftServer server, ICommandSender par1ICommandSender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "set", "send", "unlock") : args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : args.length == 3 ? getListOfStringsMatchingLastWord(args, HatHandler.getAllHatsAsArray()) : null;
    }

	public String getUsageString()
	{
		return I18n.translateToLocal("hats.command") + " \n" +
				I18n.translateToLocal("hats.command.help.send") + " \n" +
				I18n.translateToLocal("hats.command.help.set") + " \n" +
				I18n.translateToLocal("hats.command.help.unlock");
	}

}
