package org.repocrud.domain;

import org.repocrud.history.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Constraint;
import javax.validation.constraints.NotNull;

import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 07.11.2018:17:06<br/>
 */
@Data
@Entity(name = "smpt_settings")
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"company_id"})})
public class SmtpSettings extends Auditable {

    @NotNull
    String smptServer;
    @NotNull
    Integer smptPort;
    @NotNull
    String smptUser;
    @NotNull
    String smptPassword;
    @NotNull
    String warningMail;

    @Override
    public String toString() {
        return format("%s:%d/%s", smptServer, smptPort, smptUser);
    }
}
