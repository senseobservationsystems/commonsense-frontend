package nl.sense_os.commonsense.client.utility;

import java.util.Comparator;

import nl.sense_os.commonsense.client.common.models.SensorModel;

public class SensorComparator<M extends Object> implements Comparator<Object> {

    private static final int DEVICES = 1;
    private static final int ENVIRONMENTS = 2;
    private static final int APPLICATIONS = 3;
    private static final int FEEDS = 4;
    private static final int STATES = 5;

    @Override
    public int compare(Object obj1, Object obj2) {
        if (!(obj1 instanceof SensorModel) || !(obj2 instanceof SensorModel)) {
            return 0;
        }

        SensorModel mdl1 = (SensorModel) obj1;
        SensorModel mdl2 = (SensorModel) obj2;
        int type1 = Integer.parseInt(mdl1.getType());
        switch (type1) {
        case 0:
            type1 = FEEDS;
            break;
        case 1:
            type1 = DEVICES;
            break;
        case 2:
            type1 = STATES;
            break;
        case 3:
            type1 = ENVIRONMENTS;
            break;
        case 4:
            type1 = APPLICATIONS;
            break;
        }
        int type2 = Integer.parseInt(mdl2.getType());
        switch (type2) {
        case 0:
            type2 = FEEDS;
            break;
        case 1:
            type2 = DEVICES;
            break;
        case 2:
            type2 = STATES;
            break;
        case 3:
            type2 = ENVIRONMENTS;
            break;
        case 4:
            type2 = APPLICATIONS;
            break;
        }

        String name1 = type1 + mdl1.getDisplayName();
        String name2 = type2 + mdl2.getDisplayName();

        return name1.compareToIgnoreCase(name2);
    }
}
