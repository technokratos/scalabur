package org.repocrud.repository;

import org.repocrud.domain.Courier;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:12:41<br/>
 */
public interface CourierRepository extends CommonRepository<Courier, Long>{
    Courier findByPhoneId(String phoneId);
}
