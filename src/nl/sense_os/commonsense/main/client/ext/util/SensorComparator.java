package nl.sense_os.commonsense.main.client.ext.util;

import java.util.Comparator;

import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

public class SensorComparator<M extends Object> implements Comparator<Object> {

    private static final int DEVICES = 1;
    private static final int ENVIRONMENTS = 2;
    private static final int APPLICATIONS = 3;
    private static final int FEEDS = 4;
    private static final int STATES = 5;

    @Override
    public int compare(Object obj1, Object obj2) {
        if (!(obj1 instanceof ExtSensor) || !(obj2 instanceof ExtSensor)) {
            return 0;
        }

        ExtSensor mdl1 = (ExtSensor) obj1;
        ExtSensor mdl2 = (ExtSensor) obj2;
        int type1 = -1;
        switch (mdl1.getType()) {
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
        int type2 = -1;
        switch (mdl2.getType()) {
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
