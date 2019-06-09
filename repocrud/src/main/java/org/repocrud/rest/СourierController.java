package org.repocrud.rest;

import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.*;
import org.repocrud.repository.*;
import org.repocrud.rest.data.ClientStatusResponse;
import org.repocrud.rest.data.Position;
import org.repocrud.rest.data.Route;
import org.repocrud.service.RouteService;
import org.repocrud.service.SmtpFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:32<br/>
 */
@Slf4j
@RestController
public class СourierController {


    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RequestRouteRepository requestRouteRepository;
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CourierRepository courierRepository;
    @Autowired
    private RouteService routeService;


    @Autowired
    private SmtpSettingsRepository repository;

    @Autowired
    private SmtpFactoryService smtpFactoryService;

    //"/updateposition/{courierId}/{lat}/{lon}",
    //"/route/{courierId}", //
    // "/courierAuth/{courierId}"
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/route/{courierId}", //
            method = RequestMethod.GET, //
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Route getRoute(@PathVariable String courierId) {
        return routeService.getRouteByCourierId(courierId);
    }



    @Transactional
    @ResponseBody
    @RequestMapping(value = "/updateposition/{courierId}/{lat}/{lon}", //
            method = RequestMethod.GET, //
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String updatePosition(@PathVariable String courierId,
                                               @PathVariable String lat,
                                               @PathVariable String lon) {

        try {
            Optional<Courier> optCourier = courierRepository.findById(Long.parseLong(courierId));

            if (optCourier.isPresent()) {
                GeoPosition geoPosition = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lon), null);
                Courier courier = optCourier.get();
                courier.setLatitude(geoPosition.getLatitude());
                courier.setLongitude(geoPosition.getLongitude());
                courierRepository.saveAndFlush(courier);
                return "success";
            } else {
                return "notfound";
            }
        } catch (Exception e) {
            log.error("Failed update location", e);
            return "failed";
        }



    }



    @ResponseBody
    @RequestMapping(value = "/courierAuth/{phoneId}", //
            method = RequestMethod.GET, //
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCourierAuth(@PathVariable String phoneId) {

        try{

            Courier courier = courierRepository.findByPhoneId(phoneId);
            if (courier != null) {
                SmtpSettings smptSettings = smtpFactoryService.getSmptSettings();
                boolean result = smtpFactoryService.sendAuthMail(phoneId, smptSettings, courier.getId().toString());

                return (result)?"sent":"failed";
            } else {
                return "notfound";
            }

        }catch (Exception e) {
            log.error("Failed courierAuth", e);
            return "failed";
        }

    }
}
