package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import java.util.Comparator;

public class SensorComparator implements Comparator<Object> {

    private static final int DEVICES = 1;
    private static final int ENVIRONMENTS = 2;
    private static final int APPLICATIONS = 3;
    private static final int FEEDS = 4;
    private static final int STATES = 5;
    
    @Override
    public int compare(Object obj1, Object obj2) {
        try {
            TreeModel o1 = (TreeModel) obj1;
            TreeModel o2 = (TreeModel) obj2;
            int type1 = o1.<Integer> get("tagType");
            int type2 = o2.<Integer> get("tagType");
            if (type1 == type2) {
                if (type1 == TagModel.TYPE_SENSOR) {
                    String name1 = o1.<String> get("name");
                    String name2 = o2.<String> get("name");
                    return name1.compareToIgnoreCase(name2);
                } else if (type1 == TagModel.TYPE_GROUP) {
                    String n1 = o1.<String> get("name"); 
                    int t1 = 0;
                    if (n1.equalsIgnoreCase("Devices")) {
                        t1 = DEVICES;
                    } else if (n1.equalsIgnoreCase("Environments")) {
                        t1 = ENVIRONMENTS;
                    } else if (n1.equalsIgnoreCase("Applications")) {
                        t1 = APPLICATIONS;
                    } else if (n1.equalsIgnoreCase("Feeds")) {
                        t1 = FEEDS;
                    } else if (n1.equalsIgnoreCase("States")) {
                        t1 = STATES;
                    } 
                    String n2 = o2.<String> get("name"); 
                    int t2 = 0;
                    if (n2.equalsIgnoreCase("Devices")) {
                        t2 = DEVICES;
                    } else if (n2.equalsIgnoreCase("Environments")) {
                        t2 = ENVIRONMENTS;
                    }  else if (n2.equalsIgnoreCase("Feeds")) {
                        t2 = FEEDS;
                    } else if (n2.equalsIgnoreCase("Applications")) {
                        t2 = APPLICATIONS;
                    } else if (n2.equalsIgnoreCase("States")) {
                        t2 = STATES;
                    }
                    return t1-t2;
                } else if (type1 == TagModel.TYPE_DEVICE) {
                    String name1 = o1.<String> get("type");
                    String name2 = o2.<String> get("type");
                    return name1.compareToIgnoreCase(name2);
                }
            }
            return 0;
        } catch (ClassCastException e) {
            return 0;
        }
    }
}
