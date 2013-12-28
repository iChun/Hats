package hats.client.gui;

import hats.client.core.HatInfoClient;
import hats.client.render.HatRendererHelper;
import hats.common.Hats;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiTradeWindow extends GuiScreen
	implements ISlider
{
	public static final ResourceLocation texIcons = new ResourceLocation("hats", "textures/gui/icons.png");
	public static final ResourceLocation texTradeWindow = new ResourceLocation("hats", "textures/gui/tradewindow.png");
	
	public static boolean hasStencilBits = MinecraftForgeClient.getStencilBits() > 0;
	
	private final int ID_TOGGLE_HATINV = 1;
	private final int ID_SLIDER_INV = 2;
	private final int ID_MAKE_READY = 3;
	private final int ID_MAKE_TRADE = 4;
	
	protected int xSize = 256;
	protected int ySize = 230;
	
	public float mouseX;
	public float mouseY;
	
	protected int guiLeft;
	protected int guiTop;

    public ArrayList<ItemStack> items;
    public ArrayList<String> hats;
    
    public ArrayList<ItemStack> ourItemsForTrade;
    public ArrayList<String> ourHatsForTrade;

    public ArrayList<ItemStack> theirItemsForTrade;
    public ArrayList<String> theirHatsForTrade;
    
    public ItemStack grabbedStack;

	public int hatSlots = 3;
	public int invSlots = 12;
	public double sliderProg = 0.0D;
	public RenderItem itemRenderer = new RenderItem();
	
	public boolean selfCanScroll = false;
	public boolean theirCanScroll = false;
	public boolean selfIsScrolling = false;
	public boolean theirIsScrolling = false;
	
	public float selfScrollProg;
	public float theirScrollProg;
	
	public String lastClicked = "";
	public int clickTimeout;
	
	public Minecraft mc;
	
	public boolean showInv;
	
	public long rotationalClock;
	
	public GuiTradeWindow()
	{
		mc = Minecraft.getMinecraft();
		showInv = false;
		
        items = new ArrayList<ItemStack>();
        hats = new ArrayList<String>(Hats.proxy.tickHandlerClient.availableHats);
        
    	invSlots = 0;
    	for(ItemStack is : mc.thePlayer.inventory.mainInventory)
    	{
    		if(is != null)
    		{
    			items.add(is.copy());
    			invSlots++;
    		}
    	}
    	
    	if(invSlots < 12)
    	{
    		invSlots = 12;
    	}
    	
    	hatSlots = 0;
    	for(int i = hats.size() - 1; i >= 0; i--)
    	{
    		String hatName = hats.get(i);
    		if(hatName.startsWith("(C) ") && hatName.substring(4).startsWith(mc.thePlayer.username)
					|| hatName.equalsIgnoreCase("(C) iChun") && mc.thePlayer.username.equalsIgnoreCase("ohaiiChun") //special casing for initial contrib hats.
					|| hatName.equalsIgnoreCase("(C) Mr. Haz") && mc.thePlayer.username.equalsIgnoreCase("damien95")
					|| hatName.equalsIgnoreCase("(C) Fridgeboy") && mc.thePlayer.username.equalsIgnoreCase("lacsap32"))
    		{
    			hats.remove(i);
    		}
    		else
    		{
    			hatSlots++;
    		}
    	}
    	if(hatSlots < 3)
    	{
    		hatSlots = 3;
    	}

    	ourItemsForTrade = new ArrayList<ItemStack>();
    	ourHatsForTrade = new ArrayList<String>();
    	
    	theirItemsForTrade = new ArrayList<ItemStack>();
    	theirHatsForTrade = new ArrayList<String>();
	}
	
	@Override
	public void initGui()
	{
        Keyboard.enableRepeatEvents(true);

        rotationalClock = Minecraft.getSystemTime();
        
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();
        
        buttonList.add(new GuiButton(ID_TOGGLE_HATINV, guiLeft + 6, guiTop + 6, 108, 20, showInv ? StatCollector.translateToLocal("hats.trade.yourInventory") : StatCollector.translateToLocal("hats.trade.yourHats")));
        buttonList.add(new GuiSlider(ID_SLIDER_INV, guiLeft + 6, guiTop + 65, "", 0D, 1D, sliderProg, this, "", false, 108));
        
        buttonList.add(new GuiButton(ID_MAKE_READY, guiLeft + 128, guiTop + 77, 120, 20, ""));//text is custom rendered
        buttonList.add(new GuiButton(ID_MAKE_TRADE, guiLeft + 148, guiTop + ySize - 32, 80, 20, StatCollector.translateToLocal("hats.trade.makeTrade")));
	}

	@Override
	public void onGuiClosed() 
	{
        Keyboard.enableRepeatEvents(false);
	}
	
	public void updateScreen() 
	{
		if(clickTimeout > 0)
		{
			clickTimeout--;
			if(clickTimeout == 0)
			{
				lastClicked = "";
			}
		}
	}
	
    @Override
    protected void mouseClicked(int x, int y, int btn)
    {
    	super.mouseClicked(x, y, btn);
    	
    	boolean isInInv = x >= guiLeft + 6 && x < guiLeft + 6 + 108 && y >= guiTop + 29 && y < guiTop + 29 + 36;
    	
    	if(isInInv)
    	{
    		int mouseProg = x - (guiLeft + 6);
    		
            ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(items);
            ArrayList<String> hatsList = new ArrayList<String>(hats);

            hatsList.removeAll(ourHatsForTrade);
            
            invSlots = itemsList.size();
            hatSlots = hatsList.size();
            
        	if(invSlots < 12)
        	{
        		invSlots = 12;
        	}
        	if(hatSlots < 3)
        	{
        		hatSlots = 3;
        	}

	        int size = showInv ? 18 : 36;
	        int columnWidth = 108;
	        int slotsToDraw = showInv ? (int)Math.ceil((float)invSlots / 2F) : hatSlots;
	        
	        int overallLength = slotsToDraw * size;
	        int startX = guiLeft + 6 + (int)((overallLength - columnWidth) * sliderProg);
	        
	        if(showInv)
	        {
	        	for(int i = 0; i < itemsList.size(); i++)
	        	{
	        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size < startX)
	        		{
	        			continue;
	        		}
	        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) <= startX + mouseProg && guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size > startX + mouseProg && (y < guiTop + 29 + size && i % 2 == 0 || y >= guiTop + 29 + size && i % 2 == 1))
	        		{
	        			System.out.println(itemsList.get(i));
	        			//TODO Grab stack and render it
	        		}
	        	}
	        }
	        else
	        {
	        	for(int i = 0; i < hatsList.size(); i++)
	        	{
	        		if(guiLeft + 6 + (size * i) + size < startX)
	        		{
	        			continue;
	        		}
	        		if(guiLeft + 6 + (size * i) <= startX + mouseProg && guiLeft + 6 + (size * i) + size > startX + mouseProg)
	        		{
	        			String hatName = hatsList.get(i);
	        			if(hatName.equalsIgnoreCase(lastClicked))
	        			{
	        				//TODO tell server!
	        				ourHatsForTrade.add(hatName);
	        				selfCanScroll = ourHatsForTrade.size() > 3 || ourItemsForTrade.size() > 6;
	        				if(!selfCanScroll)
	        				{
	        					selfScrollProg = 0.0F;
	        				}
	        				else if(ourHatsForTrade.size() % 3 == 1)
	        				{
	        					float oldBoxes = (float)Math.floor((float)Math.max(ourHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F) - 3;
	        					if(oldBoxes > 0)
	        					{
	        						selfScrollProg = selfScrollProg * oldBoxes / (oldBoxes + 2);
	        					}
	        				}
	        				
        		    		for(int ii = 0; ii < buttonList.size(); ii++)
        		    		{
        		    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
        		    			if(btn1 instanceof GuiSlider)
        		    			{
        		    				((GuiSlider)btn1).sliderValue = (double)MathHelper.clamp_float((float)((GuiSlider)btn1).sliderValue * (hatSlots - 3) / (hatSlots - 4), 0.0F, 1.0F);
        		    				((GuiSlider)btn1).updateSlider();
        		    			}
        		    		}
	        			}
	        			else
	        			{
	        				lastClicked = hatName;	        				
	        			}
	        			break;
	        		}
	        	}
	        }
    	}
    	
    	boolean isInSelf = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 17 && y < guiTop + 17 + 54;
    	
    	if(isInSelf)
    	{
    		int mouseProg = y - (guiTop + 17);

    		int size = 36;
    		int columnWidth = 18 * 3;
    		int slotsToDraw = ourHatsForTrade.size();

    		while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
    		{
    			slotsToDraw++;
    		}

    		int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);

    		int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);

	        int overallLength = boxes * 18;
    		
	        int startY = guiTop + 17 + (int)((overallLength - columnWidth) * selfScrollProg);
	        
	        boolean clicked = false;
	        
	        for(int i = 0; i < ourHatsForTrade.size(); i++)
	        {
        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size < startY)
        		{
        			continue;
        		}
        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) <= startY + mouseProg && guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size > startY + mouseProg && (x < guiLeft + 125 + size && i % 3 == 0 || x < guiLeft + 125 + size + size && i % 3 == 1 || x >= guiLeft + 125 + size + size && i % 3 == 2))
        		{
        			String hatName = ourHatsForTrade.get(i);
        			if(hatName.equalsIgnoreCase(lastClicked))
        			{
        				//TODO tell server!
        				ourHatsForTrade.remove(hatName);
        				selfCanScroll = ourHatsForTrade.size() > 3 || ourItemsForTrade.size() > 6;
        				if(!selfCanScroll)
        				{
        					selfScrollProg = 0.0F;
        				}
        				else if(ourHatsForTrade.size() % 3 == 0)
        				{
        					float oldBoxes = (float)Math.floor((float)Math.max(ourHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F) - 3;
        					if(oldBoxes > 0)
        					{
        						selfScrollProg = MathHelper.clamp_float(selfScrollProg * (oldBoxes + 2) / (oldBoxes), 0.0F, 1.0F);
        					}
        				}
        				
    					if(!showInv)
    					{
        		    		for(int ii = 0; ii < buttonList.size(); ii++)
        		    		{
        		    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
        		    			if(btn1 instanceof GuiSlider)
        		    			{
        		    				((GuiSlider)btn1).sliderValue = (double)MathHelper.clamp_float((float)((GuiSlider)btn1).sliderValue * (hatSlots - 3) / (hatSlots + 1 - 3), 0.0F, 1.0F);
        		    				((GuiSlider)btn1).updateSlider();
        		    			}
        		    		}
    					}
        			}
        			else
        			{
        				lastClicked = hatName;	        				
        			}
        			break;
        		}
	        }
	        
    		size = 18;
    		columnWidth = 108 / size;
    		slotsToDraw = 6;

    		if(!clicked)
    		{
    			//TODO item counterpart
//	    		for(int i = 0; i < slotsToDraw; i++)
//	    		{
//	    			this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 17 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels), size == 36 ? 0 : 36, 45, size, size);
//	    			this.mc.getTextureManager().bindTexture(texIcons);
//	    		}
    		}
    	}

    	
    	boolean isOnOurScroll = x >= guiLeft + 237 && x < guiLeft + 237 + 12 && y >= guiTop + 18 && y < guiTop + 18 + 37 + 15;
    	if(isOnOurScroll && btn == 0 && selfCanScroll)
    	{
    		selfIsScrolling = true;
    	}
    	boolean isOnTheirScroll = x >= guiLeft + 237 && x < guiLeft + 237 + 12 && y >= guiTop + 117 && y < guiTop + 117 + 37 + 15;
    	if(isOnTheirScroll && btn == 0 && theirCanScroll)
    	{
    		theirIsScrolling = true;
    	}
    }
    
    public void drawForeground(int x, int y, float par3)
    {
    	boolean isInInv = x >= guiLeft + 6 && x < guiLeft + 6 + 108 && y >= guiTop + 29 && y < guiTop + 29 + 36;
    	boolean isInSelf = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 17 && y < guiTop + 17 + 54;
    	boolean isInThem = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 116 && y < guiTop + 116 + 54;
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
//    	initGui();
    	if(mc == null)
    	{
    		mc = Minecraft.getMinecraft();
    		fontRenderer = mc.fontRenderer;
    	}
    	drawDefaultBackground();
    	
        boolean flag = Mouse.isButtonDown(0);
        if(!flag)
        {
        	selfIsScrolling = false;
        	theirIsScrolling = false;
        }
        else if(selfIsScrolling)
        {
//        	this.drawTexturedModalRect(k + 237, l + 18, selfCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 12 wide 15 long //scroll bar is 37 long
//        	37 pix long; 7 on one side, 8 on the top.
//        	y >= guiTop + 18 && y < guiTop + 18 + 37
        	selfScrollProg = MathHelper.clamp_float((float)(par2 - (guiTop + 18 + 7)) / 37F, 0.0F, 1.0F);
        }
        else if(theirIsScrolling)
        {
        }

        this.mouseX = (float)par1;
        this.mouseY = (float)par2;
    	
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.guiLeft;
        int l = this.guiTop;
        
        ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(items);
        ArrayList<String> hatsList = new ArrayList<String>(hats);

        hatsList.removeAll(ourHatsForTrade);
        
        invSlots = itemsList.size();
        hatSlots = hatsList.size();
        
    	if(invSlots < 12)
    	{
    		invSlots = 12;
    	}
    	if(hatSlots < 3)
    	{
    		hatSlots = 3;
    	}
        
        
        this.mc.getTextureManager().bindTexture(texIcons);
        
        GL11.glPushMatrix();
        
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        drawSolidRect(guiLeft + 6, guiTop + 29, 108, 36, 0xffffff);//Inv area
        
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        GL11.glColorMask(true, true, true, true);
        
        if(mc.thePlayer != null)
        {
	        int size = showInv ? 18 : 36;
	        int columnWidth = 108 / size;
	        int slotsToDraw = showInv ? (int)Math.ceil((float)invSlots / 2F) : hatSlots;
	        
	        if(slotsToDraw > columnWidth)
	        {
	        	GL11.glTranslatef(-(float)(size * (slotsToDraw - columnWidth) * sliderProg), 0.0F, 0.0F);
	        }
	        
	        int lastInv = 0;
	        for(int i = 0; i < slotsToDraw; i++)
	        {
	        	if(((size * i) + size < (float)(size * (slotsToDraw - columnWidth) * sliderProg) || (size * i) > (float)(size * (slotsToDraw) * sliderProg + columnWidth * size)))
	        	{
	        		lastInv++;
	        		if(showInv)
	        		{
	        			lastInv++;
	        		}
	        		continue;
	        	}
	        	this.drawTexturedModalRect(k + 6 + (size * i), l + 29, size == 36 ? 0 : 36, 45, size, size);
	        	
	        	if(showInv)
	        	{
	        		if(lastInv < itemsList.size())
	        		{
	        			drawItemStack(itemsList.get(lastInv), k + 6 + (size * i) + 1, l + 29 + 1);
	        			lastInv++;
	        		}
	        	}
	        	else
	        	{
	        		if(lastInv < hatsList.size())
	        		{
	        			GL11.glPushMatrix();

	                    GL11.glTranslatef((float)(k + 6 + (size * i) - 2), (float)(l + 29 + 14), -3.0F + this.zLevel);

	    	            GL11.glScalef(20.0F, 20.0F, 20.0F);

	                    GL11.glTranslatef(1.0F, 0.5F, 1.0F);
	                    GL11.glScalef(1.0F, 1.0F, -1.0F);
	                    GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
	                    GL11.glRotatef(45.0F + (Minecraft.getSystemTime() - this.rotationalClock) / 6F, 0.0F, 1.0F, 0.0F);
	        			
	        			HatInfoClient info = new HatInfoClient(hatsList.get(lastInv).toLowerCase());
	        			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
	        			lastInv++;
	        			GL11.glPopMatrix();
	        		}
	        	}
	        	this.mc.getTextureManager().bindTexture(texIcons);
	        	if(size == 18)
	        	{
	        		this.drawTexturedModalRect(k + 6 + (size * i), l + 29 + size, size == 36 ? 0 : 36, 45, size, size);
	        		
	               	if(showInv)
	            	{
		        		if(lastInv < itemsList.size())
		        		{
	        				drawItemStack(itemsList.get(lastInv), k + 6 + (size * i) + 1, l + 29 + size + 1);
	        		        lastInv++;
		            		this.mc.getTextureManager().bindTexture(texIcons);
		        		}
	            	}
	        	}
	        }
        }
        GL11.glPopMatrix();
        
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        
        GL11.glColorMask(false, false, false, false);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        drawSolidRect(guiLeft + 125, guiTop + 17, 108, 54, 0xffffff);//Inv area
        
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        GL11.glColorMask(true, true, true, true);
         
        int size = 36;
        int columnWidth = 108 / size;
        int slotsToDraw = ourHatsForTrade.size();
        
        while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
        {
        	slotsToDraw++;
        }
        
        int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
        
        GL11.glPushMatrix();
        
        int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);
        
        GL11.glTranslatef(0.0F, (-boxes * 18F + 54) * selfScrollProg, 0.0F);
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 17 + (size * (int)(Math.floor(i / columnWidth))), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < ourHatsForTrade.size())
        	{
    			GL11.glPushMatrix();

                GL11.glTranslatef((float)(k + 125 + (size * (i % columnWidth)) - 2), (float)(l + 17 + (size * (int)(Math.floor(i / columnWidth))) + 14), -3.0F + this.zLevel);

	            GL11.glScalef(20.0F, 20.0F, 20.0F);

                GL11.glTranslatef(1.0F, 0.5F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
    			
    			HatInfoClient info = new HatInfoClient(ourHatsForTrade.get(i).toLowerCase());
    			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
    			GL11.glPopMatrix();
        	}
        	
        	this.mc.getTextureManager().bindTexture(texIcons);
        }
        
        size = 18;
        columnWidth = 108 / size;
        slotsToDraw = 6;
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 17 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels), size == 36 ? 0 : 36, 45, size, size);
        	this.mc.getTextureManager().bindTexture(texIcons);
        }
        
        GL11.glPopMatrix();
        
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        
        GL11.glColorMask(false, false, false, false);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        drawSolidRect(guiLeft + 125, guiTop + 116, 108, 54, 0xffffff);//Inv area
        
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        GL11.glColorMask(true, true, true, true);
         
        size = 36;
        columnWidth = 108 / size;
        slotsToDraw = theirHatsForTrade.size();
        
        while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
        {
        	slotsToDraw++;
        }
        
        hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
        
        GL11.glPushMatrix();
        
        boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(theirHatsForTrade.size(), 6) / 6F);
        
        GL11.glTranslatef(0.0F, (-boxes * 18F + 54) * selfScrollProg, 0.0F);
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 116 + (size * (int)(Math.floor(i / columnWidth))), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < theirHatsForTrade.size())
        	{
    			GL11.glPushMatrix();

                GL11.glTranslatef((float)(k + 125 + (size * (i % columnWidth)) - 2), (float)(l + 116 + (size * (int)(Math.floor(i / columnWidth))) + 14), -3.0F + this.zLevel);

	            GL11.glScalef(20.0F, 20.0F, 20.0F);

                GL11.glTranslatef(1.0F, 0.5F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
    			
    			HatInfoClient info = new HatInfoClient(theirHatsForTrade.get(i).toLowerCase());
    			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
    			GL11.glPopMatrix();
        	}
        	
        	this.mc.getTextureManager().bindTexture(texIcons);
        }
        
        size = 18;
        columnWidth = 108 / size;
        slotsToDraw = 6;
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 116 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels), size == 36 ? 0 : 36, 45, size, size);
        	this.mc.getTextureManager().bindTexture(texIcons);
        }

        GL11.glPopMatrix();
        
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        
        this.mc.getTextureManager().bindTexture(texTradeWindow);
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
         
        this.mc.getTextureManager().bindTexture(texIcons);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 37F * selfScrollProg, 0.0F);
        this.drawTexturedModalRect(k + 237, l + 18, selfCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 15 wide //scroll bar is 37 long
        GL11.glPopMatrix();
        this.drawTexturedModalRect(k + 237, l + 117, theirCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 15 wide //scroll bar is 37 long
        
        super.drawScreen(par1, par2, par3);
        
        fontRenderer.drawString(StatCollector.translateToLocal("hats.trade.yourOfferings"), (int)(guiLeft + 125), (int)(guiTop + 6), 0x2c2c2c, false);
        fontRenderer.drawString(StatCollector.translateToLocalFormatted("hats.trade.theirOfferings", new Object[] { "Kihira" }), (int)(guiLeft + 125), (int)(guiTop + 105), 0x2c2c2c, false);
        
        GL11.glPushMatrix();
        float scale = 1F;
        GL11.glScalef(scale, scale, scale);
        fontRenderer.drawString(StatCollector.translateToLocal("hats.trade.tradeReady"), (int)((guiLeft + 187) / scale - (fontRenderer.getStringWidth(StatCollector.translateToLocal("hats.trade.tradeReady")) / 2)), (int)((guiTop + 83) / scale), 0x81b63a, false); //0x790000
        GL11.glPopMatrix();
        
        fontRenderer.drawString(StatCollector.translateToLocal("hats.trade.tradeReady"), (int)(guiLeft + 187 - (fontRenderer.getStringWidth(StatCollector.translateToLocal("hats.trade.tradeReady")) / 2)), (int)(guiTop + 176), 0x517924, false); //0x790000
        
        GL11.glPushMatrix();
        scale = 0.5F;
        GL11.glScalef(scale, scale, scale);
        fontRenderer.drawString(StatCollector.translateToLocal("hats.trade.waitingForReady"), (int)((guiLeft + 187) / scale - (fontRenderer.getStringWidth(StatCollector.translateToLocal("hats.trade.waitingForReady")) / 2)), (int)((guiTop + ySize - 10) / scale), -16777216, false);
        GL11.glPopMatrix();
        
        drawForeground(par1, par2, par3);
    }
    
    public void drawItemStack(ItemStack itemstack, int par2, int par3)
    {
        if (itemstack != null)
        {
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton btn)
    {
    	if(btn.id == ID_TOGGLE_HATINV)
    	{
    		showInv = !showInv;
    		btn.displayString = showInv ? StatCollector.translateToLocal("hats.trade.yourInventory") : StatCollector.translateToLocal("hats.trade.yourHats");
    		for(int i = 0; i < buttonList.size(); i++)
    		{
    			GuiButton btn1 = (GuiButton)buttonList.get(i);
    			if(btn1 instanceof GuiSlider)
    			{
    				((GuiSlider)btn1).sliderValue = 0.0D;
    				((GuiSlider)btn1).updateSlider();
    			}
    		}
    	}
    }

	@Override
	public void onChangeSliderValue(GuiSlider slider) 
	{
		sliderProg = slider.getValue();
	}
	
    public void drawSolidRect(int par0, int par1, int par2, int par3, int par4)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorRGBA(255, 255, 255, 255);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)(par0 + 0), (double)(par1 + par3), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + par2), (double)(par1 + par3), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + par2), (double)(par1 + 0), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + 0), (double)(par1 + 0), (double)this.zLevel);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
