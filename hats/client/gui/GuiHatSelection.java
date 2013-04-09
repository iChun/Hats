package hats.client.gui;

import hats.common.Hats;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiHatSelection extends GuiScreen 
	implements ISlider
{

	private final int ID_PAGE_LEFT = 1;
	private final int ID_DONE_SELECT = 2;
	private final int ID_PAGE_RIGHT = 3;
	private final int ID_NONE = 4;
	// 5 6 7 are slider IDs
	private final int ID_HAT_COLOUR_SWAP = 8;
	private final int ID_CLOSE = 9;
	private final int ID_RANDOM = 10;
	private final int ID_HELP = 11;
	private final int ID_RELOAD_HATS = 12;
	
	private final int ID_HAT_START_ID = 20;
	
	private final int VIEW_HATS = 0;
	private final int VIEW_COLOURIZER = 1;
	
	public EntityPlayer player;
	public EntityHat hat;
	public List availableHats;
	
	protected int xSize = 176;
	protected int ySize = 170;
	
	public float mouseX;
	public float mouseY;
	
	protected int guiLeft;
	protected int guiTop;
	
	public int pageNumber;
	public int colourR;
	public int colourG;
	public int colourB;
	
	private String prevHatName;
	private int prevColourR;
	private int prevColourG;
	private int prevColourB;
	
	public int view;
	
	public Random rand;
	
	public GuiHatSelection(EntityPlayer ply)
	{
		player = ply;
		hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
		availableHats = ImmutableList.copyOf(Hats.proxy.tickHandlerClient.availableHats);
		prevHatName = hat.hatName;
		prevColourR = colourR = hat.getR();
		prevColourG = colourG = hat.getG();
		prevColourB = colourB = hat.getB();
		pageNumber = 0;
		view = VIEW_HATS;
		rand = new Random();
	}
	
	@Override
	public void initGui()
	{
		if(hat == null || player == null)
		{
			mc.displayGuiScreen(null);
		}
		else
		{
			buttonList.clear();
			
	        this.guiLeft = (this.width - this.xSize) / 2;
	        this.guiTop = (this.height - this.ySize) / 2;
	        
	        buttonList.add(new GuiButton(ID_PAGE_LEFT, width / 2 - 6, height / 2 + 54, 20, 20, "<"));
	        buttonList.add(new GuiButton(ID_PAGE_RIGHT, width / 2 + 62, height / 2 + 54, 20, 20, ">"));
	        buttonList.add(new GuiButton(ID_DONE_SELECT, width / 2 + 16, height / 2 + 54, 44, 20, "Done"));
	        
	        //4, 5, 6, 7 = taken.
	        
	        buttonList.add(new GuiButton(ID_NONE, width / 2 + 89, height / 2 - 85, 20, 20, "N"));
	        buttonList.add(new GuiButton(ID_HAT_COLOUR_SWAP, width / 2 + 89, height / 2 - 85 + (1 * 22), 20, 20, "C"));
	        buttonList.add(new GuiButton(ID_RANDOM, width / 2 + 89, height / 2 - 85 + (2 * 22), 20, 20, ""));
	        buttonList.add(new GuiButton(ID_RELOAD_HATS, width / 2 + 89, height / 2 - 85 + (4 * 22), 20, 20, ""));
	        buttonList.add(new GuiButton(ID_HELP, width / 2 + 89, height / 2 - 85 + (4 * 22), 20, 20, ""));
	        
	        buttonList.add(new GuiButton(ID_CLOSE, width - 22, 2, 20, 20, "X"));
	        
	        pageNumber = 0;
	        
	        if(hat.hatName.equalsIgnoreCase(""))
	        {
	        	int i = availableHats.size();
	    		i -= i % 6;
	    		pageNumber = i / 6;
	        }
	        else
	        {
		        for(int i = 0; i < availableHats.size(); i++)
		        {
		        	String hatName = (String)availableHats.get(i);
		        	if(hatName.equalsIgnoreCase(hat.hatName))
		        	{
		        		i -= i % 6;
		        		pageNumber = i / 6;
		        		break;
		        	}
		        }
	        }
	        
	        updateButtonList();
		}
	}
	
    @Override
    protected void keyTyped(char c, int i)
    {
        if (i == 1)
        {
        	exitWithoutUpdate();
        	
            this.mc.setIngameFocus();
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton btn)
    {
    	if(btn.id == ID_PAGE_LEFT)
    	{
    		pageNumber--;
    		if(pageNumber < 0)
    		{
    			pageNumber = 0;
    		}
    		updateButtonList();
    	}
    	else if(btn.id == ID_PAGE_RIGHT)
    	{
    		pageNumber++;
    		if(pageNumber * 6 >= availableHats.size())
    		{
    			pageNumber--;
    		}
    		updateButtonList();
    	}
    	else if(btn.id == ID_DONE_SELECT)
    	{
    		exitAndUpdate();
    	}
    	else if(btn.id == ID_NONE)
    	{
    		hat.hatName = "";
    		
    		updateButtonList();
    	}
    	else if(btn.id == ID_HAT_COLOUR_SWAP)
    	{
    		view = view == VIEW_COLOURIZER ? VIEW_HATS : VIEW_COLOURIZER;
    		btn.displayString = view == VIEW_COLOURIZER ? "H" : "C";
    		
    		updateButtonList();
    	}
    	else if(btn.id == ID_CLOSE)
    	{
    		exitWithoutUpdate();
    	}
    	else if(btn.id == ID_RANDOM)
    	{
    		if(view == VIEW_HATS)
    		{
    			int randVal = rand.nextInt(availableHats.size());
	        	String hatName = (String)availableHats.get(randVal);
	        	
	        	hat.hatName = hatName.toLowerCase();
	        	
	    		colourR = colourG = colourB = 255;
	    		hat.setR(255);
	    		hat.setG(255);
	    		hat.setB(255);
	        	
        		pageNumber = randVal / 6;
        		
        		updateButtonList();
    		}
    		else if(view == VIEW_COLOURIZER)
    		{
    			for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
    			{
    				GuiButton btn1 = (GuiButton)this.buttonList.get(k1);
    				if(btn1 instanceof GuiSlider)
    				{
    					GuiSlider slider = (GuiSlider)btn1;
    					if(slider.id >= 5 && slider.id <= 7)
    					{
    						if(hat.hatName.equalsIgnoreCase(""))
    						{
    							slider.sliderValue = 0.0F;
    						}
    						else
    						{
    							slider.sliderValue = rand.nextFloat();
    						}
    						slider.updateSlider();
    					}
    				}
    			}
    		}
    	}
    	else if(btn.id == ID_RELOAD_HATS)
    	{
			for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
			{
				GuiButton btn1 = (GuiButton)this.buttonList.get(k1);
				if((btn1 instanceof GuiSlider) || btn1.id == ID_CLOSE)
				{
					continue;
				}
				btn1.enabled = false;
			}    		
    		Hats.proxy.getHatsAndOpenGui();
    	}
    	else if(btn.id >= ID_HAT_START_ID)
    	{
    		hat.hatName = btn.displayString.toLowerCase();
    		
    		colourR = colourG = colourB = 255;
    		hat.setR(255);
    		hat.setG(255);
    		hat.setB(255);
    		
    		updateButtonList();
    	}
    }
    
    public void exitAndUpdate()
    {
		mc.displayGuiScreen(null);
		
		if(!Hats.proxy.tickHandlerClient.serverHasMod)
		{
    		Hats.favouriteHat = hat.hatName;
    		
    		String r = Integer.toHexString(colourR);
    		String b = Integer.toHexString(colourG);
    		String g = Integer.toHexString(colourB);
    		
    		if(r.length() < 2)
    		{
    			r = "0" + r;
    		}
    		if(g.length() < 2)
    		{
    			g = "0" + g;
    		}
    		if(b.length() < 2)
    		{
    			b = "0" + b;
    		}
    		
    		String name = "#" + r + g + b; 
    		
    		Hats.favouriteHatColourizer = name;
    		
    		Hats.favouriteHatInfo = new HatInfo(hat.hatName, colourR, colourG, colourB);
    		
    		Hats.handleConfig();
		}
		else if(!(player == null || player.isDead || !player.isEntityAlive()))
		{
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bytes);

	        try
	        {
	        	stream.writeUTF(hat.hatName);
	        	stream.writeInt(colourR); //R
	        	stream.writeInt(colourG); //G
	        	stream.writeInt(colourB); //B
	        	
	        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)0, bytes.toByteArray()));
	        }
	        catch(IOException e)
	        {}
		}
    }
    
    public void exitWithoutUpdate()
    {
		mc.displayGuiScreen(null);
    	
		hat.hatName = prevHatName;
		hat.setR(prevColourR);
		hat.setG(prevColourG);
		hat.setB(prevColourB);
    }
    
    public void updateButtonList()
    {
        for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            if(btn.id >= ID_HAT_START_ID || btn.id >= 5 && btn.id <= 7)
            {
            	buttonList.remove(k1);
            }
            else if(btn.id == ID_PAGE_LEFT)
            {
	            if(pageNumber == 0 || view == VIEW_COLOURIZER)
	            {
	            	btn.enabled = false;
	            }
	            else
	            {
	            	btn.enabled = true;
	            }
            }
            else if(btn.id == ID_PAGE_RIGHT)
            {
        		if((pageNumber + 1) * 6 >= availableHats.size() || view == VIEW_COLOURIZER)
        		{
        			btn.enabled = false;
        		}
	            else
	            {
	            	btn.enabled = true;
	            }
            }
            else if(btn.id == ID_NONE)
            {
            	if(hat.hatName.equalsIgnoreCase(""))
            	{
            		btn.enabled = false;
            	}
            	else
            	{
            		btn.enabled = true;
            	}
            }
            else if(btn.id == ID_HAT_COLOUR_SWAP)
            {
            	if(hat.hatName.equalsIgnoreCase("") && view == VIEW_HATS)
            	{
            		btn.enabled = false;
            	}
            	else
            	{
            		btn.enabled = true;
            	}
            }
        }
        
    	if(view == VIEW_HATS)
    	{
	    	int button = 0;
	
	        for(int i = pageNumber * 6; i < availableHats.size() && i < (pageNumber + 1) * 6; i++)
	        {
	        	GuiButton btn;
	        	String hatName = (String)availableHats.get(i);
	        	
	        	btn = new GuiButton(ID_HAT_START_ID + i, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, hatName);
	        	
	        	if(hatName.toLowerCase().equalsIgnoreCase(hat.hatName))
	        	{
	        		btn.enabled = false;
	        	}
	        
	        	buttonList.add(btn);
	        	
	        	button++;
	        	if(button == 6)
	        	{
	        		button = 0;
	        		break;
	        	}
	        }
    	}
    	else if(view == VIEW_COLOURIZER)
    	{
    		int button = 0;
    		
    		for(int i = 0; i < 3; i++)
    		{
    			GuiButton btn = new GuiSlider(5 + i, width / 2 - 6, height / 2 - 78 + (22 * button), i == 0 ? "Red: " : i == 1 ? "Green: " : "Blue: ", 0, 255, i == 0 ? colourR : i == 1 ? colourG : colourB, this);
    			buttonList.add(btn);
    			
    			button++;
    		}
    	}
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture("/mods/hats/textures/gui/hatchooser.png");
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            String disp = btn.displayString;
            
            if(btn.id >= ID_HAT_START_ID)
            {
            	int id = btn.id - ID_HAT_START_ID;
            	if(!((pageNumber) * 6 <= id && (pageNumber + 1) * 6 > id))
            	{
            		continue;
            	}
            	
            	if(btn.displayString.length() > 16)
            	{
            		btn.displayString = btn.displayString.substring(0, 13) + "...";
            	}
            }
            btn.drawButton(this.mc, par1, par2);
            
            if(!(btn instanceof GuiSlider))
            {
            	btn.displayString = disp;
            }
            
            if(btn.id == ID_HAT_COLOUR_SWAP || btn.id == ID_NONE || btn.id == ID_RANDOM || btn.id == ID_HELP)
            {
            	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            	Minecraft.getMinecraft().renderEngine.bindTexture("/mods/hats/textures/gui/icons.png");
            	if(btn.id == ID_HAT_COLOUR_SWAP)
            	{
            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, (view == VIEW_HATS ? 16 : 0), 0, 16, 16);
            	}
            	else if(btn.id == ID_NONE)
            	{
            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 32, 0, 16, 16);
            	}
            	else if(btn.id == ID_RANDOM)
            	{
            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 80, 0, 16, 16);
            	}
            	else if(btn.id == ID_HELP)
            	{
            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 96, 0, 16, 16);
            	}

            	GL11.glDisable(GL11.GL_BLEND);
            }
        }
    	
        this.mouseX = (float)par1;
        this.mouseY = (float)par2;

        drawPlayerOnGui(k + 42, l + 155, 55, (float)(k + 42) - (float)mouseX, (float)(l + 155 - 92) - (float)mouseY);

        drawForeground(par1, par2, par3);
    }
    
    public void drawForeground(int par1, int par2, float par3)
    {
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            if(btn.func_82252_a())
            {
	            if(btn.id >= ID_HAT_START_ID && btn.displayString.length() > 16)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"\u00a77" + btn.displayString}), par1, par2);
	            }
	            else if(btn.id == ID_CLOSE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Discard changes?"}), par1, par2);
	            }
	            else if(btn.id == ID_NONE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Remove hat"}), par1, par2);
	            }
	            else if(btn.id == ID_HAT_COLOUR_SWAP)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS ? "Colourizer" : "Hats List")}), par1, par2);
	            }
	            else if(btn.id == ID_RANDOM)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS ? "Random Hat" : "Random Colour")}), par1, par2);
	            }
	            else if(btn.id == ID_HELP)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"HELP!", "", "Shift click on the Hat button for more options!"}), par1, par2);
	            }
	            else if(btn.id == ID_PAGE_LEFT && btn.enabled)
	            {
	            	drawTooltip(Arrays.asList(new String[] {Integer.toString(pageNumber)}), par1, par2);
	            }
	            else if(btn.id == ID_PAGE_RIGHT && btn.enabled)
	            {
	            	drawTooltip(Arrays.asList(new String[] {Integer.toString(pageNumber + 2)}), par1, par2);
	            }
            }
        }
    }
    
    protected void drawTooltip(List par1List, int par2, int par3)
    {
        if (!par1List.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = par1List.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int l = this.fontRenderer.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if (par1List.size() > 1)
            {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            if (i1 + k > this.width)
            {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height)
            {
                j1 = this.height - k1 - 6;
            }

            this.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < par1List.size(); ++k2)
            {
                String s1 = (String)par1List.get(k2);
                this.fontRenderer.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0)
                {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    
    public void drawPlayerOnGui(int par1, int par2, int par3, float par4, float par5)
    {
    	if(player != null)
    	{
	    	hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
	    	if(hat == null)
	    	{
	    		return;
	    	}
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
	        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        float f2 = player.renderYawOffset;
	        float f3 = player.rotationYaw;
	        float f4 = player.rotationPitch;
	        
	        float ff3 = hat.rotationYaw;
	        float ff4 = hat.rotationPitch;
	        
	        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        
	        player.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
	        player.rotationYaw = hat.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
	        player.rotationPitch = hat.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
	        player.rotationYawHead = player.rotationYaw;
	        GL11.glTranslatef(0.0F, player.yOffset, 0.0F);
	        
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
	        Tessellator.instance.setBrightness(240);
	        RenderManager.instance.renderEntityWithPosYaw(hat, 0.0D, 0.0D, 0.0D, hat.rotationYaw, 1.0F);
	        
	        player.renderYawOffset = f2;
	        player.rotationYaw = f3;
	        player.rotationPitch = f4;
	        
	        hat.rotationYaw = ff3;
	        hat.rotationPitch = ff4;
	        
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	}
    }

	@Override
	public void onChangeSliderValue(GuiSlider slider)
	{
		if(slider.id == 5)
		{
			colourR = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setR(colourR);
		}
		else if(slider.id == 6)
		{
			colourG = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setG(colourG);
		}
		else if(slider.id == 7)
		{
			colourB = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setB(colourB);
		}
	}

}
