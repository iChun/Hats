package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.sort.HatSorter;
import me.ichun.mods.hats.common.hats.sort.SortHandler;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class WindowHatSorter extends Window<WorkspaceHats>
{
    //title will be localised, text won't.
    public WindowHatSorter(WorkspaceHats parent)
    {
        super(parent);

        disableDockingEntirely();
        disableDrag();
        disableTitle();

        setView(new ViewHatSorter(this));
    }

    public static class ViewHatSorter extends View<WindowHatSorter>
    {
        public ElementList<?> filters;
        public ElementList<?> sorters;

        public ViewHatSorter(@Nonnull WindowHatSorter parent)
        {
            super(parent, "hats.gui.window.sorter.title");

            int padding = 8;

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(button);

            ElementTextWrapper textFilters = new ElementTextWrapper(this);
            textFilters.setText(I18n.format("hats.gui.window.sorter.filters"));
            textFilters.constraints().left(this, Constraint.Property.Type.LEFT, padding).top(this, Constraint.Property.Type.TOP, 4).width(this, Constraint.Property.Type.WIDTH, 45);
            elements.add(textFilters);

            filters = new ElementList<>(this);
            filters.constraints().top(textFilters, Constraint.Property.Type.BOTTOM, 2).left(this, Constraint.Property.Type.LEFT, padding).bottom(button, Constraint.Property.Type.TOP, padding).width(this, Constraint.Property.Type.WIDTH, 45); //45%
            elements.add(filters);

            sorters = new ElementList<>(this).setDragHandler((i, j) -> {}).setRearrangeHandler((i, j) -> {});
            sorters.constraints().left(filters, Constraint.Property.Type.RIGHT, padding).top(filters, Constraint.Property.Type.TOP, 0).bottom(filters, Constraint.Property.Type.BOTTOM, 0). right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(sorters);

            for(HatSorter filterSorter : Hats.configClient.filterSorters)
            {
                addHatSorterToList(filterSorter.isFilter() ? filters : sorters, filterSorter, filterSorter.isInverse ? 2 : 1);
            }

            SortHandler.SORTERS.forEach((type, clz) -> {
                try
                {
                    HatSorter hatSorter = clz.newInstance();
                    ElementList<?> elementList = hatSorter.isFilter() ? filters : sorters;

                    boolean add = true;
                    for(ElementList.Item<?> item : elementList.items)
                    {
                        if(((HatSorter)item.getObject()).type().equals(hatSorter.type()))
                        {
                            add = false;
                            break;
                        }
                    }

                    if(add)
                    {
                        addHatSorterToList(hatSorter.isFilter() ? filters : sorters, hatSorter, 0);
                    }
                }
                catch(InstantiationException | IllegalAccessException e)
                {
                    Hats.LOGGER.error("Error creating known sorter type: {}", clz.getName());
                    e.printStackTrace();
                }
            });

            ElementTextWrapper textSorters = new ElementTextWrapper(this);
            textSorters.setText(I18n.format("hats.gui.window.sorter.sorters"));
            textSorters.constraints().left(sorters, Constraint.Property.Type.LEFT, 0).bottom(sorters, Constraint.Property.Type.TOP, 3).right(sorters, Constraint.Property.Type.RIGHT, 0);
            elements.add(textSorters);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                ArrayList<HatSorter> newSorters = new ArrayList<>();

                for(ElementList.Item<?> item : filters.items)
                {
                    if(!((ElementToggleTextured<?>)item.getById("btnDisabled")).toggleState) //is not disabled
                    {
                        newSorters.add((HatSorter)item.getObject());
                    }
                }

                for(ElementList.Item<?> item : sorters.items)
                {
                    if(!((ElementToggleTextured<?>)item.getById("btnDisabled")).toggleState) //is not disabled
                    {
                        newSorters.add((HatSorter)item.getObject());
                    }
                }

                ArrayList<String> filterSorterText = new ArrayList<>();
                for(HatSorter newSorter : newSorters)
                {
                    String s = newSorter.type();

                    if(newSorter.isInverse)
                    {
                        s += ":inverse";
                    }

                    filterSorterText.add(s);
                }

                Hats.configClient.filterSorterConfig = filterSorterText;
                Hats.configClient.save(); //This calls onConfigLoaded. No need to reset the config.

                parent.parent.windowHatsList.getCurrentView().updateSearch("");

                parent.parent.removeWindow(parent);
            });
            button1.setSize(60, 20);
            button1.constraints().right(button, Constraint.Property.Type.LEFT, padding);
            elements.add(button1);

            ElementButton<?> buttonTip = new ElementButton<>(this, "?", btn -> {});
            buttonTip.setSize(20, 20);
            buttonTip.setTooltip(I18n.format("hats.gui.window.sorter.dragTip"));
            buttonTip.constraints().left(this, Constraint.Property.Type.LEFT, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(buttonTip);
        }

        public void addHatSorterToList(ElementList<?> list, HatSorter filterSorter, int state)//state: 0 disabled, 1 enabled, 2 inverse
        {
            ElementList.Item<HatSorter> item = list.addItem(filterSorter);

            ElementToggleTextured<?> btnStack;
            ElementToggleTextured<?> btnStackLast;

            btnStack = new ElementToggleTextured<>(item, I18n.format("hats.gui.window.sorter.disabled"), WindowSidebar.ViewSidebar.TEX_CANCEL, btn -> {
                for(Element<?> element : item.elements)
                {
                    if(element instanceof ElementToggleTextured && element != btn)
                    {
                        ((ElementToggleTextured<?>)element).toggleState = false;
                    }
                }

                btn.toggleState = true;

                item.getObject().isInverse = false;
            });
            btnStack.setSize(14, 14).setId("btnDisabled");
            btnStack.setToggled(state == 0);
            btnStack.constraints().right(item, Constraint.Property.Type.RIGHT, item.getBorderSize());
            item.elements.add(btnStack);
            btnStackLast = btnStack;

            btnStack = new ElementToggleTextured<>(item, I18n.format("hats.gui.window.sorter.enabledInverse"), WindowSidebar.ViewSidebar.TEX_RELOAD, btn -> {
                for(Element<?> element : item.elements)
                {
                    if(element instanceof ElementToggleTextured && element != btn)
                    {
                        ((ElementToggleTextured<?>)element).toggleState = false;
                    }
                }

                btn.toggleState = true;

                item.getObject().isInverse = true;
            });
            btnStack.setSize(14, 14).setId("btnInverse");
            btnStack.setToggled(state == 2);
            btnStack.constraints().right(btnStackLast, Constraint.Property.Type.LEFT, item.getBorderSize());
            item.elements.add(btnStack);
            btnStackLast = btnStack;

            btnStack = new ElementToggleTextured<>(item, I18n.format("hats.gui.window.sorter.enabled"), WindowSidebar.ViewSidebar.TEX_CONFIRM, btn -> {
                for(Element<?> element : item.elements)
                {
                    if(element instanceof ElementToggleTextured && element != btn)
                    {
                        ((ElementToggleTextured<?>)element).toggleState = false;
                    }
                }

                btn.toggleState = true;

                item.getObject().isInverse = false;
            });
            btnStack.setSize(14, 14).setId("btnEnabled");
            btnStack.setToggled(state == 1);
            btnStack.constraints().right(btnStackLast, Constraint.Property.Type.LEFT, item.getBorderSize());
            item.elements.add(btnStack);
            btnStackLast = btnStack;

            ElementTextWrapper wrapper = new ElementTextWrapper(item).setText(I18n.format("hats.gui.sorter." + filterSorter.type() + ".name"));
            wrapper.setConstraint(Constraint.matchParent(wrapper, item, item.getBorderSize()).right(btnStackLast, Constraint.Property.Type.LEFT, item.getBorderSize()).bottom(null, Constraint.Property.Type.BOTTOM, 0));
            item.elements.add(wrapper);
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            stack.push();
            stack.translate(0F, 0F, 375F); //silly ElementHatRender

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();
        }
    }
}
