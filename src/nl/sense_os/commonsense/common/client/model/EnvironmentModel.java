package nl.sense_os.commonsense.common.client.model;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

/**
 * Model for an environment. GXT-style bean, used in various GXT components.
 */
public class EnvironmentModel extends BaseTreeModel {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentModel.class.getName());
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

    public EnvironmentModel(EnvironmentJso environment) {
        this();
        setId(environment.getId());
        setName(environment.getName());
        setFloors(environment.getFloors());
        setOutline(environment.getOutline());
        setPosition(environment.getPosition());
        setDate(environment.getDate());
    }

    public EnvironmentModel(Map<String, Object> properties) {
        super(properties);
    }

    public EnvironmentModel(TreeModel parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnvironmentModel) {
            return getId() == ((EnvironmentModel) obj).getId();
        } else {
            return super.equals(obj);
        }
    }

    public Date getDate() {
        return new Date(0); // get(DATE, new Date(0));
    }

    public int getFloors() {
        Object property = get(FLOORS);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + FLOORS);
            return -1;
        }
    }

    public int getId() {
        Object property = get(ID);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + ID);
            return -1;
        }
    }

    public String getName() {
        return get(NAME);
    }

    public Polygon getOutline() {
        return get(OUTLINE, null);
    }

    public LatLng getPosition() {
        return get(POSITION, null);
    }

    public EnvironmentModel setDate(Date date) {
        set(DATE, date);
        return this;
    }

    public EnvironmentModel setFloors(int floors) {
        set(FLOORS, floors);
        return this;
    }

    public EnvironmentModel setId(int id) {
        set(ID, id);
        return this;
    }

    public EnvironmentModel setName(String name) {
        set(NAME, name);
        return this;
    }

    public EnvironmentModel setOutline(Polygon outline) {
        set(OUTLINE, outline);
        return this;
    }

    public EnvironmentModel setPosition(LatLng position) {
        set(POSITION, position);
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }
}
