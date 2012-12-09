package nl.sense_os.commonsense.main.client.gxt.model;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.model.apiclass.Environment;
import nl.sense_os.commonsense.main.client.shared.util.MapTools;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

/**
 * Model for an environment. GXT-style bean, used in various GXT components.
 */
public class GxtEnvironment extends BaseTreeModel {

	private static final Logger LOGGER = Logger.getLogger(GxtEnvironment.class.getName());
	private static final long serialVersionUID = 1L;
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String FLOORS = "floors";
	public final static String OUTLINE = "gps_outline";
	public final static String POSITION = "position";
	public final static String DATE = "date";



	public GxtEnvironment() {
		super();
	}

	public GxtEnvironment(Environment environment) {
		this();
		setId(environment.getId());
		setName(environment.getName());
		setFloors(environment.getFloors());
        setOutline(MapTools.outlineToPolygon(environment.getRawOutline()));
        setPosition(MapTools.positionToLatLng(environment.getPosition()));
        setDate(environment.getDate());
	}

	public GxtEnvironment(Map<String, Object> properties) {
		super(properties);
	}

	public GxtEnvironment(TreeModel parent) {
		super(parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GxtEnvironment) {
			return getId() == ((GxtEnvironment) obj).getId();
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

    public String getId() {
        return get(ID);
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

	public GxtEnvironment setDate(Date date) {
		set(DATE, date);
		return this;
	}

	public GxtEnvironment setFloors(int floors) {
		set(FLOORS, floors);
		return this;
	}

    public GxtEnvironment setId(String id) {
		set(ID, id);
		return this;
	}

	public GxtEnvironment setName(String name) {
		set(NAME, name);
		return this;
	}

	public GxtEnvironment setOutline(Polygon outline) {
		set(OUTLINE, outline);
		return this;
	}

	public GxtEnvironment setPosition(LatLng position) {
		set(POSITION, position);
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}
}
