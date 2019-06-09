package org.repocrud.rest.data;

import lombok.Data;
import org.repocrud.domain.RequestStatus;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:38<br/>
 */
@Data
public class ClientStatusResponse {

    String clientId;
    String orderId;
    Position current;
    RequestStatus status;


}
