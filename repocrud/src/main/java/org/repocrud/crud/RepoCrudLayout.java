//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.repocrud.crud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.layout.CrudLayout;

import java.util.HashMap;
import java.util.Map;

import static org.repocrud.text.LocalText.text;

public class RepoCrudLayout extends Composite<VerticalLayout> implements CrudLayout, HasSize {
    protected VerticalLayout mainLayout = new VerticalLayout();
    protected VerticalLayout headerLayout = new VerticalLayout();
    protected HorizontalLayout toolbarLayout = new HorizontalLayout();
    protected HorizontalLayout filterLayout = new HorizontalLayout();
    protected VerticalLayout mainComponentLayout = new VerticalLayout();
    protected Dialog dialog;
    protected String formWindowWidth;
    protected Map<CrudOperation, String> windowCaptions = new HashMap();

    public RepoCrudLayout() {
        ((VerticalLayout)this.getContent()).setPadding(false);
        ((VerticalLayout)this.getContent()).setMargin(false);
        ((VerticalLayout)this.getContent()).add(new Component[]{this.mainLayout});
        this.mainLayout.setSizeFull();
        this.mainLayout.setMargin(false);
        this.mainLayout.setPadding(false);
        this.mainLayout.setSpacing(false);
        this.setSizeFull();
        this.headerLayout.setVisible(false);
        this.headerLayout.setSpacing(true);
        this.headerLayout.setMargin(true);
        this.toolbarLayout.setVisible(false);
        this.toolbarLayout.setPadding(true);
//        this.toolbarLayout.getElement().getStyle().set("padding-bottom", "20px");
        this.headerLayout.add(new Component[]{this.toolbarLayout});
        this.filterLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.filterLayout.setVisible(false);
        this.filterLayout.setSpacing(true);
        this.filterLayout.setPadding(true);
        this.filterLayout.getElement().getStyle().set("padding", "100px");
        this.headerLayout.add(new Component[]{this.filterLayout});
        this.mainComponentLayout.setSizeFull();
        this.mainComponentLayout.setMargin(false);
        this.mainComponentLayout.setPadding(false);
        this.mainLayout.add(new Component[]{this.mainComponentLayout});
        this.mainLayout.expand(new Component[]{this.mainComponentLayout});
        this.setWindowCaption(CrudOperation.ADD, text(RepoCrudLayout.class, "Add"));
        this.setWindowCaption(CrudOperation.UPDATE, text(RepoCrudLayout.class, "Update"));
        this.setWindowCaption(CrudOperation.DELETE, text(RepoCrudLayout.class, "confirmDelete"));
    }

    public void setMainComponent(Component component) {
        this.mainComponentLayout.removeAll();
        this.mainComponentLayout.add(new Component[]{component});
    }

    public void addFilterComponent(Component component) {
        if (!this.headerLayout.isVisible()) {
            this.headerLayout.setVisible(true);
            this.mainLayout.getElement().insertChild(this.mainLayout.getComponentCount() - 1, new Element[]{this.headerLayout.getElement()});
        }

        this.filterLayout.setVisible(true);
        this.filterLayout.add(new Component[]{component});
    }

    public void addToolbarComponent(Component component) {
        if (!this.headerLayout.isVisible()) {
            this.headerLayout.setVisible(true);
            this.mainLayout.getElement().insertChild(this.mainLayout.getComponentCount() - 1, new Element[]{this.headerLayout.getElement()});
        }

        this.toolbarLayout.setVisible(true);
        this.toolbarLayout.add(new Component[]{component});
    }

    public void showDialog(String caption, Component form) {
        VerticalLayout dialogLayout = new VerticalLayout(new Component[]{form});
        dialogLayout.setWidth("100%");
        dialogLayout.setMargin(false);
        dialogLayout.setPadding(false);
        this.dialog = new Dialog(new Component[]{new H3(caption), dialogLayout});
        this.dialog.setWidth(this.formWindowWidth);
        this.dialog.open();
    }

    public void showForm(CrudOperation operation, Component form, String s) {
        if (!operation.equals(CrudOperation.READ)) {
            this.showDialog((String)this.windowCaptions.get(operation), form);
        }

    }

    public void hideForm() {
        if (this.dialog != null) {
            this.dialog.close();
        }

    }

    public void setWindowCaption(CrudOperation operation, String caption) {
        this.windowCaptions.put(operation, caption);
    }

    public void setFormWindowWidth(String formWindowWidth) {
        this.formWindowWidth = formWindowWidth;
    }

    public void hideFilter() {
        this.filterLayout.setVisible(false);
        this.filterLayout.removeAll();
    }


}
