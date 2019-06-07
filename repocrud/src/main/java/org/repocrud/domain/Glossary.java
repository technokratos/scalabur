package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Denis B. Kulikov<br/>
 * date: 29.09.2018:0:27<br/>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@EntityListeners(GlossaryListener.class)
public class Glossary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Language language;

    @NotNull
    @Column(unique = true, nullable = false)
    private String key;

    @NotNull
    @Column(nullable = false, length = 10000)
    private String value;
}
