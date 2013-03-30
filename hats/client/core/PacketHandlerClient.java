package hats.client.core;

import hats.common.Hats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient
	implements IPacketHandler 
{
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		Minecraft mc = Minecraft.getMinecraft();
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					Hats.proxy.tickHandlerClient.serverHasMod = true;
					Hats.proxy.tickHandlerClient.serverHatMode = stream.readByte();
					
					String availHats = stream.readUTF(); //ignored on Free Mode
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}		
}
