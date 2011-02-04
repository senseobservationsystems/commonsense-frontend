package nl.sense_os.commonsense.client.utility;

import java.util.Comparator;

import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.TreeModel;

public class SensorComparator implements Comparator<Object> {

    private static final int DEVICES = 1;
    private static final int ENVIRONMENTS = 2;
    private static final int APPLICATIONS = 3;
    private static final int FEEDS = 4;
    private static final int STATES = 5;

    @Override
    public int compare(Object obj1, Object obj2) {
        try {
            TreeModel model1 = (TreeModel) obj1;
            TreeModel model2 = (TreeModel) obj2;
            int tagType1 = model1.<Integer> get("tagType");
            int tagType2 = model2.<Integer> get("tagType");

            if (tagType1 == tagType2) {
                if (tagType1 == TagModel.TYPE_SENSOR) {
                    return compareSensors(model1, model2);
                } else if (tagType1 == TagModel.TYPE_GROUP) {
                    return compareGroups(model1, model2);
                } else if (tagType1 == TagModel.TYPE_DEVICE) {
                    return compareDevices(model1, model2);
                }
            }
            return tagType2 - tagType1;
        } catch (ClassCastException e) {
            return 0;
        }
    }

    private int compareSensors(TreeModel model1, TreeModel model2) {
        String name1 = model1.<String> get("text");
        String name2 = model2.<String> get("text");
        return name1.compareToIgnoreCase(name2);
    }

    private int compareDevices(TreeModel model1, TreeModel model2) {
        String name1 = model1.<String> get("text");
        String name2 = model2.<String> get("text");
        return name1.compareToIgnoreCase(name2);
    }

    private int compareGroups(TreeModel model1, TreeModel model2) {

        String name1 = model1.<String> get("text");
        int t1 = -1;
        if (name1.equalsIgnoreCase("Devices")) {
            t1 = DEVICES;
        } else if (name1.equalsIgnoreCase("Environments")) {
            t1 = ENVIRONMENTS;
        } else if (name1.equalsIgnoreCase("Applications")) {
            t1 = APPLICATIONS;
        } else if (name1.equalsIgnoreCase("Feeds")) {
            t1 = FEEDS;
        } else if (name1.equalsIgnoreCase("States")) {
            t1 = STATES;
        }
        String name2 = model2.<String> get("text");
        int t2 = -1;
        if (name2.equalsIgnoreCase("Devices")) {
            t2 = DEVICES;
        } else if (name2.equalsIgnoreCase("Environments")) {
            t2 = ENVIRONMENTS;
        } else if (name2.equalsIgnoreCase("Feeds")) {
            t2 = FEEDS;
        } else if (name2.equalsIgnoreCase("Applications")) {
            t2 = APPLICATIONS;
        } else if (name2.equalsIgnoreCase("States")) {
            t2 = STATES;
        }
        if (t1 != -1) {
            if (t2 != -1) {
                return t1 - t2;
            } else {
                return -1;
            }
        } else {
            if (t2 != -1) {
                return 1;
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        }

    }
}
