package org.repocrud.domain;

import org.repocrud.history.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author Denis B. Kulikov<br/>
 * date: 02.11.2018:11:52<br/>
 */
@Data
@Entity(name = "company")
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Company extends Auditable {
    @NotNull
    String title;

    @NotNull
    @Min(value = 1000000000L)
    @Max(value = 999999999999L)
    Long inn;

    String person;
    @Email
    String email;
    String phone;
    String address;

    @Override
    public String toString() {
        return title + "/" + inn;
    }
}
