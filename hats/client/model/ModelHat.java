package hats.client.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

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
			
			for(int j = 0; j < node.getAttributes().getLength(); j++)
			{
				Node attribute = node.getAttributes().item(j);

				if(attribute.getNodeName().equalsIgnoreCase("type") && attribute.getNodeValue().equalsIgnoreCase("d9e621f7-957f-4b77-b1ae-20dcd0da7751"))
				{
					try
					{
						boolean mirrored = false;
						String[] offsets;
						String[] positions;
						String[] rotations;
						String[] size;
						String[] textureOffsets;
						for(int k = 0; k < node.getChildNodes().getLength(); k++)
						{
							Node child = node.getChildNodes().item(k);
//							System.out.println(child);
							System.out.println(child.getTextContent());
							if(child.getNodeName().equalsIgnoreCase("IsMirrored"))
							{
//								System.out.println(child.getTextContent());
//								mirrored = !child.getNodeValue().equalsIgnoreCase("False");
							}
							else if(child.getNodeName().equalsIgnoreCase("Offset"))
							{
//								offsets = child.getNodeValue().split(",");
//								System.out.println(offsets.length);
								
							}
						}
//						ModelRenderer cube =
					}
					catch(NumberFormatException e)
					{
						
					}
				}
			}
		}
		
	}
	
}
