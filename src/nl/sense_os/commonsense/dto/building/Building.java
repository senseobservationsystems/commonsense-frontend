package nl.sense_os.commonsense.dto.building;

import java.io.Serializable;
import java.util.Date;

public class Building implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
//    private Key key;
    private Date createdAt;
    private int[] floorNumbers;
    private String[] floorNames;

}
