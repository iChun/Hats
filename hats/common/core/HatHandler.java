package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

public class HatHandler 
{

	public static void requestHat(String name)
	{
		
	}
	
	public static void sendHat(String hatName, EntityPlayer player)
	{
		File file = null;
		
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(hatName))
			{
				file = e.getKey();
			}
		}

		if(file != null)
		{
			try
			{
				
				int fileSize = (int)file.length();
				
				if(fileSize > 100000)
				{
					Hats.console("Unable to send " + file.getName() + ". It is above the size limit!", true);
				}
				
				FileInputStream fis = new FileInputStream(file);
				int packetCount = 0;
				
				while(fileSize > 0)
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream = new DataOutputStream(bytes);
	
			        stream.writeByte(1); //id
			        stream.writeUTF(hatName); //name
			        stream.writeByte((int)Math.ceil((float)fileSize / 32000F)); //number of overall packets to send
			        stream.writeInt(packetCount); // packet number being sent
			        stream.writeInt(fileSize > 32000 ? 32000 : fileSize); //byte size
			        
			        byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
			        fis.read(fileBytes);
			        stream.write(fileBytes); //hat info

			        packetCount++;
			        fileSize -= 32000;
			        
			        if(player != null)
			        {
			        	PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
			        }
			        else
			        {
			        	PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("Hats", bytes.toByteArray()));
			        }
				}
				
				fis.close();
			}
			catch(Exception e)
			{
				
			}
			
			//byte size should be 32000
		}
	}
	
	public static boolean hasHat(String name)
	{
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public static File hatsFolder;
	
	public static HashMap<String, ArrayList<byte[][]>> hatParts = new HashMap<String, ArrayList<byte[][]>>();
	
	public static HashMap<File, String> hatNames = new HashMap<File, String>();

}
