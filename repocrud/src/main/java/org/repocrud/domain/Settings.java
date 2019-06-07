package org.repocrud.domain;

import org.repocrud.history.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * @author Denis B. Kulikov<br/>
 * date: 22.01.2019:22:10<br/>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Settings extends Auditable {

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

//    @Column(nullable = false)
//    @Enumerated(value = EnumType.STRING)
//    private Level level = Level.SYSTEM;

//    @Enumerated(value = EnumType.STRING)
//
//    private Type type;
//
//    public enum Type {
//        View,
//        Setup
//
//    }

    public enum Level {
        USER,
        COMPANY,
        SYSTEM
    }
}
