package org.repocrud.history;

import org.repocrud.service.SymmetricEngineName;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Denis B. Kulikov<br/>
 * date: 19.03.2019:14:43<br/>
 */
@Slf4j
public class NodeIdGenerator implements IdentifierGenerator{//Configurable

    private static String node = null;

    public NodeIdGenerator() {

    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object o) throws HibernateException {
        Connection connection = session.connection();
        try {

            PreparedStatement ps = connection
                    .prepareStatement("select nextval('hibernate_sequence')");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                NodeId.of(id, SymmetricEngineName.getEngineName());

                return NodeId.of(id, SymmetricEngineName.getEngineName());
            }

        } catch (SQLException e) {
            log.error("Error in generated id", e);
        }
        return null;
    }

}
