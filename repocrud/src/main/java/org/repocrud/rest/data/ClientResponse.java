package org.repocrud.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:38<br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponse {

    String clientId;
    String orderId;


}
