package org.repocrud.service;

import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.*;
import org.repocrud.repository.CarModelRepository;
import org.repocrud.repository.CarRepository;
import org.repocrud.repository.CourierRepository;
import org.repocrud.repository.SmtpSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.Contended;

import javax.annotation.PostConstruct;

/**
 * @author Denis B. Kulikov<br/>
 * date: 09.06.2019:10:02<br/>
 */
@Slf4j
@Component
public class InitDataComponent {


    @Autowired
    SmtpSettingsRepository smtpSettingsRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CourierRepository courierRepository;
    @Autowired
    CarModelRepository carModelRepository;


    @PostConstruct
    private void init() {
        addSmpt();

        addCourier();


    }

    private void addCourier() {
        CarModel carModel= new CarModel("MAZ", TransportType.Truck, 1, 20000d, 12000d, 40d, 5, 20d, 40d);
        carModelRepository.saveAndFlush(carModel);
        Car car = new Car("textCar",carModel, null, new GeoPosition(37.99, 140.9, 0d));
        carRepository.saveAndFlush(car);
        Courier courier = new Courier("testCourier", "796439833672", car, null, 37.99, 140.9);
        courierRepository.saveAndFlush(courier);

        carRepository.saveAndFlush(car);
    }

    private void addSmpt() {
        SmtpSettings smtpSettings = new SmtpSettings();
        smtpSettings.setSmptPort(465);
        smtpSettings.setSmptServer("smtp.beget.ru");
        smtpSettings.setSmptUser("denis.kulikov@avtobar.ru");
        smtpSettings.setWarningMail("deniskulikov@sms.ru;denis.kulikov@avtobar.ru");
        smtpSettings.setSmptPassword("failed");
        smtpSettingsRepository.saveAndFlush(smtpSettings);
        log.info("Added smtp settings");
    }
}
