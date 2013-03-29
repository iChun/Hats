package hats.client.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHat extends ModelBase 
{

	public ArrayList<ModelRenderer> models;


	public ModelHat(Document doc)
	{
		models = new ArrayList<ModelRenderer>();

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
						String[] offsets = new String[3];
						String[] positions = new String[3];
						String[] rotations = new String[3];
						String[] size = new String[3];
						String[] textureOffsets = new String[2];
						for(int k = 0; k < node.getChildNodes().getLength(); k++)
						{
							Node child = node.getChildNodes().item(k);
							if(child.getNodeName().equalsIgnoreCase("IsMirrored"))
							{
								mirrored = !child.getTextContent().trim().equalsIgnoreCase("False");
							}
							else if(child.getNodeName().equalsIgnoreCase("Offset"))
							{
								offsets = child.getTextContent().trim().split(",");
							}
							else if(child.getNodeName().equalsIgnoreCase("Position"))
							{
								positions = child.getTextContent().trim().split(",");
							}
							else if(child.getNodeName().equalsIgnoreCase("Rotation"))
							{
								rotations = child.getTextContent().trim().split(",");
							}
							else if(child.getNodeName().equalsIgnoreCase("Size"))
							{
								size = child.getTextContent().trim().split(",");
							}
							else if(child.getNodeName().equalsIgnoreCase("TextureOffset"))
							{
								textureOffsets = child.getTextContent().trim().split(",");
							}
						}
						ModelRenderer cube = new ModelRenderer(this, Integer.parseInt(textureOffsets[0]), Integer.parseInt(textureOffsets[1]));
						cube.addBox(Float.parseFloat(offsets[0]), Float.parseFloat(offsets[1]), Float.parseFloat(offsets[2]), Integer.parseInt(size[0]), Integer.parseInt(size[1]), Integer.parseInt(size[2]));
						cube.setRotationPoint(Float.parseFloat(positions[0]), Float.parseFloat(positions[1]) - 32F, Float.parseFloat(positions[2]));
						cube.mirror = mirrored;
						cube.rotateAngleX = Float.parseFloat(rotations[0]);
						cube.rotateAngleY = Float.parseFloat(rotations[1]);
						cube.rotateAngleZ = Float.parseFloat(rotations[2]);

						models.add(cube);
					}
					catch(NumberFormatException e)
					{
					}
				}
			}
		}
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		for(ModelRenderer cube : models)
		{
			float rotY = cube.rotationPointY;
			cube.rotationPointY += 0.6F;

			cube.renderWithRotation(f5);

			cube.rotationPointY = rotY;
		}
	}

}
