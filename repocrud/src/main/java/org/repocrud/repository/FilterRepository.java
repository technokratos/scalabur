package org.repocrud.repository;

import org.repocrud.domain.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Long>, JpaSpecificationExecutor<Filter> {


    List<Filter> findByEntityOrderByPositionAsc(String entity);
}
