package org.repocrud.repository;

import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface CrudHistoryRepository extends JpaRepository<CrudHistory, Long>, JpaSpecificationExecutor<CrudHistory> {

    List<CrudHistory> findByUser(User user);

    Page<CrudHistory> findByUserOrderByTimeDesc(User user, Pageable pageable);

    Page<CrudHistory> findByUserAndDomainOrderByTimeDesc(User user, String domain, Pageable pageable);

    Long countByUser(User user);
    Long countByUserAndDomain(User user, String domain);


    @Query("select distinct domain from CrudHistory")
    List<String> allDomains();

    List<CrudHistory> findByTimeAfter(ZonedDateTime time);
}
