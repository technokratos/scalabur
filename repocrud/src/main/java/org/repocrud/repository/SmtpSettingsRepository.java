package org.repocrud.repository;

import org.repocrud.domain.Company;
import org.repocrud.domain.SmtpSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmtpSettingsRepository extends JpaRepository<SmtpSettings, Long>, JpaSpecificationExecutor<SmtpSettings> {

    List<SmtpSettings> findByCompany(Company company);

    Long countByCompany(Company company);
}
