package hats.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import hats.common.Hats;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import java.util.Arrays;
import java.util.List;

public class CommandHats extends CommandBase {

	@Override
	public String getCommandName() 
	{
		return "hats";
	}
	
	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/" + this.getCommandName() + "           " + StatCollector.translateToLocal("hats.command.help");
    }
	
	@Override
    public List getCommandAliases()
    {
		return Arrays.asList("hat");
    }

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) 
	{
		if(astring.length > 0)
		{
			String command = astring[0];
			
			if(astring.length == 1)
			{
				if("send".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.send")));
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.set")));
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.unlock")));
				}
			}
			else if(astring.length == 2)
			{
				if("send".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.send")));
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.set")));
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.help.unlock")));
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
				EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(playerName);
				if(player == null)
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocalFormatted("hats.command.notOnline", playerName)));
					return;
				}
				if(!HatHandler.hasHat(hatName))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocalFormatted("hats.command.hatDoesNotExist", hatName)));
					return;
				}

				if("send".startsWith(command.toLowerCase()))
				{
					if(Hats.config.getInt("allowSendingOfHats") == 0)
					{
						icommandsender.addChatMessage(new ChatComponentTranslation("\u00A7c" + StatCollector.translateToLocal("hats.command.serverDisabledHatSending")));
						return;
					}
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A77" + StatCollector.translateToLocalFormatted("hats.command.sendToPlayer", hatName, player.getCommandSenderName())));
					HatHandler.sendHat(hatName, player);
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.addChatMessage(new ChatComponentTranslation("\u00A77" + StatCollector.translateToLocalFormatted("hats.command.setPlayerHat", hatName, player.getCommandSenderName())));
					Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo(hatName.toLowerCase(), 255, 255, 255, 255));
					Hats.proxy.sendPlayerListOfWornHats(player, false, false);
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					if(Hats.config.getSessionInt("playerHatsMode") >= 4)
					{
						if(player.capabilities.isCreativeMode)
						{
							icommandsender.addChatMessage(new ChatComponentTranslation("\u00A77" + StatCollector.translateToLocalFormatted("hats.command.playerIsInCreative", player.getCommandSenderName())));
						}
						else
						{
							icommandsender.addChatMessage(new ChatComponentTranslation("\u00A77" + StatCollector.translateToLocalFormatted("hats.command.unlockHatForPlayer", hatName, player.getCommandSenderName())));
							Hats.console(StatCollector.translateToLocalFormatted("hats.command.adminNotify.unlockHatForPlayer", icommandsender.getCommandSenderName(), hatName, player.getCommandSenderName()));
							HatHandler.unlockHat(player, hatName);
						}
					}
					else
					{
						icommandsender.addChatMessage(new ChatComponentTranslation("\u00A77" + StatCollector.translateToLocal("hats.command.serverIsNotOnHatHuntingMode")));
					}
				}
			}
        }
		else
		{
			throw new WrongUsageException(getUsageString());
		}

	}

	@Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] args)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "set", "send", "unlock") : args.length == 2 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : args.length == 3 ? getListOfStringsMatchingLastWord(args, HatHandler.getAllHatsAsArray()) : null;
    }
	
	public String getUsageString()
	{
		return StatCollector.translateToLocal("hats.command") + " \n" +
				StatCollector.translateToLocal("hats.command.help.send") + " \n" +
				StatCollector.translateToLocal("hats.command.help.set") + " \n" +
				StatCollector.translateToLocal("hats.command.help.unlock");
	}

}
