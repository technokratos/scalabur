package org.repocrud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:11<br/>
 */
@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class GeoPosition {


    private Double latitude;
    private Double longitude;
    private Double altitude;

}
