package org.repocrud.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Denis B. Kulikov<br/>
 * date: 09.06.2019:8:57<br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Route {
    List<Position> positions;
}
