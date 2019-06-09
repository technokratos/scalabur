package org.repocrud.repository;

import org.repocrud.domain.Car;
import org.repocrud.domain.Client;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:12:41<br/>
 */
public interface ClientRepository extends CommonRepository<Client, Long>{
    Client findByClientId(String clientId);
}
