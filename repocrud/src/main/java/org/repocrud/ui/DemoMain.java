package org.repocrud.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.crud.CompanyCrudContainer;
import org.repocrud.crud.GlossaryCrudContainer;
import org.repocrud.crud.SettingsCrudContainer;
import org.repocrud.crud.UserRepositoryCrud;
import org.repocrud.ui.components.TabContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

import static org.repocrud.text.LocalText.text;


/**
 * @author Denis B. Kulikov<br/>
 * date: 18.03.2019:12:02<br/>
 */
@Slf4j
//@Profile("demo")
@Route(LoginForm.MAIN)
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class DemoMain extends HorizontalLayout {


    @Autowired
    UserRepositoryCrud userRepositoryCrud;
    @Autowired
    private SettingsCrudContainer settingsCrud;
    @Autowired
    private CompanyCrudContainer companyCrud;
    @Autowired
    private GlossaryCrudContainer glossaryCrud;
    @Autowired
    private SettingsCrudContainer settingsCrudContainer;

    @PostConstruct
    private void init() {
        TabContainer mainMenu = new TabContainer(Tabs.Orientation.VERTICAL);

        mainMenu.addTab(text("main.user"), userRepositoryCrud);
        mainMenu.addTab(text("main.company"), companyCrud);
        mainMenu.addTab(text("main.glossary"), glossaryCrud);
        mainMenu.addTab(text("main.smpt" ), settingsCrudContainer);
        add(mainMenu);
    }
}

