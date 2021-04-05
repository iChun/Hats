package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.client.gui.window.element.ElementHatsScrollView;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.hats.sort.SortHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WindowAllHats extends Window<WorkspaceHats>
{
    public int age;

    public WindowAllHats(WorkspaceHats parent)
    {
        super(parent);

        disableDockingEntirely();
        disableDrag();
        disableTitle();

        setView(new ViewAllHats(this));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(age <= Hats.configClient.guiAnimationTime)
        {
            float prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((age + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            float revProg = 1F - prog;

            int padding = 10;

            constraints().left(parent, Constraint.Property.Type.LEFT, (int)(padding + (parent.getWidth() - padding) * revProg)).right(parent, Constraint.Property.Type.RIGHT, (int)(padding + (parent.getWidth() - padding) * revProg))
                    .top(parent, Constraint.Property.Type.TOP, (int)(padding + (parent.getHeight() - padding) * revProg)).bottom(parent, Constraint.Property.Type.BOTTOM, (int)(padding + (parent.getHeight() - padding) * revProg));
            this.resize(parent.getMinecraft(), parent.getWidth(), parent.getHeight());
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef(0F, 0F, 300F);

        super.render(stack, mouseX, mouseY, partialTick);

        RenderSystem.popMatrix();
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;

        if(age == Hats.configClient.guiAnimationTime + 1) //set the constraint and resize
        {
            this.constraint = Constraint.matchParent(this, parent, 10);
            this.resize(parent.getMinecraft(), parent.getWidth(), parent.getHeight());
        }
    }

    public static class ViewAllHats extends View<WindowAllHats>
    {
        public ViewAllHats(@Nonnull WindowAllHats parent)
        {
            super(parent, "hats.gui.window.allHats.title");

            int padding = 8;

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.ok"), elementClickable ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(button);

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.constraints().top(this, Constraint.Property.Type.TOP, padding)
                    .bottom(button, Constraint.Property.Type.TOP, padding) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(sv);

            ElementHatsScrollView list = new ElementHatsScrollView(this).setScrollVertical(sv);
            list.constraints().top(this, Constraint.Property.Type.TOP, padding + 1)
                    .bottom(button, Constraint.Property.Type.TOP, padding + 1)
                    .left(this, Constraint.Property.Type.LEFT, padding + 1)
                    .right(sv, Constraint.Property.Type.LEFT, padding + 1);
            elements.add(list);

            List<HatsSavedData.HatPart> hatPartSource = parent.parent.getHatPartSource();
            SortHandler.sort(Hats.configClient.filterSorters, hatPartSource, false);
            hatPartSource = hatPartSource.stream().filter(hatPart -> {
                HatInfo info = HatResourceHandler.getInfo(hatPart);
                return info == null || info.contributorUUID == null;
            }).collect(Collectors.toList());

            for(HatsSavedData.HatPart hatPart : hatPartSource)
            {
                HatsSavedData.HatPart copyPart = hatPart.createCopy();
                copyPart.hideAll();
                addHatRender(list, copyPart, copyPart);
            }

            int found = 0;
            for(Element<?> element : list.elements)
            {
                if(element instanceof ElementHatRender)
                {
                    if(!(((ElementHatRender<?>)element).hatLevel.count == 0 && ((ElementHatRender<?>)element).hatLevel.hsbiser[0] == 0F && ((ElementHatRender<?>)element).hatLevel.hsbiser[1] == 0F && ((ElementHatRender<?>)element).hatLevel.hsbiser[2] == 1F))
                    {
                        found++;
                    }
                }
            }

            float prog = MathHelper.clamp(found / (float)list.elements.size(), 0F, 1F);

            ElementProgressBar progressBar = new ElementProgressBar(this);//TODO test B&S Theme
            progressBar.constraints().bottom(button, Constraint.Property.Type.BOTTOM, 2).top(button, Constraint.Property.Type.TOP, 2).left(this, Constraint.Property.Type.LEFT, padding).right(button, Constraint.Property.Type.LEFT, padding);
            progressBar.setProgress(prog);
            elements.add(progressBar);

            ElementTextWrapper textPercent = new ElementTextWrapper(this);
            textPercent.setNoWrap().setSize(20, 100);
            textPercent.setText(new DecimalFormat("###.##").format(prog * 100F) + "%");
            textPercent.constraints().left(progressBar, Constraint.Property.Type.LEFT, 2);
            elements.add(textPercent);

            ElementTextWrapper textOutOf = new ElementTextWrapper(this);
            textOutOf.setNoWrap().setSize(20, 100);
            textOutOf.setText(I18n.format("hats.gui.window.allHats.hatsOutOf", found, list.elements.size()));
            textOutOf.constraints().right(progressBar, Constraint.Property.Type.RIGHT, 6);
            elements.add(textOutOf);
        }

        public void addHatRender(ElementHatsScrollView list, HatsSavedData.HatPart hatOrigin, HatsSavedData.HatPart hatPart)
        {
            hatPart.isShowing = true;
            if(!(hatPart.count == 0 && hatPart.hsbiser[0] == 0F && hatPart.hsbiser[1] == 0F && hatPart.hsbiser[2] == 1F)) //not undiscovered
            {
                hatPart.colouriser = new float[] { 0F, 0F, 0F, 0F };
                hatPart.hsbiser = new float[] { 0F, 0F, 0F };
            }
            HatsSavedData.HatPart copy = hatOrigin.createCopy();
            HatsSavedData.HatPart copyHatPart = copy.get(hatPart);
            ElementHatRender<?> hat = new ElementHatRender(list, copy, copyHatPart, btn -> {}, true) {
                @Override
                public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
                {
                    boolean isShowing = hatLevel.isShowing;
                    hatLevel.isShowing = true;
                    super.render(stack, mouseX, mouseY, partialTick);
                    hatLevel.isShowing = isShowing;
                }

                @Override
                public boolean isMouseOver(double mouseX, double mouseY)
                {
                    return false;
                }
            };
            hat.setSize(50, 70);
            list.addElement(hat);

            for(HatsSavedData.HatPart part : hatPart.hatParts)
            {
                HatsSavedData.HatPart copyPart = copy.get(part);
                if(copyPart != null)
                {
                    addHatRender(list, copy, copyPart);
                }
            }
            hatPart.isShowing = false;
        }
    }
}
