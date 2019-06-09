package org.repocrud.service;

import org.repocrud.rest.data.Position;
import org.repocrud.rest.data.Route;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Denis B. Kulikov<br/>
 * date: 09.06.2019:0:18<br/>
 */
@Service
public class RouteService {
    public Route getRouteByCourierId(String courierId) {
        Route route = new Route();
        Position pos1 =  new Position(37.7914050,-122.3987030, 200d, "testA", "123");

        Position pos2 = new Position(37.3456,56.778, 100d, "testB", "124");
        List<Position> positons = Arrays.asList(pos1, pos2);
        route.setPositions(positons);
        return route;
    }
}
