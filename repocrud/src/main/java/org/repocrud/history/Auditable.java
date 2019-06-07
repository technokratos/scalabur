package org.repocrud.history;

import org.repocrud.components.Identifiable;
import org.repocrud.domain.Company;
import org.repocrud.domain.User;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(HistoryListener.class)
public abstract class Auditable extends Identified  {

    @ManyToOne
    @CreatedBy
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    protected User createdBy;

    @JsonIgnore
    @CreatedDate
    protected ZonedDateTime  createdDate;

    @ManyToOne
    @LastModifiedBy
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    protected User lastModifiedBy;

    @JsonIgnore
    @LastModifiedDate
    protected ZonedDateTime lastModifiedDate;

    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    protected Company company;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identified that = (Identified) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

}