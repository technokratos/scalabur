package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.vaadin.crudui.crud.CrudOperation;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Denis B. Kulikov<br/>
 * date: 21.09.2018:22:27<br/>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class CrudHistory{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;


    private ZonedDateTime time;

    private String domain;

    @Enumerated(EnumType.STRING)
    private Operation operation;

    @Column(length = 1000)
    private String body;



    public enum Operation {
        SAVE,
        UPDATE,
        LOGIN, LOGOUT, DELETE, REMBER_LOGIN;
    }
}
