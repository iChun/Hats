package hats.common.core;

import hats.common.Hats;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

public class CommandHats extends CommandBase {

	@Override
	public String getCommandName() 
	{
		return "hats";
	}
	
	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/" + this.getCommandName() + "           type /hats for full list.";
    }
	
	@Override
    public List getCommandAliases()
    {
		return Arrays.asList(new String[] {"hat"});
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
					icommandsender.sendChatToPlayer("\u00A7c/hats send <player> <hat name>  Send a hat to player.");
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A7c/hats set <player> <hat name>   Set a player hat.");
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A7c/hats unlock <player> <hat name>   Unlocks a hat for a player.");
				}
			}
			else if(astring.length == 2)
			{
				if("send".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A7c/hats send <player> <hat name>  Send a hat to player.");
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A7c/hats set <player> <hat name>   Set a player hat.");
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A7c/hats unlock <player> <hat name>   Unlocks a hat for a player.");
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
					icommandsender.sendChatToPlayer("\u00A7c" + playerName + " is not online!");
					return;
				}
				if(!HatHandler.hasHat(hatName))
				{
					icommandsender.sendChatToPlayer("\u00A7c" + hatName + " does not exist!");
					return;
				}
				
				if("send".startsWith(command.toLowerCase()))
				{
					if(Hats.allowSendingOfHats == 0)
					{
						icommandsender.sendChatToPlayer("\u00A7c" + "Server has disabled sending hats!");
						return;
					}
					icommandsender.sendChatToPlayer("\u00A77" + "Sending " + hatName + " to " + player.username);
					HatHandler.sendHat(hatName, player);
				}
				else if("set".startsWith(command.toLowerCase()))
				{
					icommandsender.sendChatToPlayer("\u00A77" + "Setting " + hatName + " on " + player.username);
					Hats.proxy.playerWornHats.put(player.username, new HatInfo(hatName.toLowerCase(), 255, 255, 255));
					Hats.proxy.sendPlayerListOfWornHats(player, false, false);
				}
				else if("unlock".startsWith(command.toLowerCase()))
				{
					if(Hats.playerHatsMode == 4)
					{
						if(player.capabilities.isCreativeMode)
						{
							icommandsender.sendChatToPlayer("\u00A77" + player.username + " is in creative!");
						}
						else
						{
							icommandsender.sendChatToPlayer("\u00A77" + "Unlocking " + hatName + " for " + player.username);
							Hats.console(icommandsender.getCommandSenderName() + " unlocked " + hatName + " for " + player.username);
							HatHandler.unlockHat(player, hatName);
						}
					}
					else
					{
						icommandsender.sendChatToPlayer("\u00A77" + "Server is not in Hat Hunting Mode!");
					}
				}
			}
			return;
		}
		else
		{
			throw new WrongUsageException(getUsageString(), new Object[0]);
		}

	}

	@Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] args)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"set", "send", "unlock"}) : args.length == 2 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : args.length == 3 ? getListOfStringsMatchingLastWord(args, HatHandler.getAllHatsAsArray()) : null;
    }
	
	public String getUsageString()
	{
		return " Hats commands\n" +
				"/hats set <player> <hat name>      Set a player hat.\n" +
				"/hats send <player> <hat name>    Send a hat to player.\n" +
				"/hats unlock <player> <hat name>  Unlock a hat for a player.";
	}

}
