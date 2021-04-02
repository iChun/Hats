package me.ichun.mods.hats.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class ModelHat extends Model //A VERY dumbed down version of ModelTabula
{
    public final @Nonnull HatInfo info;
    public ArrayList<ModelRenderer> models = new ArrayList<>();
    public HashMap<ModelRenderer, Project.Part> partMap = new HashMap<>();

    public ModelHat(@Nonnull HatInfo info)
    {
        super(RenderType::getEntityTranslucentCull);
        this.info = info;

        textureWidth = info.project.texWidth;
        textureHeight = info.project.texHeight;

        info.project.parts.forEach(part -> populateModel(models, part));
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder iVertexBuilder, int light, int overlay, float r, float g, float b, float alpha)
    {
        partMap.forEach(((modelRenderer, part) -> modelRenderer.showModel = part.showModel));

        for(ModelRenderer model : models)
        {
            model.render(matrixStack, iVertexBuilder, light, overlay, r, g, b, alpha);
        }
    }

    public void populateModel(Collection<? super ModelRenderer> parts, Project.Part part)
    {
        int[] dims = part.getProjectTextureDims();
        if(!part.matchProject)
        {
            dims[0] = part.texWidth;
            dims[1] = part.texHeight;
        }
        ModelRenderer modelPart = new ModelRenderer(dims[0], dims[1], part.texOffX, part.texOffY);
        modelPart.rotationPointX = part.rotPX;
        modelPart.rotationPointY = part.rotPY;
        modelPart.rotationPointZ = part.rotPZ;

        modelPart.rotateAngleX = (float)Math.toRadians(part.rotAX);
        modelPart.rotateAngleY = (float)Math.toRadians(part.rotAY);
        modelPart.rotateAngleZ = (float)Math.toRadians(part.rotAZ);

        modelPart.mirror = part.mirror;
        modelPart.showModel = part.showModel;

        int texOffX = modelPart.textureOffsetX;
        int texOffY = modelPart.textureOffsetY;

        for(Project.Part.Box box : part.boxes)
        {
            modelPart.setTextureOffset(texOffX + box.texOffX, texOffY + box.texOffY);
            modelPart.addBox(box.posX, box.posY, box.posZ, box.dimX, box.dimY, box.dimZ, box.expandX, box.expandY, box.expandZ);
        }
        modelPart.setTextureOffset(texOffX, texOffY);

        part.children.forEach(part1 -> populateModel(modelPart.childModels, part1));

        parts.add(modelPart);
        partMap.put(modelPart, part);
    }
}
