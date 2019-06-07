package org.repocrud.history;

import org.repocrud.components.Identifiable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Denis B. Kulikov<br/>
 * date: 29.09.2018:1:39<br/>
 */
@Getter
@Setter
@MappedSuperclass
public class NodeIdentified implements Identifiable<NodeId> {

    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nodeIdGenerator")
    @GenericGenerator(name = "nodeIdGenerator",
            strategy = "org.repocrud.history.NodeIdGenerator"
    )
    protected NodeId id;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeIdentified that = (NodeIdentified) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
