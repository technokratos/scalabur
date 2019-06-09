package org.repocrud.rest.data;

import lombok.Data;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:38<br/>
 */
@Data
public class ClientRequest {


    String clientId;
    String clientName;

    Position start;
    Position end;


}
