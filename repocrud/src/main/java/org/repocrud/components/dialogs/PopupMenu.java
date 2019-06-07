package org.repocrud.components.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

/**
 * @author Denis B. Kulikov<br/>
 * date: 21.10.2018:1:24<br/>
 */
@Slf4j
public class PopupMenu extends Dialog {

    ListBox<ContextItem> actionListBox;
    private VerticalLayout itemsLayout;

    public PopupMenu(ContextItem... items) {

        Stream.of(items)
                .map(contextItem -> new Button(contextItem.text, event -> {
                    try {
                        contextItem.action.act();
                    } catch (Exception e) {
                        log.error("Error in context menu item " + contextItem.text, e);
                        Notification.show(e.getLocalizedMessage());
                    }
                    PopupMenu.this.close();
                }))
                .forEach(button -> itemsLayout.add(button));



    }

    public PopupMenu add(ContextItem contextItem) {
        new Button(contextItem.text, event -> {
            try {
                contextItem.action.act();
            } catch (Exception e) {
                log.error("Error in context menu item " + contextItem.text, e);
                Notification.show(e.getLocalizedMessage());
            }
            PopupMenu.this.close();
        });
        return this;
    }

    @Override
    public void open() {
        super.open();
        Page page = UI.getCurrent().getPage();
        page.executeJavaScript("$('[id*=\"overlay\"]').removeAttribute('with-backdrop')");

    }

    @AllArgsConstructor
    public static class ContextItem {
        String text;
        Action action;

        @Override
        public String toString() {
            return text;
        }
    }


    public interface Action {
        void act();
    }
}
