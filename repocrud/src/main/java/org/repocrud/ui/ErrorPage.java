package org.repocrud.ui;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;


@Slf4j
@Route("error")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class ErrorPage extends Div {


    @PostConstruct
    public void init() {
        log.info("Open error page");
        UI.getCurrent().addAfterNavigationListener(e -> getUI().ifPresent(ui -> ui.navigate(LoginForm.class)));
    }

}