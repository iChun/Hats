package hats.addons.hatstand.common.tileentity;

import hats.client.core.HatInfoClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	
    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
    	readFromNBT(pkt.data);
    	
    	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
	
    @Override
    public Packet getDescriptionPacket()
    {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tag);
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
	
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
	
}
