package org.repocrud.text;

import org.repocrud.config.SecurityUtils;
import org.repocrud.components.HtmlText;
import org.repocrud.components.dialogs.ConfirmDialog;
import org.repocrud.domain.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.pekka.WysiwygE;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 05.03.2019:13:01<br/>
 */
public class HelpTools {

    public static Button addHelpButton(Class domain, String key) {
        String helpKey = "help." + key;
        Button button = new Button(VaadinIcon.QUESTION.create());
        button.addClickListener(buttonClickEvent -> {
            String text = text(domain, helpKey);


            HtmlText htmlText = new HtmlText(text);
            Button editButton = new Button(text("help.edit"));
            Button closeButton = new Button(text("help.close"));
            final Dialog dialog;
            User user = (User) SecurityUtils.getUserDetails();
            if (user.getCompany() == null) {
                dialog = new Dialog(htmlText, new HorizontalLayout(closeButton, editButton));
                editButton.addClickListener(e -> {
                    WysiwygE editor = new WysiwygE();
                    editor.setValue(text);
                    ConfirmDialog editDialog = new ConfirmDialog(text("help.header"),
                            text("help.success"),
                            text("help.cancel"),
                            800,
                            () -> {
                                boolean save = LocalText.save(domain, helpKey, editor.getValue());

                                dialog.close();
                                return save;
                            },
                            editor
                    );
                    editDialog.open();
                });
            } else {
                dialog = new Dialog(htmlText, closeButton);
            }

            closeButton.addClickListener(e -> {
                dialog.close();
            });
            dialog.open();

        });
        return button;
    }
}
