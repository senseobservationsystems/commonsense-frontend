package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class EnvironmentModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String FLOORS = "floors";
    public final static String OUTLINE = "gps_outline";
    public final static String POSITION = "position";
    public final static String DATE = "date";

    public EnvironmentModel() {
        super();
    }

    public EnvironmentModel(Map<String, Object> properties) {
        super(properties);
    }

    public EnvironmentModel(TreeModel parent) {
        super(parent);
    }

    public String getId() {
        return get(ID);
    }

    public String getName() {
        return get(NAME);
    }

    public String getFloors() {
        return get(FLOORS);
    }

    public String getOutline() {
        return get(OUTLINE);
    }

    public String getPosition() {
        return get(POSITION);
    }

    public long getDate() {
        return get(DATE);
    }
}
