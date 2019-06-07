package org.repocrud.repository;

import org.repocrud.config.SecurityUtils;
import org.repocrud.domain.Company;
import org.repocrud.domain.User;
import org.repocrud.repository.spec.RepoSpecificationFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * @author Denis B. Kulikov<br/>
 * date: 28.02.2019:10:20<br/>
 */
@NoRepositoryBean
public interface CompanyFilteredRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {


    default List<T> findAllByCompany() {
        User userDetails = (User ) SecurityUtils.getUserDetails();
        if (userDetails != null && userDetails.getCompany() != null) {
            return this.findAll(RepoSpecificationFactory.getCompanyRestriction(userDetails.getCompany()));
        } else {
            return this.findAll();
        }
    }

    default List<T> findAllByCompany(Sort sort) {
        User userDetails = (User ) SecurityUtils.getUserDetails();
        if (userDetails != null && userDetails.getCompany() != null) {
            return this.findAll(RepoSpecificationFactory.getCompanyRestriction(userDetails.getCompany()), sort);
        } else {
            return this.findAll(sort);
        }
    }

    default List<T> findAllByCompany(Company company, Sort sort) {
        return this.findAll(RepoSpecificationFactory.getCompanyRestriction(company), sort);

    }

    default Page<T> findAllByCompany(Pageable pageable) {
        User userDetails = (User ) SecurityUtils.getUserDetails();
        if (userDetails != null && userDetails.getCompany() != null) {
            return this.findAll(RepoSpecificationFactory.getCompanyRestriction(userDetails.getCompany()), pageable);
        } else {
            return this.findAll(pageable);
        }
    }



}
