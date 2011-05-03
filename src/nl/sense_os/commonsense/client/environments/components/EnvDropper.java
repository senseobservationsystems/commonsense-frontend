package nl.sense_os.commonsense.client.environments.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.Copier;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;

public class EnvDropper extends LayoutContainer {

    private static final String TAG = "EnvDropper";
    private MapWidget map;
    private LayoutContainer mapContainer;
    private TreePanel<TreeModel> sensors;
    private TreeStore<TreeModel> store;
    private HashMap<SensorModel, Marker> markers;

    public EnvDropper() {
        this.setLayout(new BorderLayout());

        initTree();
        initMap();
        setupDragDrop();
    }

    /**
     * Create a Google map and add in to center panel
     */
    private void initMap() {
        // Create the map.
        this.map = new MapWidget();

        // Add some controls
        this.map.setUIToDefault();

        // put the map in a container so we can drop stuff on it
        this.mapContainer = new LayoutContainer(new FitLayout());
        this.mapContainer.add(this.map);

        // Add the map to the dock.
        final BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
        this.add(this.mapContainer, center);
    }

    private void initTree() {
        this.store = new TreeStore<TreeModel>();

        List<TreeModel> mySensors = Registry.<List<TreeModel>> get(Constants.REG_MY_SENSORS_TREE);
        TreeModel mySensorsParent = new BaseTreeModel();
        mySensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        mySensorsParent.set("text", "My personal sensors");
        if (null != mySensors) {
            for (TreeModel sensor : mySensors) {
                TreeModel copy = Copier.copySensor(sensor);
                mySensorsParent.add(copy);
            }
            this.store.add(mySensorsParent, true);
        }

        List<TreeModel> groupSensors = Registry.<List<TreeModel>> get(Constants.REG_GROUP_SENSORS);
        TreeModel groupSensorsParent = new BaseTreeModel();
        groupSensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        groupSensorsParent.set("text", "My group sensors");
        if (null != groupSensors) {
            for (TreeModel sensor : groupSensors) {
                TreeModel copy = Copier.copySensor(sensor);
                groupSensorsParent.add(copy);
            }
            this.store.add(groupSensorsParent, true);
        }

        // SensorModel dummy = new SensorModel();
        // dummy.set(SensorModel.ID, "1");
        // dummy.set(SensorModel.NAME, "dummy");
        // dummy.set("text", "dummy");
        // this.store.add(dummy, false);
        //
        // SensorModel dummy2 = new SensorModel();
        // dummy2.set(SensorModel.ID, "2");
        // dummy2.set(SensorModel.NAME, "dommie");
        // dummy2.set("text", "dommie");
        // this.store.add(dummy2, false);

        this.sensors = new TreePanel<TreeModel>(this.store);
        this.sensors.setDisplayProperty("text");

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setScrollMode(Scroll.AUTO);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(sensors);

        final BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 275, 275, 2000);
        west.setSplit(true);
        this.add(panel, west);
    }

    private void setupDragDrop() {

        this.markers = new HashMap<SensorModel, Marker>();

        TreePanelDragSource source = new TreePanelDragSource(this.sensors);
        source.setTreeStoreState(true);
        source.setGroup("env-creator");

        final DropTarget dropTarget = new DropTarget(this.mapContainer);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {

            @Override
            public void dragDrop(DNDEvent e) {
                final ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                int x = e.getClientX() - map.getAbsoluteLeft();
                int y = e.getClientY() - map.getAbsoluteTop();
                onSensorsDropped(data, x, y);
            }
        });
        dropTarget.setGroup("env-creator");
    }

    protected void onSensorsDropped(ArrayList<TreeStoreModel> data, int x, int y) {
        Log.d(TAG, "onSensorsDropped");

        LatLng latLng = this.map.convertDivPixelToLatLng(Point.newInstance(x, y));
        Marker marker = new Marker(latLng);
        map.addOverlay(marker);
        markers.put(null, marker); // TODO
    }
}
