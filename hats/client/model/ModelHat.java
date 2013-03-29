package hats.client.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraft.client.model.ModelBase;

public class ModelHat extends ModelBase 
{

	public ArrayList models;
	
	
	public ModelHat(Document doc)
	{
		models = new ArrayList();
		
		String[] textureSizes = doc.getElementsByTagName("TextureSize").item(0).getChildNodes().item(0).getNodeValue().split(",");
		
		try
		{
			textureWidth = Integer.parseInt(textureSizes[0]);
			textureHeight = Integer.parseInt(textureSizes[1]);
		}
		catch(NumberFormatException e)
		{
			textureWidth = 64;
			textureHeight = 32;
		}
		
		NodeList list = doc.getElementsByTagName("Shape");
		
		for(int i = 0; i < list.getLength(); i++)
		{
			Node node = list.item(i);
			
			System.out.println(node.getAttributes().item(1));
			
		}
		
	}
	
}
