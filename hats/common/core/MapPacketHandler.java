package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MapPacketHandler
	implements ITinyPacketHandler
{
	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) 
	{
		int id = mapData.uniqueID;
		if(handler instanceof NetServerHandler)
		{
			handleServerPacket((NetServerHandler)handler, mapData.uniqueID, mapData.itemData, (EntityPlayerMP)handler.getPlayer());
		}
		else
		{
			handleClientPacket((NetClientHandler)handler, mapData.uniqueID, mapData.itemData);
		}
	}

	public void handleServerPacket(NetServerHandler handler, short id, byte[] data, EntityPlayerMP player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					String hatName = stream.readUTF();
					
					if(HatHandler.hasHat(hatName))
					{
						Hats.proxy.playerWornHats.put(player.username, hatName);
						
						Hats.proxy.saveData(DimensionManager.getWorld(0));
						
						Hats.proxy.sendPlayerListOfWornHats(player, false);
					}
					else
					{
						HatHandler.requestHat(hatName, player);
					}
					
					break;
				}
				case 1:
				{
					String hatName = stream.readUTF();
					
					HatHandler.sendHat(hatName, player);
					
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}

	//TODO Side Split
	
	@SideOnly(Side.CLIENT)
	public void handleClientPacket(NetClientHandler handler, short id, byte[] data)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 1:
				{
					String hatName = stream.readUTF();
					
					HatHandler.sendHat(hatName, null);
					
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
	
}
