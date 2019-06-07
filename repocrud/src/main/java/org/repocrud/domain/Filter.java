package org.repocrud.domain;

import org.repocrud.history.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 18.11.2018:22:18<br/>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Filter extends Auditable {

    private String entity;
    private String field;
    @Column(name = "filter_value")
    private String value;
    private Integer position;

    @Enumerated(EnumType.STRING)
    private Operation operation;

    public enum Operation{
        LIKE("~", String.class),
        EQUAL ("=", Number.class, Temporal.class, String.class),
        BEFORE ("<", Number.class, Temporal.class, String.class),
        AFTER(">", Number.class, Temporal.class, String.class);
        private final String symbol;
        private final Class[] types;

        Operation(String symbol, Class... types) {
            this.symbol = symbol;
            this.types = types;
        }

        public List<Operation> byType(Class type) {
            return
            Stream.of(Operation.values())
                    .filter(operation1 -> Stream.of(operation1.types)
                    .anyMatch(type::isAssignableFrom)).collect(Collectors.toList());
        }


        @Override
        public String toString() {
            return symbol;
        }
    }

    @Override
    public String toString() {
        return format("%s.%s %s %s", entity, field, operation, value);
    }
}
