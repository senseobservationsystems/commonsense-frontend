package nl.sense_os.commonsense.client.environments;

import java.util.ArrayList;
import java.util.HashMap;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;

public class EnvCreator extends View {

    private static final String TAG = "EnvCreator";
    private Window window;
    private MapWidget map;
    private LayoutContainer mapContainer;
    private TreePanel<TreeModel> sensors;
    private TreeStore<TreeModel> store;
    private HashMap<SensorModel, Marker> markers;

    public EnvCreator(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvEvents.ShowCreator)) {
            showPanel();
        }
    }

    private void hidePanel() {
        this.window.hide();
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new BorderLayout());
        this.window.setMinWidth(720);
        this.window.setMinHeight(444);

        initTree();
        initMap();
        setupDragDrop();
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
        this.window.add(this.mapContainer, center);
    }

    private void initTree() {
        this.store = new TreeStore<TreeModel>();

        SensorModel dummy = new SensorModel();
        dummy.set(SensorModel.ID, "1");
        dummy.set(SensorModel.NAME, "dummy");
        dummy.set("text", "dummy");
        this.store.add(dummy, false);

        SensorModel dummy2 = new SensorModel();
        dummy2.set(SensorModel.ID, "2");
        dummy2.set(SensorModel.NAME, "dommie");
        dummy2.set("text", "dommie");
        this.store.add(dummy2, false);

        this.sensors = new TreePanel<TreeModel>(this.store);
        this.sensors.setDisplayProperty("text");

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setScrollMode(Scroll.AUTO);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(sensors);

        final BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 275, 275, 2000);
        west.setSplit(true);
        this.window.add(panel, west);
    }

    private void showPanel() {
        this.window.show();
        this.window.center();
    }
}
