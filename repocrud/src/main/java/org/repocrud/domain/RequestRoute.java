package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.repocrud.history.Auditable;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

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
public class RequestRoute extends Auditable {

    @ManyToOne
    private Client client;

    private String startTitle;
    private String endTitle;

    @Embedded
    private GeoPosition start;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude",
                    column=@Column(name="end_latitude")),
        @AttributeOverride(name="longitude",
                column=@Column(name="end_longitude")),
        @AttributeOverride(name="altitude",
                column=@Column(name="end_altitude"))
    })
    private GeoPosition end;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude",
                    column=@Column(name="cur_latitude")),
            @AttributeOverride(name="longitude",
                    column=@Column(name="cur_longitude")),
            @AttributeOverride(name="altitude",
                    column=@Column(name="cur_altitude"))
    })
    private GeoPosition current;

    protected ZonedDateTime finishTime;

    @Enumerated(value = EnumType.STRING)
    private RequestStatus status = RequestStatus.NEW;
    @ManyToOne
    private Courier courier;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RequestRoute that = (RequestRoute) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return id + " " + client;
    }
}
