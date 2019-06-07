package org.repocrud.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis B. Kulikov<br/>
 * date: 19.03.2019:13:24<br/>
 */
@Data
@Slf4j
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class NodeId implements Serializable {

    public static Pattern NodeIdPattern = Pattern.compile("(\\d*):?(.*)");

    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "node_id")
    String nodeId;
    public static NodeId of(Long value, String nodeId) {
        return new NodeId(value, nodeId);
    };

    @Override
    public String toString() {
        return Long.toString(id) + (nodeId!= null ? nodeId : "");
    }

    public static NodeId parse(String nodeId) {
        Matcher matcher = NodeIdPattern.matcher(nodeId);
        if (matcher.find()) {
            Long id = Long.parseLong(matcher.group(1));
            String parsedNodeId = matcher.group(2);
            return NodeId.of(id, parsedNodeId.isEmpty()? null: parsedNodeId);
        } else {
            log.error("Impossible parse nodeId {}", nodeId);
            return null;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return Objects.equals(id, nodeId.id) &&
                Objects.equals(this.nodeId, nodeId.nodeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, nodeId);
    }

}
