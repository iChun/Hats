package hats.client.model;

import java.util.ArrayList;

import ichun.common.core.techne.TC2Info;
import ichun.common.core.techne.model.ModelTechne2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHat extends ModelTechne2
{
	public ModelHat(TC2Info info)
    {
        super(info);

        for(int i = 0; i < boxList.size(); i++)
        {
            ModelRenderer box = (ModelRenderer)boxList.get(i);
            box.rotationPointY -= 16F;
        }
    }
}
