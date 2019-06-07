package org.repocrud.components.dialogs;


import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static org.repocrud.text.LocalText.text;

public class InformationDialog extends Dialog {


    public InformationDialog(Component... components) {
        this(new VerticalLayout(components));
    }

    private InformationDialog(VerticalLayout verticalLayout) {
        super(verticalLayout);
        Button close = new Button(text("close"));
        //$('[id*="overlay"]').removeAttribute('with-backdrop')
        close.addClickListener(event -> InformationDialog.this.close());
        verticalLayout.add(close);
    }


}
