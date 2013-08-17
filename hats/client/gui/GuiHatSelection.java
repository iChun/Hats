package hats.client.gui;

import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiHatSelection extends GuiScreen 
	implements ISlider
{

	public static final ResourceLocation texIcons = new ResourceLocation("hats", "textures/gui/icons.png");
	public static final ResourceLocation texChooser = new ResourceLocation("hats", "textures/gui/hatchooser.png");
	
	private final int ID_PAGE_LEFT = 1;
	private final int ID_DONE_SELECT = 2;
	private final int ID_PAGE_RIGHT = 3;
	private final int ID_CLOSE = 4;
	// 5 6 7 are slider IDs

	private final int ID_NONE = 8;
	private final int ID_HAT_COLOUR_SWAP = 9;
	private final int ID_RANDOM = 10;
	private final int ID_FAVOURITES = 11;
	private final int ID_CATEGORIES = 12;
	private final int ID_PERSONALIZE = 13;
	private final int ID_RELOAD_HATS = 14;
	private final int ID_HELP = 15;
	private final int ID_SEARCH = 16;
	
	private final int ID_ADD = 17;
	private final int ID_CANCEL = 18;
	private final int ID_RENAME = 19;
	private final int ID_DELETE = 20;
	private final int ID_FAVOURITE = 21;
	
	private final int ID_SET_KEY = 22;
	private final int ID_SET_FP = 23;
	private final int ID_RESET_SIDE = 24;
	private final int ID_MOB_SLIDER = 25;
	private final int ID_SHOW_HATS = 26;
	
	private final int ID_CATEGORIES_START = 30;
	
	private final int ID_HAT_START_ID = 600;
	
	private final int VIEW_HATS = 0;
	private final int VIEW_COLOURIZER = 1;
	private final int VIEW_CATEGORIES = 2;
	private final int VIEW_CATEGORY = 3;
	
	private String category = "";
	private String currentDisplay;
	private GuiTextField searchBar;
	private String selectedButtonName = "";
	private int favourite;
	
	private int randoMob;
	
	private boolean hasClicked = false;
	private boolean confirmed = false;
	private boolean adding = false;
	private boolean invalidFolderName = false;
	private boolean deleting = false;
	private boolean justClickedButton = false;
	private boolean renaming = false;
	private boolean addingToCategory = false;
	private boolean personalizing = false;
	private boolean settingKey = false;
	
	private boolean enabledSearchBar = false;
	
	public EntityPlayer player;
	public EntityHat hat;
	public List<String> availableHats;
	public List<String> hatsToShow;
	public List<String> categories;
	public List<String> categoryHats = new ArrayList<String>();
	public List<String> enabledButtons = new ArrayList<String>();
	
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
		if(Hats.proxy.tickHandlerClient.serverHatMode == 4)
		{
			if(Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode)
			{
				HatHandler.repopulateHatsList();
			}
			else
			{
				Hats.proxy.tickHandlerClient.availableHats = new ArrayList<String>(Hats.proxy.tickHandlerClient.serverHats);
				Collections.sort(Hats.proxy.tickHandlerClient.availableHats);
			}
		}
		
		player = ply;
		hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
		ArrayList<String> list = new ArrayList<String>(Hats.proxy.tickHandlerClient.availableHats);
		
		for(int i = list.size() - 1; i >= 0; i--)
		{
			if(Hats.allowContributorHats == 0 && list.get(i).startsWith("(C) "))
			{
				list.remove(i);
			}
		}
		
		availableHats = ImmutableList.copyOf(list);
		hatsToShow = new ArrayList<String>(availableHats);
		Collections.sort(hatsToShow);
		
		categories = new ArrayList<String>();
		for(Map.Entry<String, ArrayList<String>> e : HatHandler.categories.entrySet())
		{
			if(!e.getKey().equalsIgnoreCase("Favourites"))
			{
				categories.add(e.getKey());
			}
		}
		Collections.sort(categories);
		
		if(hat != null)
		{
			prevHatName = hat.hatName;
			prevColourR = colourR = hat.getR();
			prevColourG = colourG = hat.getG();
			prevColourB = colourB = hat.getB();
		}
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
	        Keyboard.enableRepeatEvents(true);

	        String[] enabledBtn = Hats.enabled.split(" ");
        	enabledButtons.clear();
	        for(int i = 0; i < enabledBtn.length; i++)
	        {
	        	if(enabledBtn[i].equalsIgnoreCase("9"))
	        	{
	        		enabledSearchBar = true;
	        	}
	        	else if(!enabledButtons.contains(enabledBtn[i]))
	        	{
	        		enabledButtons.add(enabledBtn[i]);
	        	}
	        }
	        
	        
			buttonList.clear();
			
	        this.guiLeft = (this.width - this.xSize) / 2;
	        this.guiTop = (this.height - this.ySize) / 2;
	        
	        buttonList.add(new GuiButton(ID_PAGE_LEFT, width / 2 - 6, height / 2 + 54, 20, 20, "<"));
	        buttonList.add(new GuiButton(ID_PAGE_RIGHT, width / 2 + 62, height / 2 + 54, 20, 20, ">"));
	        buttonList.add(new GuiButton(ID_DONE_SELECT, width / 2 + 16, height / 2 + 54, 44, 20, "Done"));
	        
	        //4, 5, 6, 7 = taken.
	        
	        addToolButton(ID_NONE);
	        addToolButton(ID_HAT_COLOUR_SWAP);
	        addToolButton(ID_RANDOM);
	        addToolButton(ID_FAVOURITES);
	        addToolButton(ID_CATEGORIES);
	        addToolButton(ID_PERSONALIZE);
	        addToolButton(ID_RELOAD_HATS);
	        addToolButton(ID_HELP);
	        
	        buttonList.add(new GuiButton(ID_CLOSE, width - 22, 2, 20, 20, "X"));
	        
	        pageNumber = 0;
	        
	        if(!hat.hatName.equalsIgnoreCase(""))
	        {
	        	for(int i = 0; i < hatsToShow.size(); i++)
		        {
		        	String hatName = (String)hatsToShow.get(i);
		        	if(hatName.equalsIgnoreCase(hat.hatName))
		        	{
		        		i -= i % 6;
		        		pageNumber = i / 6;
		        		break;
		        	}
		        }
	        }
	        
	        updateButtonList();
	        
			searchBar = new GuiTextField(this.fontRenderer, this.width / 2 - 65, height - 24, 150, 20);
			searchBar.setMaxStringLength(255);
			searchBar.setText("Search");
			searchBar.setTextColor(0xAAAAAA);
			searchBar.setVisible(enabledSearchBar);
		}
	}
	
	public void addToolButton(int id)
	{
		boolean enabled = false;
		for(int i = 0; i < enabledButtons.size(); i++)
		{
			if(enabledButtons.get(i).equalsIgnoreCase(Integer.toString(id - (ID_NONE - 1))))
			{
				buttonList.add(new GuiButton(id, width / 2 + 89, height / 2 - 85 + (i * 21), 20, 20, ""));
				enabled = true;
				break;
			}
		}
		if(!enabled)
		{
			GuiButton btn = new GuiButton(id, width - 24, height / 2 - 93 + ((id - 8) * 21), 20, 20, "");
			btn.drawButton = false;
			buttonList.add(btn);
		}
	}
	
	@Override
    public void updateScreen()
    {
        searchBar.updateCursorCounter();
        if(searchBar.isFocused())
        {
        	searchBar.setVisible(true);
        }
        else
        {
        	searchBar.setVisible(enabledSearchBar);
        }
        if(favourite > 0)
        {
        	favourite--;
        }
    }
	
	@Override
	public void onGuiClosed() 
	{
		if(!confirmed)
		{
			hat.hatName = prevHatName;
			hat.setR(prevColourR);
			hat.setG(prevColourG);
			hat.setB(prevColourB);
		}
        Keyboard.enableRepeatEvents(false);
	}
	
    @Override
    protected void keyTyped(char c, int i)
    {
    	if(settingKey)
    	{
    		Hats.guiKeyBind = i;
    		for(int i1 = 0; i1 < buttonList.size(); i1++)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(i1);
    			if(btn.id == ID_SET_KEY)
    			{
    				btn.displayString = "GUI: " + Keyboard.getKeyName(i);
    				break;
    			}
    		}
    		settingKey = false;
    		Hats.handleConfig();
    	}
    	else if(!personalizing)
    	{
	    	searchBar.textboxKeyTyped(c, i);
	    	if(searchBar.isFocused())
	    	{
	    		onSearch();
	    	}
	        if (i == 1)
	        {
		    	if(searchBar.isFocused())
		    	{
		    		searchBar.setText("");
		    		searchBar.setFocused(false);
		    		onSearchBarInteract();
		    	}
		    	else
		    	{
		        	exitWithoutUpdate();
		        	
		            this.mc.setIngameFocus();
		    	}
	        }
	        if(!searchBar.isFocused())
	        {
	        	GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		        if(i == Keyboard.KEY_R)
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	randomize();
		        }
		        else if(i == Keyboard.KEY_TAB || i == gameSettings.keyBindChat.keyCode)
		        {
		        	searchBar.setFocused(true);
		        	onSearchBarInteract();
		        }
		        else if(i == Keyboard.KEY_H && !(hat.hatName.equalsIgnoreCase("") && view == VIEW_HATS))
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	toggleHatsColourizer();
		        }
		        else if(i == Keyboard.KEY_N && !hat.hatName.equalsIgnoreCase(""))
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	removeHat();
		        }
		        else if(i == Keyboard.KEY_C)
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	showCategories();
		        }
		        else if(i == Keyboard.KEY_F)
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	if((view == VIEW_HATS || view == VIEW_CATEGORY) && isShiftKeyDown())
		        	{
		        		String s = "";
		        		
		    	        for(String hat1 : availableHats)
		    	        {
		    	        	if(hat1.toLowerCase().equalsIgnoreCase(hat.hatName))
		    	        	{
		    	        		s = hat1;
		    	        	}
		    	        }
	
		    	        if(!s.equalsIgnoreCase(""))
		    	        {
				    		if(HatHandler.isInFavourites(s))
				    		{
				    			HatHandler.removeFromCategory(s, "Favourites");
				    		}
				    		else
				    		{
				    			HatHandler.addToCategory(s, "Favourites");
				    		}
				    		
			    			if(view == VIEW_CATEGORY && category.equalsIgnoreCase("Favourites"))
			    			{
			    				showCategory("Favourites");
			    			}
				    		favourite = 6;
		    	        }
		        	}
		        	else
		        	{
		        		showCategory("Favourites");
		        	}
		        }
		        else if(i == Keyboard.KEY_P)
		        {
		        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		        	personalize();
		        }
		        if(view == VIEW_HATS)
		        {
			        if((i == gameSettings.keyBindLeft.keyCode || i == Keyboard.KEY_LEFT) && pageNumber > 0)
			        {
			        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			        	switchPage(true);
			        }
			        else if((i == gameSettings.keyBindRight.keyCode || i == Keyboard.KEY_RIGHT) && !((pageNumber + 1) * 6 >= hatsToShow.size()))
			        {
			        	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			        	switchPage(false);
			        }
		        }
	        }
	        else
	        {
	        	if(i == Keyboard.KEY_RETURN && (adding || renaming) && view == VIEW_CATEGORIES && !invalidFolderName)
	        	{
	    			if(adding && addCategory(searchBar.getText().trim()) || renaming && renameCategory(selectedButtonName, searchBar.getText().trim()))
	    			{
	    				searchBar.setText("");
	    				onSearchBarInteract();
	    				
	    				updateButtonList();
	    			}
	        	}
	        }
    	}
    }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
    	if(settingKey)
    	{
    		Hats.guiKeyBind = par3 - 100;
    		for(int i = 0; i < buttonList.size(); i++)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(i);
    			if(btn.id == ID_SET_KEY)
    			{
    				btn.displayString = "GUI: " + Mouse.getButtonName(par3);
    				break;
    			}
    		}
    		settingKey = false;
    		Hats.handleConfig();
    	}
    	else
    	{
	        super.mouseClicked(par1, par2, par3);
	        boolean flag = par1 >= (this.width / 2 - 65) && par1 < (this.width / 2 - 65) + this.width && par2 >= (height - 24) && par2 < (height - 24) + this.height;
	        if(enabledSearchBar)
	        {
		        if(!personalizing)
		        {
		        	searchBar.mouseClicked(par1, par2, par3);
		        	
			        if(par3 == 1 && flag)
			        {
			        	searchBar.setText("");
			        	onSearch();
			        }
		        	onSearchBarInteract();
		        }
		        else if(flag)
		        {
		        	toggleSearchBar();
		        }
	        }
    	}
    }
    
    @Override
    protected void mouseMovedOrUp(int par1, int par2, int par3)
    {
    	super.mouseMovedOrUp(par1, par2, par3);
    	if(adding || renaming)
    	{
			searchBar.setFocused(true);
			onSearchBarInteract();
			onSearch();
    	}
    	justClickedButton = false;
    }
    
    public void onSearch()
    {
    	if(adding || renaming)
    	{
    		invalidFolderName = false;
    		searchBar.setTextColor(14737632);
    		for(String s : categories)
    		{
    			if(s.equalsIgnoreCase(searchBar.getText()))
    			{
    				searchBar.setTextColor(0xFF5555);
    				invalidFolderName = true;
    			}
    		}
    		for(String s : invalidChars)
    		{
    			if(searchBar.getText().contains(s))
    			{
    				searchBar.setTextColor(0xFF5555);
    				invalidFolderName = true;
    			}
    		}
    		if(searchBar.getText().equalsIgnoreCase("Favourites") || searchBar.getText().equalsIgnoreCase("All Hats") || searchBar.getText().equalsIgnoreCase("Add New"))
    		{
				searchBar.setTextColor(0xFF5555);
				invalidFolderName = true;
    		}
    		if(searchBar.getText().equalsIgnoreCase(""))
    		{
				invalidFolderName = true;    			
    		}
    		for(int k = 0; k < buttonList.size(); k++)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(k);
    			if(btn.id == ID_ADD)
    			{
    				btn.enabled = !invalidFolderName;
    				break;
    			}
    		}
    	}
    	else
    	{
	    	if(searchBar.getText().equalsIgnoreCase("") || !hasClicked && searchBar.getText().equalsIgnoreCase("Search"))
	    	{
	    		searchBar.setTextColor(14737632);
	   			hatsToShow = new ArrayList<String>(view == VIEW_HATS ? availableHats : view == VIEW_CATEGORY ? categoryHats : categories);
        		Collections.sort(hatsToShow);
        		if(view == VIEW_CATEGORIES)
        		{
        			hatsToShow.add(0, "All Hats");
        			hatsToShow.add("Add New");
        		}
	    	}
	    	else
	    	{
	    		String query = searchBar.getText();
	    		ArrayList<String> matches = new ArrayList<String>();
	    		for(String s : (view == VIEW_HATS ? availableHats : view == VIEW_CATEGORY ? categoryHats : categories))
	    		{
	    			if(view == VIEW_CATEGORIES && (s.equalsIgnoreCase("All Hats") || s.equalsIgnoreCase("Add New")))
	    			{
	    				continue;
	    			}
	    			if(s.toLowerCase().startsWith(query.toLowerCase()))
	    			{
						if(!matches.contains(s))
						{
							matches.add(s);
						}
	    			}
	    			else
	    			{
		    			String[] split = s.split(" ");
		    			for(String s1 : split)
		    			{
		    				if(s1.toLowerCase().startsWith(query.toLowerCase()))
		    				{
		    					if(!matches.contains(s))
		    					{
		    						matches.add(s);
		    					}
		    					break;
		    				}
		    			}
	    			}
	    		}
	    		if(matches.size() == 0)
	    		{
	    			searchBar.setTextColor(0xFF5555);
	        		hatsToShow = new ArrayList<String>(view == VIEW_HATS ? availableHats : view == VIEW_CATEGORY ? categoryHats : categories);
	        		Collections.sort(hatsToShow);
	        		if(view == VIEW_CATEGORIES)
	        		{
	        			hatsToShow.add(0, "All Hats");
	        			hatsToShow.add("Add New");
	        		}
	    		}
	    		else
	    		{
	    			searchBar.setTextColor(14737632);
	    			pageNumber = 0;
	    			hatsToShow = new ArrayList<String>(matches);
	    			Collections.sort(hatsToShow);
	    		}
	    	}
	    	
	    	updateButtonList();
    	}
    }
    
    public void onSearchBarInteract()
    {
        if(searchBar.isFocused())
        {
        	searchBar.setTextColor(14737632);
        	if(!hasClicked && searchBar.getText().equalsIgnoreCase("Search"))
        	{
        		hasClicked = true;
        		searchBar.setText("");
        		onSearch();
        	}
        }
        else
        {
        	searchBar.setTextColor(0xAAAAAA);
        	if(searchBar.getText().equalsIgnoreCase(""))
        	{
        		hasClicked = false;
        		if(!adding && !renaming)
        		{
        			searchBar.setText("Search");
        			
            		if(view == VIEW_CATEGORIES)
            		{
            			updateButtonList();
            		}
        		}
        	}
        }    	
    }
    
    @Override
    protected void actionPerformed(GuiButton btn)
    {
    	if(btn.id == ID_DONE_SELECT)
    	{
    		if(personalizing)
    		{
    			personalize();
    		}
    		else
    		{
    			exitAndUpdate();
    		}
    	}
    	if(personalizing)
    	{
    		if(btn.id >= ID_NONE && btn.id <= ID_SEARCH && !justClickedButton)
    		{
    			justClickedButton = true;
    			toggleVisibility(btn);
    			
    			if(btn.id == ID_SEARCH)
    			{
    				buttonList.remove(btn);
    			}
    		}
	    	else if(btn.id == ID_SET_KEY)
	    	{
	    		settingKey = true;
	    		btn.displayString = "GUI: >???<";
	    	}
	    	else if(btn.id == ID_SET_FP)
	    	{
	    		Hats.renderInFirstPerson = (Hats.renderInFirstPerson == 1 ? 0 : 1);
	    		btn.displayString = "First Person: " + (Hats.renderInFirstPerson == 1 ? "Yes" : "No");
	    	}
	    	else if(btn.id == ID_SHOW_HATS)
	    	{
	    		Hats.renderHats = (Hats.renderHats == 1 ? 0 : 1);
	    		btn.displayString = "Show Hats: " + (Hats.renderHats == 1 ? "Yes" : "No");
	    	}
	    	else if(btn.id == ID_RESET_SIDE)
	    	{
	    		enabledSearchBar = false;
	    		toggleSearchBar();
	    		
	    		enabledButtons.clear();
	    		enabledButtons.add("1");
	    		enabledButtons.add("2");
	    		enabledButtons.add("3");
	    		enabledButtons.add("4");
	    		enabledButtons.add("5");
	    		enabledButtons.add("6");
	    		enabledButtons.add("7");
	    		enabledButtons.add("8");
	    		
	    		for(int i = buttonList.size() - 1; i >= 0; i--)
	    		{
	    			GuiButton btn1 = (GuiButton)buttonList.get(i);
	    			if(btn1.id >= ID_NONE && btn1.id <= (ID_SEARCH - 1))
	    			{
		    			btn1.xPosition = width / 2 + 89;
		    			btn1.yPosition = height / 2 - 85 + ((btn1.id - ID_NONE) * 21);
	    			}
	    			else if(btn1.id == ID_SEARCH)
	    			{
	    				buttonList.remove(i);
	    			}
	    		}
	    	}
    	}
    	else
    	{
	    	if(btn.id == ID_PAGE_LEFT)
	    	{
	    		switchPage(true);
	    	}
	    	else if(btn.id == ID_PAGE_RIGHT)
	    	{
	    		switchPage(false);
	    	}
	    	else if(btn.id == ID_CLOSE)
	    	{
	    		exitWithoutUpdate();
	    	}
	    	else if(btn.id == ID_NONE)
	    	{
	    		removeHat();
	    	}
	    	else if(btn.id == ID_HAT_COLOUR_SWAP)
	    	{
	    		toggleHatsColourizer();
	    	}
	    	else if(btn.id == ID_RANDOM)
	    	{
	    		randomize();
	    	}
	    	else if(btn.id == ID_RELOAD_HATS)
	    	{
	    		reloadHatsAndReopenGUI();
	    	}
	    	else if(btn.id == ID_FAVOURITES)
	    	{
	    		showCategory("Favourites");
	    	}
	    	else if(btn.id == ID_CATEGORIES)
	    	{
	    		showCategories();
	    	}
	    	else if(btn.id == ID_PERSONALIZE)
	    	{
	    		personalize();
	    	}
	    	else if(btn.id == ID_HELP)
	    	{
	    		helpPage++;
	    		if(helpPage >= help.size())
	    		{
	    			helpPage = 0;
	    		}
	    	}
	    	else if(btn.id == ID_CANCEL)
	    	{
	    		if((adding || renaming) && view == VIEW_CATEGORIES && searchBar.isFocused())
	    		{
					searchBar.setText("");
					onSearchBarInteract();
	
	    			updateButtonList();
	    		}
	    		else if(!justClickedButton && !selectedButtonName.equalsIgnoreCase(""))
	    		{
	    			selectedButtonName = "";
	    			updateButtonList();
	    		}
	    	}
	    	else if(btn.id == ID_ADD)
	    	{
	    		if(!justClickedButton)
	    		{
	    			if(view == VIEW_CATEGORY && !category.equalsIgnoreCase("Favourites") && !category.equalsIgnoreCase("Contributors"))
	    			{
	    				HatHandler.removeFromCategory(selectedButtonName, category);
	    				
	    				showCategory(category);
	    			}
	    			else if(view == VIEW_HATS || view == VIEW_CATEGORY)
	    			{
	    				addingToCategory = true;
	    				
	    				showCategories();
	    			}
	    			else
	    			{
			    		if((adding || renaming) && view == VIEW_CATEGORIES && searchBar.isFocused())
			    		{
			    			if(adding && addCategory(searchBar.getText().trim()) || renaming && renameCategory(selectedButtonName, searchBar.getText().trim()))
			    			{
			    				searchBar.setText("");
			    				onSearchBarInteract();
			    				
			    				updateButtonList();
			    			}
			    		}
	    			}
	    		}
	    	}
	    	else if(btn.id == ID_DELETE)
	    	{
	    		if(!justClickedButton && !selectedButtonName.equalsIgnoreCase(""))
	    		{
	    			if(!deleting)
	    			{
	    				deleting = true;
	    			}
	    			else
	    			{
	    				deleting = false;
	    				
	    				try
	    				{
	    					if(view == VIEW_CATEGORIES)
	    					{
		    					File dir = new File(HatHandler.hatsFolder, "/" + selectedButtonName);
		    					if(dir.exists() && dir.isDirectory())
		    					{
		    						File[] files = dir.listFiles();
		    						for(File file: files)
		    						{
		    							File hat = new File(HatHandler.hatsFolder, file.getName());
		    							if(!hat.isDirectory() && hat.getName().endsWith(".tcn"))
		    							{
			    							if(!hat.exists())
			    							{
			    								file.renameTo(hat);
			    							}
			    							file.delete();
		    							}
		    						}
		    						if(!dir.delete())
		    						{
		    							Hats.console("Cannot delete category \"" + selectedButtonName + "\", directory is not empty!", true);
		    						}
		    					}
	    					}
	    					if(view == VIEW_HATS || view == VIEW_CATEGORY)
	    					{
	    						HatHandler.deleteHat(selectedButtonName, isShiftKeyDown());
	    					}
	    				}
	    				catch(Exception e)
	    				{
	    					Hats.console("Failed to delete " + (view == VIEW_CATEGORIES ? "category" : "hat") +": " + selectedButtonName, true);
	    				}
	    				
	    				selectedButtonName = "";
	    				updateButtonList();
	    				
	    				reloadHatsAndReopenGUI();
	    			}
	    		}
	    	}
	    	else if(btn.id == ID_RENAME)
	    	{
	    		if(!justClickedButton && !selectedButtonName.equalsIgnoreCase(""))
	    		{
	    			renaming = true;
	    			
	    			for(int i = buttonList.size() - 1; i >= 0; i--)
	    			{
	    				GuiButton button = (GuiButton)buttonList.get(i);
	    				if(button.id == ID_RENAME || button.id == ID_DELETE || button.id == ID_CANCEL)
	    				{
	    					buttonList.remove(i);
	    				}
	    			}
	    			
	    			buttonList.add(new GuiButton(ID_ADD, btn.xPosition + 16 - 7, btn.yPosition, 20, 20, ""));
	    			buttonList.add(new GuiButton(ID_CANCEL, btn.xPosition + 52 - 7, btn.yPosition, 20, 20, ""));
	
	    			searchBar.setText(selectedButtonName);
	    		}
	    	}
	    	else if(btn.id == ID_FAVOURITE)
	    	{
	    		if(!justClickedButton)
	    		{
		    		if(!selectedButtonName.equalsIgnoreCase("") && HatHandler.isInFavourites(selectedButtonName))
		    		{
		    			HatHandler.removeFromCategory(selectedButtonName, "Favourites");
		    			
		    			if(view == VIEW_CATEGORY && category.equalsIgnoreCase("Favourites"))
		    			{
		    				showCategory("Favourites");
		    			}
		    		}
		    		else
		    		{
		    			HatHandler.addToCategory(selectedButtonName, "Favourites");
		    		}
	    		}
	    	}
	    	else if(btn.id >= ID_HAT_START_ID)
	    	{
	    		justClickedButton = true;
	    		if(isShiftKeyDown())
	    		{
	    			updateButtonList();
	    			
	    			selectedButtonName = btn.displayString;
	    			
	    			for(int i = buttonList.size() - 1; i >= 0 ; i--)
	    			{
	    				GuiButton button = (GuiButton)buttonList.get(i);
	    				if(button.id == btn.id)
	    				{
	    					buttonList.remove(button);
	    					break;
	    				}
	    			}
	    			
	    			buttonList.add(new GuiButton(ID_ADD, btn.xPosition + 1, btn.yPosition, 20, 20, ""));
	    			buttonList.add(new GuiButton(ID_FAVOURITE, btn.xPosition + 23, btn.yPosition, 20, 20, ""));
	    			GuiButton btn1 = new GuiButton(ID_DELETE, btn.xPosition + 45, btn.yPosition, 20, 20, "");
	    			if(HatHandler.isContributor(selectedButtonName))
	    			{
	    				btn1.enabled = false;
	    			}
	    			buttonList.add(btn1);
	    			buttonList.add(new GuiButton(ID_CANCEL, btn.xPosition + 67, btn.yPosition, 20, 20, ""));
	    		}
	    		else
	    		{
		    		hat.hatName = btn.displayString.toLowerCase();
		    		
		    		colourR = colourG = colourB = 255;
		    		hat.setR(255);
		    		hat.setG(255);
		    		hat.setB(255);
		    		
		    		updateButtonList();
	    		}
	    	}
	    	else if(btn.id >= ID_CATEGORIES_START)
	    	{
	    		justClickedButton = true;
	    		if(addingToCategory)
	    		{
	    			HatHandler.addToCategory(selectedButtonName, btn.displayString);
	    			
	    			view = VIEW_COLOURIZER;
	    			category = "";
	    			
	    			toggleHatsColourizer();
	    		}
	    		else
	    		{
		    		if(btn.displayString.equalsIgnoreCase("Add New"))
		    		{
		    			updateButtonList();
		
		    			for(int i = buttonList.size() - 1; i >= 0 ; i--)
		    			{
		    				GuiButton button = (GuiButton)buttonList.get(i);
		    				if(button.id == btn.id)
		    				{
		    					buttonList.remove(button);
		    					break;
		    				}
		    			}
		    			
		    			buttonList.add(new GuiButton(ID_ADD, btn.xPosition + 16, btn.yPosition, 20, 20, ""));
		    			buttonList.add(new GuiButton(ID_CANCEL, btn.xPosition + 52, btn.yPosition, 20, 20, ""));
		    			
		    			adding = true;
		    			
		    			searchBar.setText("");
		    		}
		    		else if(isShiftKeyDown() && !btn.displayString.equalsIgnoreCase("All Hats") && !btn.displayString.equalsIgnoreCase("Contributors"))
		    		{
		    			updateButtonList();
		    			
		    			selectedButtonName = btn.displayString;
		    			
		    			for(int i = buttonList.size() - 1; i >= 0 ; i--)
		    			{
		    				GuiButton button = (GuiButton)buttonList.get(i);
		    				if(button.id == btn.id)
		    				{
		    					buttonList.remove(button);
		    					break;
		    				}
		    			}
		    			
		    			buttonList.add(new GuiButton(ID_RENAME, btn.xPosition + 7, btn.yPosition, 20, 20, ""));
		    			buttonList.add(new GuiButton(ID_DELETE, btn.xPosition + 34, btn.yPosition, 20, 20, ""));
		    			buttonList.add(new GuiButton(ID_CANCEL, btn.xPosition + 61, btn.yPosition, 20, 20, ""));
		    		}
		    		else
		    		{
		    			showCategory(btn.displayString);
		    		}
		    	}
	    	}
    	}
    }
    
    public void exitAndUpdate()
    {
    	confirmed = true;
    	
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
	        	
	        	Hats.proxy.tickHandlerClient.playerWornHats.put(player.username, new HatInfo(hat.hatName, colourR, colourG, colourB));
	        	
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
    	adding = false;
    	deleting = false;
    	renaming = false;
    	if(view != VIEW_CATEGORIES)
    	{
    		addingToCategory = false;
    	}
    	
        for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            if(btn.id >= 5 && btn.id <= 7 || btn.id >= ID_CATEGORIES_START || btn.id == ID_ADD || btn.id == ID_CANCEL || btn.id == ID_RENAME || btn.id == ID_DELETE || btn.id == ID_FAVOURITE)
            {
            	buttonList.remove(k1);
            }
            else if(btn.id == ID_PAGE_LEFT)
            {
	            if(pageNumber == 0 || view == VIEW_COLOURIZER || personalizing)
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
        		if((pageNumber + 1) * 6 >= hatsToShow.size() || view == VIEW_COLOURIZER || personalizing)
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
            else if(btn.id == ID_CATEGORIES)
            {
            	if(view == VIEW_CATEGORIES)
            	{
            		btn.enabled = false;
            	}
            	else
            	{
            		btn.enabled = true;
            	}
            }
            else if(btn.id == ID_FAVOURITES)
            {
            	if(view == VIEW_CATEGORY && category.equalsIgnoreCase("Favourites"))
            	{
            		btn.enabled = false;
            	}
            	else
            	{
            		btn.enabled = true;
            	}
            }
        }
        
        if(!personalizing)
        {
	    	if(view == VIEW_HATS || view == VIEW_CATEGORIES || view == VIEW_CATEGORY)
	    	{
		    	int button = 0;
		
		        for(int i = pageNumber * 6; i < hatsToShow.size() && i < (pageNumber + 1) * 6; i++)
		        {
		        	GuiButton btn;
		        	String hatName = (String)hatsToShow.get(i);
		        	
		        	btn = new GuiButton((view == VIEW_HATS || view == VIEW_CATEGORY ? ID_HAT_START_ID + i : ID_CATEGORIES_START + i), width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, hatName);
		        	
		        	if((view == VIEW_HATS || view == VIEW_CATEGORY) && hatName.toLowerCase().equalsIgnoreCase(hat.hatName))
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
		        
		        int pageCount = (int)Math.ceil((float)hatsToShow.size() / 6F);
		        if(pageCount <= 0)
		        {
		        	pageCount = 1;
		        }
		        currentDisplay = (view == VIEW_HATS ? "All Hats" : view == VIEW_CATEGORY ? "Category - " + category : "Categories") + " (" + (pageNumber + 1) + "/" + (pageCount) + ")";
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
	    		
	    		currentDisplay = "Colourizer";
	    	}
        }
        else
        {
        	buttonList.add(new GuiButton(ID_SET_KEY, width / 2 - 6, height / 2 - 78, 88, 20, "GUI: " + (Hats.guiKeyBind < 0 ? Mouse.getButtonName(Hats.guiKeyBind + 100) : Keyboard.getKeyName(Hats.guiKeyBind))));
        	buttonList.add(new GuiButton(ID_SET_FP, width / 2 - 6, height / 2 - 78 + 22, 88, 20, "First Person: " + (Hats.renderInFirstPerson == 1 ? "Yes" : "No")));
        	buttonList.add(new GuiButton(ID_SHOW_HATS, width / 2 - 6, height / 2 - 78 + (22 * 2), 88, 20, "Show Hats: " + (Hats.renderHats == 1 ? "Yes" : "No")));
        	buttonList.add(new GuiButton(ID_RESET_SIDE, width / 2 - 6, height / 2 - 78 + (22 * 5), 88, 20, "Reset Side?"));
        	if(Hats.proxy.tickHandlerClient.serverHatMode != 4)
        	{
        		buttonList.add(new GuiSlider(ID_MOB_SLIDER, width / 2 - 6, height / 2 - 78 + (22 * 3), "RandoMobs: ", 0, 100, Hats.randomMobHat, this, "%"));
        	}
        	
        	currentDisplay = "Personalize";
        }
    }
    
    public void removeHat()
    {
		hat.hatName = "";
		
		updateButtonList();
    }
    
    public void reloadHatsAndReopenGUI()
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
    
    public void switchPage(boolean left)
    {
    	if(left)
    	{
    		pageNumber--;
    		if(pageNumber < 0)
    		{
    			pageNumber = 0;
    		}
    		updateButtonList();
    	}
    	else
    	{
    		pageNumber++;
    		if(pageNumber * 6 >= hatsToShow.size())
    		{
    			pageNumber--;
    		}
    		updateButtonList();
    	}
    }
    
    public void toggleHatsColourizer()
    {
    	if(view == VIEW_COLOURIZER && !isShiftKeyDown() && !category.equalsIgnoreCase(""))
    	{
    		view = VIEW_CATEGORY;
    	}
    	else if(view == VIEW_CATEGORY && !isShiftKeyDown())
    	{
    		view = VIEW_COLOURIZER;
    	}
    	else
    	{
    		view = view > VIEW_HATS ? VIEW_HATS : VIEW_COLOURIZER;
    	}
		
		hatsToShow = new ArrayList<String>(view == VIEW_HATS ? availableHats : categoryHats);
		Collections.sort(hatsToShow);

		searchBar.setText("");
		onSearchBarInteract();

		updateButtonList();
    }
    
    public boolean addCategory(String s)
    {
    	if(invalidFolderName)
    	{
    		return false;
    	}
    	try
    	{
    		File file = new File(HatHandler.hatsFolder, "/" + s);
    		if(!file.mkdirs())
    		{
    			return false;
    		}
    		categories.add(s);
    		Collections.sort(categories);
    		HatHandler.categories.put(s, new ArrayList<String>());
    		
	    	hatsToShow = new ArrayList<String>(categories);
	    	Collections.sort(hatsToShow);
			hatsToShow.add(0, "All Hats");
			hatsToShow.add("Add New");

    		return true;
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return false;
    }
    
    public boolean renameCategory(String oriName, String newName)
    {
    	if(invalidFolderName)
    	{
    		return false;
    	}
    	try
    	{
    		File oriCat = new File(HatHandler.hatsFolder, "/" + oriName);
    		File newCat = new File(HatHandler.hatsFolder, "/" + newName);
    		if(!oriCat.exists())
    		{
    			return false;
    		}
    		if(!oriCat.renameTo(newCat))
    		{
    			return false;
    		}
 
    		reloadHatsAndReopenGUI();
 
    		return true;
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return false;
    }
    
    public void showCategory(String s)
    {
    	if(s.equalsIgnoreCase("All Hats"))
    	{
    		view = VIEW_COLOURIZER;
    		category = "";
    		
    		toggleHatsColourizer();
    	}
    	else
    	{
    		pageNumber = 0;
    		
	    	view = VIEW_CATEGORY;
	    	
	    	category = s;
	    	
	    	ArrayList<String> hatsList = HatHandler.categories.get(s);
	    	if(hatsList == null)
	    	{
	    		hatsList = new ArrayList<String>();
	    	}
	    	
	    	ArrayList<String> hatsCopy = new ArrayList<String>(hatsList);
	    	
	    	if(Hats.proxy.tickHandlerClient.serverHatMode == 4 && !mc.thePlayer.capabilities.isCreativeMode)
	    	{
	    		for(int i = hatsCopy.size() - 1; i >= 0; i--)
	    		{
	    			String hatName = hatsCopy.get(i);
	    			if(!Hats.proxy.tickHandlerClient.serverHats.contains(hatName))
	    			{
	    				hatsCopy.remove(i);
	    			}
	    		}
	    	}
	    	
	    	categoryHats = new ArrayList<String>(hatsCopy);
	    	
	    	hatsToShow = new ArrayList<String>(hatsCopy);
	    	Collections.sort(hatsToShow);
	    	
	    	updateButtonList();
    	}
    }
    
    public void randomize()
    {
		if(view == VIEW_HATS || view == VIEW_CATEGORY)
		{
	    	if(hatsToShow.size() > 0)
	    	{
				int randVal = rand.nextInt(hatsToShow.size());
	        	String hatName = (String)hatsToShow.get(randVal);
	        	
	        	hat.hatName = hatName.toLowerCase();
	        	
	    		colourR = colourG = colourB = 255;
	    		hat.setR(255);
	    		hat.setG(255);
	    		hat.setB(255);
	        	
	    		pageNumber = randVal / 6;
	    		
	    		if(isShiftKeyDown())
	    		{
	    			view = VIEW_COLOURIZER;
	    			
	    			updateButtonList();
	    			
	    			randomizeColour();
	    			
	    			view = VIEW_HATS;
	    		}
	    		
	    		updateButtonList();
	    	}
		}
		else if(view == VIEW_COLOURIZER)
		{
			if(isShiftKeyDown())
			{
	    		colourR = colourG = colourB = 255;
	    		hat.setR(255);
	    		hat.setG(255);
	    		hat.setB(255);

	    		updateButtonList();
			}
			else
			{
				randomizeColour();
			}
		}
		else if(view == VIEW_CATEGORIES)
		{
	    	if(hatsToShow.size() > 0)
	    	{
				int randVal = rand.nextInt(hatsToShow.size());
				String categoryName = (String)hatsToShow.get(randVal);
				showCategory(categoryName);
	    	}
		}
    }
    
    public void randomizeColour()
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
    
    public void showCategories()
    {
    	if(view != VIEW_CATEGORIES)
    	{
	    	view = VIEW_CATEGORIES;
	    	pageNumber = 0;
	    	
			searchBar.setText("");
			onSearchBarInteract();
	    	
	    	hatsToShow = new ArrayList<String>(categories);
	    	Collections.sort(hatsToShow);
	    	if(!addingToCategory)
	    	{
				hatsToShow.add(0, "All Hats");
				hatsToShow.add("Add New");
	    	}
	    	else
	    	{
	    		hatsToShow.remove("Contributors");
	    	}

	    	updateButtonList();
    	}
    }
    
    public void personalize()
    {
		showCategory("All Hats");

    	if(!personalizing)
    	{
    		personalizing = true;
    		
    		randoMob = Hats.randomMobHat;
    		
    		for(int i = buttonList.size() - 1; i >= 0 ; i--)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(i);
    			
    			if(btn.id >= ID_NONE && btn.id <= ID_HELP)
    			{
    				btn.drawButton = true;
    			}
    			if(btn.id == ID_SEARCH)
    			{
    				buttonList.remove(i);
    			}
    		}
    		
    		if(!enabledSearchBar)
    		{
				GuiButton btn = new GuiButton(ID_SEARCH, width - 24, height / 2 - 93 + (8 * 21), 20, 20, "");
				buttonList.add(btn);
    		}
    		updateButtonList();
    	}
    	else
    	{
    		personalizing = false;
    		
    		for(int i = buttonList.size() - 1; i >= 0 ; i--)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(i);
    			
    			if(btn.id >= ID_NONE && btn.id <= ID_HELP)
    			{
    				if(!enabledButtons.contains(Integer.toString(btn.id - (ID_NONE - 1))))
    				{
    					btn.drawButton = false;
    				}
    			}
    			if(btn.id >= ID_SET_KEY && btn.id <= ID_MOB_SLIDER || btn.id == ID_SEARCH)
    			{
    				buttonList.remove(i);
    			}
    		}
    		
    		StringBuilder sb = new StringBuilder();
    		for(int i = 0; i < enabledButtons.size(); i++)
    		{
    			sb.append(enabledButtons.get(i));
    			sb.append(" ");
    		}
    		if(enabledSearchBar)
    		{
    			sb.append("9");
    		}
    		
    		Hats.enabled = sb.toString().trim();
    		Hats.randomMobHat = randoMob;
    		
    		Hats.handleConfig();
    		
    		updateButtonList();
    	}
    }
    
    public void toggleSearchBar()
    {
    	if(enabledSearchBar)
    	{
    		enabledSearchBar = false;
    		searchBar.setVisible(false);
    		
			GuiButton btn = new GuiButton(ID_SEARCH, width - 24, height / 2 - 93 + (8 * 21), 20, 20, "");
			buttonList.add(btn);
    	}
    	else
    	{
    		enabledSearchBar = true;
    		searchBar.setVisible(true);
    	}
    }
    
    public void toggleVisibility(GuiButton btn)
    {
    	if(btn.id == ID_SEARCH)
    	{
    		toggleSearchBar();
    	}
    	else if(enabledButtons.contains(Integer.toString(btn.id - (ID_NONE - 1))))
    	{
    		enabledButtons.remove(Integer.toString(btn.id - (ID_NONE - 1)));
    		btn.xPosition = width - 24;
    		btn.yPosition = height / 2 - 93 + ((btn.id - ID_NONE) * 21);
    		for(int i = 0; i < buttonList.size(); i++)
    		{
    			GuiButton btn1 = (GuiButton)buttonList.get(i);
    			if(enabledButtons.contains(Integer.toString(btn1.id - (ID_NONE - 1))))
    			{
    				for(int i1 = 0; i1 < enabledButtons.size(); i1++)
    				{
    					if(Integer.toString(btn1.id - (ID_NONE - 1)).equalsIgnoreCase(enabledButtons.get(i1)))
    					{
		    				btn1.xPosition = width / 2 + 89;
		    				btn1.yPosition = height / 2 - 85 + (i1 * 21);
		    				break;
    					}
    				}
    			}
    		}
    	}
    	else
    	{
    		if(!enabledButtons.contains(Integer.toString(btn.id - (ID_NONE - 1))))
    		{
    			enabledButtons.add(Integer.toString(btn.id - (ID_NONE - 1)));
    			btn.xPosition = width / 2 + 89;
    			btn.yPosition = height / 2 - 85 + ((enabledButtons.size() - 1) * 21);
    		}
    	}
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	if(mc == null)
    	{
    		mc = Minecraft.getMinecraft();
    		fontRenderer = mc.fontRenderer;
    	}
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.func_110434_K().func_110577_a(hats.client.gui.GuiHatSelection.texChooser);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
        this.mc.func_110434_K().func_110577_a(hats.client.gui.GuiHatSelection.texIcons);

        if(personalizing)
        {
        	for(int l1 = 0; l1 < 8; l1++)
        	{
        		this.drawTexturedModalRect(k + 176, l - 1 + (l1 * 21), 190, 16, 22, 22);
        	}
        	this.drawTexturedModalRect(k - 1, height - 29, 0, 16, 190, 29);
        }
        
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            String disp = btn.displayString;
            
            if(btn.id >= ID_CATEGORIES_START)
            {
            	int id = btn.id >= ID_HAT_START_ID ? btn.id - ID_HAT_START_ID : btn.id - ID_CATEGORIES_START; 
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
            
            if(btn.id == ID_HAT_COLOUR_SWAP || btn.id == ID_NONE || btn.id == ID_RANDOM || btn.id == ID_HELP || btn.id == ID_RELOAD_HATS || btn.id == ID_FAVOURITES || btn.id == ID_CATEGORIES || btn.id == ID_PERSONALIZE || btn.id == ID_ADD || btn.id == ID_CANCEL || btn.id == ID_RENAME || btn.id == ID_DELETE || btn.id == ID_FAVOURITE || btn.id == ID_SEARCH)
            {
            	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            
	            this.mc.func_110434_K().func_110577_a(hats.client.gui.GuiHatSelection.texIcons);

	            if(btn.drawButton)
	            {
	            	if(btn.id == ID_HAT_COLOUR_SWAP)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, (view == VIEW_HATS || view == VIEW_CATEGORY ? 16 : 0), 0, 16, 16);
	            	}
	            	else if(btn.id == ID_NONE || btn.id == ID_CANCEL)
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
	            	else if(btn.id == ID_RELOAD_HATS)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 48, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_FAVOURITES)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2 - (favourite > 3 ? 6 - favourite : favourite), 64, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_CATEGORIES)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 112, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_PERSONALIZE)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 176, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_ADD)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, view == VIEW_CATEGORY && !category.equalsIgnoreCase("Favourites") && !category.equalsIgnoreCase("Contributors") ? 224 : 160, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_RENAME)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 192, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_DELETE)
	            	{
	            		if((view == VIEW_CATEGORY || view == VIEW_HATS) && isShiftKeyDown())
	            		{
	            			drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 208, 0, 16, 16);
	            		}
	            		else
	            		{
	            			drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 144, 0, 16, 16);
	            		}
	            	}
	            	else if(btn.id == ID_FAVOURITE)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 64, 0, 16, 16);
		            	if(!selectedButtonName.equalsIgnoreCase("") && HatHandler.isInFavourites(selectedButtonName))
		            	{
		            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 32, 0, 16, 16);
		            	}
	            	}
	            	else if(btn.id == ID_SEARCH)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 128, 0, 16, 16);
	            	}
	            }

            	GL11.glDisable(GL11.GL_BLEND);
            }
        }
        
        drawString(fontRenderer, "Viewing: " + currentDisplay, this.guiLeft, this.guiTop - 9, 0xffffff);
    	
        this.mouseX = (float)par1;
        this.mouseY = (float)par2;

        drawSearchBar();
        
        drawPlayerOnGui(k + 42, l + 155, 55, (float)(k + 42) - (float)mouseX, (float)(l + 155 - 92) - (float)mouseY);

        drawForeground(par1, par2, par3);
    }
    
    public void drawForeground(int par1, int par2, float par3)
    {
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            if(btn.func_82252_a() && !personalizing)
            {
	            if(btn.id >= ID_CATEGORIES_START && btn.displayString.length() > 16)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"\u00a77" + btn.displayString}), par1, par2);
	            }
	            else if(btn.id == ID_CLOSE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Discard changes?"}), par1, par2);
	            }
	            else if(btn.id == ID_NONE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Remove hat (N)"}), par1, par2);
	            }
	            else if(btn.id == ID_HAT_COLOUR_SWAP)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS || view == VIEW_CATEGORY ? "Colourizer (H)" : "Hats List (H)")}), par1, par2);
	            }
	            else if(btn.id == ID_RANDOM)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS || view == VIEW_CATEGORY ? "Random Hat (R)" : view == VIEW_CATEGORIES ? "Random Category (R)" : isShiftKeyDown() ? "Reset Colour (R)" : "Random Colour (R)")}), par1, par2);
	            }
	            else if(btn.id == ID_HELP)
	            {
	            	drawTooltip(getCurrentHelpText(), par1, par2);
	            }
	            else if(btn.id == ID_RELOAD_HATS)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Reload all hats?", "This will discard all changes."}), par1, par2);
	            }
	            else if(btn.id == ID_FAVOURITES)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Favourites (F)"}), par1, par2);
	            }
	            else if(btn.id == ID_CATEGORIES)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Categories (C)"}), par1, par2);
	            }
	            else if(btn.id == ID_PERSONALIZE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Personalize (P)"}), par1, par2);
	            }
	            else if(btn.id == ID_ADD)
	            {
	            	drawTooltip(Arrays.asList(new String[] {adding || renaming ? invalidFolderName ? "\u00a7cInvalid Name!" : "Add Category" : (view == VIEW_HATS || view == VIEW_CATEGORY && (category.equalsIgnoreCase("Favourites") || category.equalsIgnoreCase("Contributors"))) ? "Add to category" : "Remove from category"}), par1, par2);
	            }
	            else if(btn.id == ID_CANCEL)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Cancel"}), par1, par2);
	            }
	            else if(btn.id == ID_DELETE)
	            {
	            	if(deleting)
	            	{
	            		drawTooltip(Arrays.asList(new String[] {"Are you sure?", "", "Click again to confirm"}), par1, par2);
	            	}
	            	else if(view == VIEW_CATEGORIES)
	            	{
	            		drawTooltip(Arrays.asList(new String[] {"Delete category?", "", "This will remove all hats", "in this category and reload", "the GUI.", "", "Double click to confirm"}), par1, par2);
	            	}
	            	else
	            	{
	            		if(isShiftKeyDown())
	            		{
	            			drawTooltip(Arrays.asList(new String[] {"Disable hat?", "", "This will disable this hat from", "all categories and reload", "the GUI. This will not", "delete the file.", "", "Double click to confirm"}), par1, par2);
	            		}
	            		else
	            		{
	            			drawTooltip(Arrays.asList(new String[] {"Delete hat?", "", "This will remove this hat from", "all categories and reload", "the GUI.", "", "Double click to confirm"}), par1, par2);
	            		}
	            	}
	            }
	            else if(btn.id == ID_RENAME)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Rename Category?", "", "This will reload the GUI!"}), par1, par2);
	            }
	            else if(btn.id == ID_FAVOURITE)
	            {
	            	if(!selectedButtonName.equalsIgnoreCase("") && HatHandler.isInFavourites(selectedButtonName))
	            	{
	            		drawTooltip(Arrays.asList(new String[] {"Remove from Favourites"}), par1, par2);
	            	}
	            	else
	            	{
	            		drawTooltip(Arrays.asList(new String[] {"Add to Favourites"}), par1, par2);
	            	}
	            }
	            else if(btn.id == ID_RESET_SIDE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"Reset buttons on side of GUI?"}), par1, par2);
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
	    	if(hat == null || hat.renderingParent == null)
	    	{
	    		return;
	    	}
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
	        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        float f2 = hat.renderingParent.renderYawOffset;
	        float f3 = hat.renderingParent.rotationYaw;
	        float f4 = hat.renderingParent.rotationPitch;
	        
	        float ff3 = hat.rotationYaw;
	        float ff4 = hat.rotationPitch;
	        
	        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        
	        hat.renderingParent.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
	        hat.renderingParent.rotationYaw = hat.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
	        hat.renderingParent.rotationPitch = hat.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
	        hat.renderingParent.rotationYawHead = hat.renderingParent.rotationYaw;
	        GL11.glTranslatef(0.0F, hat.renderingParent.yOffset, 0.0F);
	        
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(hat.renderingParent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
	        Tessellator.instance.setBrightness(240);
	        GL11.glDisable(GL11.GL_LIGHTING);
	        int rend = Hats.renderHats;
	        Hats.renderHats = 1;
	        if(hat.parent == Minecraft.getMinecraft().renderViewEntity && hat.renderingParent != hat.parent)
	        {
	        	GL11.glTranslatef((float)HatHandler.getHorizontalPosOffset(hat.renderingParent), hat.parent.yOffset * 2, 0.0F);
	        }
	        RenderManager.instance.renderEntityWithPosYaw(hat, 0.0D, 0.0D, 0.0D, hat.rotationYaw, 1.0F);
	        Hats.renderHats = rend;
	        GL11.glEnable(GL11.GL_LIGHTING);
	        
	        hat.renderingParent.renderYawOffset = f2;
	        hat.renderingParent.rotationYaw = f3;
	        hat.renderingParent.rotationPitch = f4;
	        
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
    
    public void drawSearchBar()
    {
    	if(searchBar.getVisible())
    	{
	    	searchBar.drawTextBox();
	    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	
	        this.mc.func_110434_K().func_110577_a(hats.client.gui.GuiHatSelection.texIcons);
	    	
	    	drawTexturedModalRect(this.width / 2 - 85, height - 22, (adding || renaming) ? 112 : 128, 0, 16, 16);
	    	
	    	GL11.glDisable(GL11.GL_BLEND);
	    	
	    	if((adding || renaming) && searchBar.getText().equalsIgnoreCase(""))
	    	{
	    		fontRenderer.drawString("Category Name?", this.width / 2 - 61, height - 18, 0xAAAAAA);
	    	}
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
		else if(slider.id == ID_MOB_SLIDER)
		{
			randoMob = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
		}
	}

	private static final String[] invalidChars = new String[] { "\\", "/", ":", "*", "?", "\"", "<", ">", "|" };
	
	private static int helpPage = 0;
	private static final ArrayList<String[]> help = new ArrayList<String[]>();
	private static final String[] helpInfo1 = new String[] {"Shift click on the Hat", "button for more options!", "You can't shift click on", "the hat you're wearing", "however."};
	private static final String[] helpInfo2 = new String[] {"If you're on a server that doesn't have", "the mod, another player with the", "mod won't be able to see your hat."};
	private static final String[] helpInfo3 = new String[] {"Did you know you can always get more hats?", "Techne Online has a bunch of", "community made hats that you", "can download and install."};
	private static final String[] helpInfo4 = new String[] {"You can also make your own hat!", "You need Techne and you make a hat with it.", "A \"head\" model is placed in the middle", "of the wood block. It's size is", "8 x 8 x 8. Make your hat on it!"};
	private static final String[] helpInfo5 = new String[] {"Did you know that if your friend doesn't", "have the hat you're wearing, you send the hat", "to the server, and it gets sent to your friend.", "(Only on servers with the mod)"};
	private static final String[] helpInfo6 = new String[] {"Shift clicking the Random Hat button gives you", "a random hat AND colour!"};
	private static final String[] helpInfo7 = new String[] {"Shift clicking the Random Colour button resets", "all colours."};
	private static final String[] helpInfo8 = new String[] {"You can hit TAB or T to quickly select", "the search bar."};
	private static final String[] helpInfo9 = new String[] {"Did you know that this mod was initially", "made for a 96 hour modding marathon", "called ModJam?", "", "It won second place and was made and is", "currently maintained by iChun."};
	private static final String[] helpInfo10 = new String[] {"The player hats added by the mod are the", "names of some people who made a significant", "contribution to the mod, in terms", "of development, testing, model contribution", "and support.", "", "Direwolf20 is just a bonus however."};
	private static final String[] helpInfo11 = new String[] {"Shift Clicking on the Hats List will take", "you out of a category and back to all", "the hats."};
	private static final String[] helpInfo12 = new String[] {"Did you know you can make your own categories?", "Hit the Categories button (or C, on your keyboard)", "and hit the \"Add New\" button."};
	private static final String[] helpInfo13 = new String[] {"You can right click the Search bar", "to clear it."};
	private static final String[] helpInfo14 = new String[] {"You can disable hats rather than deleting", "them. Just hold shift when deleting."};
	private static final String[] helpInfo15 = new String[] {"Hitting Shift-F will favourite", "or unfavourite the hat you", "are currently wearing."};
	private static final String[] helpInfo16 = new String[] {"The hot key for \"Personalize\" is", "\"P\", in case you accidentally", "disabled the button."};
	private static final String[] helpInfo17 = new String[] {"You can get your own custom", "hat added to the mod if", "you give us a donation!", "", "Check the mod page for info."};
	private static final String[] helpInfo18 = new String[] {"The (C) in the name of the hat", "represents a hat added as a thank", "you for a donation!"};
	private static final String[] helpInfo19 = new String[] {"Did you know you can customize", "if mobs have hats? Its in the", "\"Personalize\" tab"};
	private static final String[] helpInfo20 = new String[] {"If you have a contributor hat,", "you can use it in Hat", "Hunting Mode."};
	
	static
	{
		help.add(helpInfo1);
		help.add(helpInfo2);
		help.add(helpInfo3);
		help.add(helpInfo4);
		help.add(helpInfo5);
		help.add(helpInfo6);
		help.add(helpInfo7);
		help.add(helpInfo8);
		help.add(helpInfo9);
		help.add(helpInfo10);
		help.add(helpInfo11);
		help.add(helpInfo12);
		help.add(helpInfo13);
		help.add(helpInfo14);
		help.add(helpInfo15);
		help.add(helpInfo16);
		help.add(helpInfo17);
		help.add(helpInfo18);
		help.add(helpInfo19);
		help.add(helpInfo20);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		helpPage = calendar.get(5) % help.size();
	}
	
	private static String getHelpHeader()
	{
		return "\u00a7c" + "Protip! (" + Integer.toString(helpPage + 1) + "/" + Integer.toString(help.size()) + ")";
	}
	
	private static List getCurrentHelpText()
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add(getHelpHeader());
		list.add("");
		
		String[] str = help.get(helpPage);
		for(int i = 0; i < str.length; i++)
		{
			list.add(str[i]);
		}
		
		return list;
	}
}
