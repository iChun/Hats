package hats.addons.hatstand.common.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.client.core.HatInfoClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityHatStand extends TileEntity 
{

	public boolean hasBase;
	public int head;
	public String headName;
	public boolean hasStand;
	public boolean isOnFloor;
	public int orientation;
	public int sideOn;
	
	public String hatName;
	public int colourR;
	public int colourG;
	public int colourB;
	
	public static final String[] headNames = new String[] { "None", "Skeleton", "W. Skele", "Zombie", "Player", "Steve", "Creeper", "Wither", "Wither (I)", "Pigman", "Blaze", "Invisible" };
	
	public HatInfoClient info;
	
	public TileEntityHatStand()
	{
		hasBase = true;
		head = 0;
		headName = "";
		hasStand = false;
		isOnFloor = true;
		orientation = 0;
		sideOn = 1;
		colourR = 255;
		colourG = 255;
		colourB = 255;
		hatName = "".toLowerCase();
		
		info = null;
	}

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	readFromNBT(pkt.func_148857_g());
    	
    	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
	
    @Override
    public Packet getDescriptionPacket()
    {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
    	super.writeToNBT(tag);
    	tag.setBoolean("hasBase", hasBase);
    	tag.setInteger("head", head);
    	tag.setString("headName", headName);
    	tag.setBoolean("hasStand", hasStand);
    	tag.setBoolean("isOnFloor", isOnFloor);
    	tag.setInteger("orientation", orientation);
    	tag.setInteger("sideOn", sideOn);
    	
    	tag.setString("hatName", hatName);
    	tag.setInteger("colourR", colourR);
    	tag.setInteger("colourG", colourG);
    	tag.setInteger("colourB", colourB);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
		hasBase = tag.getBoolean("hasBase");
		head = tag.getInteger("head");
		headName = tag.getString("headName");
		hasStand = tag.getBoolean("hasStand");
		isOnFloor = tag.getBoolean("isOnFloor");
		orientation = tag.getInteger("orientation");
		sideOn = tag.getInteger("sideOn");
		
		hatName = tag.getString("hatName");
		colourR = tag.getInteger("colourR");
		colourG = tag.getInteger("colourG");
		colourB = tag.getInteger("colourB");
		
		info = new HatInfoClient(hatName, colourR, colourG, colourB);
    }

    //TODO do not use getAABBPool!
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
	
}
