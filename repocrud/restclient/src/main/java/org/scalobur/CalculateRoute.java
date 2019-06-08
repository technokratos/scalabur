package org.scalobur;

import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteResponseType;

import javax.xml.bind.annotation.*;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:15:41<br/>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "CalculateRoute", namespace = "http://www.navteq.com/lbsp/Routing-CalculateRoute/4")
public class CalculateRoute {

    @XmlElement(name = "Response")
    CalculateRouteResponseType response;

    public CalculateRouteResponseType getResponse() {
        return response;
    }
}
