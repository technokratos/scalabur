package org.repocrud.ui;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.crud.*;
import org.repocrud.domain.*;
import org.repocrud.repository.*;
import org.repocrud.ui.components.TabContainer;
import org.springframework.beans.factory.annotation.Autowired;

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


    @Autowired
    CarRepository carRepository;
    @Autowired
    CourierRepository courierRepository;
    @Autowired
    CarModelRepository carModelRepository;
    @Autowired
    ClientRepository clientRepository;

    @Autowired
    CourierCrudContainer courierCrudContainer;
    @Autowired
    RequestRouteRepository requestRouteRepository;


    @PostConstruct
    private void init() {
        TabContainer mainMenu = new TabContainer(Tabs.Orientation.VERTICAL);

        TabContainer cars = new TabContainer(Tabs.Orientation.HORIZONTAL);
        mainMenu.addTab(VaadinIcon.CAR, "cars", cars);


        cars.addTab(text("couriers"), courierCrudContainer);
        cars.addTab(text("clients"), new RepositoryCrud<>(Client.class, clientRepository));
        cars.addTab(text("requests"), new RepositoryCrud<>(RequestRoute.class, requestRouteRepository));

        cars.addTab(text("car"), new RepositoryCrud<Car, Long>(Car.class, carRepository));
        cars.addTab(text("carModel"), new RepositoryCrud<>(CarModel.class, carModelRepository));



        mainMenu.addTab(VaadinIcon.USERS, text("main.user"), userRepositoryCrud);
        TabContainer settingsTab = new TabContainer(Tabs.Orientation.HORIZONTAL);
        mainMenu.addTab(VaadinIcon.COG, "Settings", settingsTab);
        settingsTab.addTab(text("main.company"), companyCrud);
        settingsTab.addTab(text("main.glossary"), glossaryCrud);
        settingsTab.addTab(text("main.smpt" ), settingsCrudContainer);
        add(mainMenu);
    }
}

