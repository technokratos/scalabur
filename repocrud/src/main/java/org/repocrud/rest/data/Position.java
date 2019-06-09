package org.repocrud.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.repocrud.domain.GeoPosition;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:20:21<br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private Double latitude;
    Double longitude;
    Double altitude;
    String title;
    String id;

    public Position(GeoPosition pos) {
        this(pos.getLatitude(), pos.getLongitude(), pos.getAltitude() , null, null);
    }

    public Position(GeoPosition pos, String title, String id) {
        this(pos.getLatitude(), pos.getLongitude(), pos.getAltitude(), title, id);
    }
}
