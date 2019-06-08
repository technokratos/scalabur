package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.repocrud.history.Auditable;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

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
public class Car extends Auditable {
    @NotNull
    private String title;

    @ManyToOne
    private Driver driver;

    @ManyToOne
    private CarModel carModel;

    private String carId;


}
