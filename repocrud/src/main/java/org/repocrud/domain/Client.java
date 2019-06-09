package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.repocrud.annotations.NestedEntity;
import org.repocrud.history.Auditable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * оптимизацией затрат на топливо
 * планированием наиболее быстрого маршрута
 * избежанием мошенничества (топливо, товары)
 * проложением маршрута с учетом характеристик грузового транспорта (нагрузка на ось, ширина, высота, вес);
 *
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:11:45<br/>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Client extends Auditable {
    @NotNull
    private String title;


    @Column(unique=true)
    private String clientId;

    @OneToMany
    @NestedEntity
    private List<RequestRoute> requestList;

    @Override
    public String toString() {
        return title + " " + clientId;
    }
}
