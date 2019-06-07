package org.repocrud.components.dialogs;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static java.lang.String.format;
import static org.repocrud.text.LocalText.text;

public class ConfirmDialog {

    private final Dialog dialog;
    private ConfirmAction action;
    private Label header;

    public ConfirmDialog(Class domainKey, String header, String success, String cancel, int widthPx, ConfirmAction action, Component... components) {
        this(text(domainKey, header), text(domainKey, success), text(domainKey, cancel), widthPx, action, components);
    }

    public ConfirmDialog(String header, String success, String cancel, int widthPx, ConfirmAction action, Component... components) {
        dialog = new Dialog();
        this.header = new Label(header);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(this.header);
        verticalLayout.add(components);
        dialog.add(verticalLayout);
        Button successButton = new Button(success);
        this.action = action;
        successButton.addClickListener(event -> {
            if (this.action.act()) {
                dialog.close();
            }
        });
        Button cancelButton = new Button(cancel, buttonClickEvent -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, successButton);
        verticalLayout.add(buttonLayout);
        dialog.setWidth(format("%dpx", widthPx));

        int padding = widthPx> 600 ?  2 * widthPx / 3: 50;
        buttonLayout.getElement().getStyle().set("padding-left", format("%dpx", padding));


    }

    public void open() {
        dialog.open();
    }

    public void close() {
        dialog.close();
    }

    public void setHeader(String text) {
        header.setText(text);
    }


    public void setConfirmAction(ConfirmAction action) {
        this.action = action;
    }



    public interface ConfirmAction {
        public boolean act();
    }
}
