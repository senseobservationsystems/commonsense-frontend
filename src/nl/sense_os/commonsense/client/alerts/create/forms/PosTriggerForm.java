package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.PositionTrigger;
import nl.sense_os.commonsense.client.alerts.create.utils.IndexPolygon;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.event.PolygonClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.PolyStyleOptions;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class PosTriggerForm extends FormPanel {

    /**
     * Marker for which you can set or get an index
     */
    class IndexMarker extends Marker {
        private int index;

        public IndexMarker(LatLng point, MarkerOptions options) {
            super(point, options);
        }

        public int getIndex() {
            index = -1;

            for (int i = 0; i < markerList.size(); i++) {
                if (markerList.get(i).equals(this)) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

    }
    /**
     * RadioGroup where you can set the spacing between radio buttons
     */
    class MyRadioGroup extends RadioGroup {
        public MyRadioGroup() {
            super();
            setSpacing(20);
        }
    }
    private static final Logger LOG = Logger.getLogger(PosTriggerForm.class.getName());
    private MapWidget map;
    private LabelField titleLabel;
    private TextBox radiusBox;
    private Radio radio1;
    private Radio radio2;
    private MyRadioGroup radioGroup2;
    private boolean radiusMode = false;
    private boolean customMode = false;
    private boolean insideMode = false;
    private ArrayList<LatLng> points;
    private LatLng[] points1;
    private ArrayList<IndexPolygon> circleList;
    private ArrayList<IndexMarker> markerList;
    private IndexPolygon polygon;
    private int currentMarkerIndex;
    private MarkerOptions options;
    private MarkerDragHandler markerDragHandler;
    private MarkerClickHandler removeCircleHandler;
    private PolygonClickHandler polygonClickHandler;
    private PolyStyleOptions normalStroke;
    private PolyStyleOptions normalFill;
    private PolyStyleOptions invertedStroke;
    private PolyStyleOptions invertedFill;
    private TextField<String> controlBox2;

    public PosTriggerForm() {
        super();
        LOG.setLevel(Level.ALL);
        setLayout(new BorderLayout());
        setHeaderVisible(false);
        setBodyBorder(false);
        this.setLayoutOnChange(true);
        createTitleLabel();

        initControls();
        initMapWidget();
        createControlField();
    }

    /**
     * This method is called when the window has already been shown. Without this, the map will not
     * be displayed properly (computer will think the size is 0);
     */
    public void afterShow() {

        LatLng sw1 = LatLng.newInstance(51.559087, 3.702393);
        LatLng ne = LatLng.newInstance(52.481888, 6.031494);

        LatLngBounds bounds = LatLngBounds.newInstance(sw1, ne);
        map.setCenter(bounds.getCenter());
        map.setZoomLevel(map.getBoundsZoomLevel(bounds));
        map.setUIToDefault();
    }

    /**
     * Checks whether the control box is filled (if it is, the user has created one or more valid
     * polygons)
     */
    private void checkControlBox() {
        if (circleList.size() == 0)
            controlBox2.clear();
        else
            controlBox2.setValue("1");
        // LOG.fine("Control box value is " + controlBox2.getValue());
    }

    /**
     * Draws a circle with a marker at the center of it, at a specified point
     */
    private void createCircle(LatLng point, double rad) {

        final IndexMarker mark = new IndexMarker(point, options);
        mark.setDraggingEnabled(true);
        mark.setIndex(currentMarkerIndex);
        map.addOverlay(mark);
        markerList.add(mark);

        IndexPolygon circle0 = drawCircleFromRadius(point, rad, 40);

        circleList.add(circle0);
        circle0.setIndex(currentMarkerIndex);
        map.addOverlay(circle0);

        mark.addMarkerDragHandler(markerDragHandler);
        mark.addMarkerClickHandler(removeCircleHandler);
        circle0.addPolygonClickHandler(polygonClickHandler);

        currentMarkerIndex++;
    }

    /**
     * Creates a control field to check whether the form has been filled, for the FormBinding
     */
    private void createControlField() {
        controlBox2 = new TextField<String>();
        controlBox2.setAllowBlank(false);
        controlBox2.setVisible(false);
        controlBox2.setValue(null);
        BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 35);
        this.add(controlBox2, data);

    }

    /**
     * Draws a polygon starting at a specified point
     */
    private void createPolygon(LatLng point) {
        if (point != null) {

            if (polygon == null) {
                points.add(point);
                points1 = null;
                points1 = new LatLng[points.size()];
                points.toArray(points1);
                polygon = new IndexPolygon(points1, "red", 2, 1, "red", 0.15);
                map.addOverlay(polygon);
                circleList.add(polygon);
            }

            polygon.setDrawingEnabled();
        }
    }

    /**
     * Create first radio button with "Radius, km", and add a clickListener
     */
    private void createRadio1() {
        radio1 = new Radio();
        radio1.setBoxLabel("Radius, km");
        radio1.setValue(true);
        radio1.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {

                radiusMode = true;
                customMode = false;
                map.clearOverlays();
                points.clear();
                polygon = null;
                currentMarkerIndex = 0;
                circleList = new ArrayList<IndexPolygon>();
                markerList = new ArrayList<IndexMarker>();
                checkControlBox();

            }
        });
    }

    /**
     * Create second radio button with "Custom", and add a clickListener
     */
    private void createRadio2() {
        radio2 = new Radio();
        radio2.setBoxLabel("Custom");
        radio2.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                radiusMode = false;
                customMode = true;
                map.clearOverlays();
                points.clear();
                if (polygon != null)
                    polygon = null;
                if (circleList != null)
                    circleList.clear();
                if (markerList != null)
                    markerList.clear();
                currentMarkerIndex = 0;
                checkControlBox();

            }
        });
    }

    /**
     * Create radio group to hold "Inside" and "Outside" radio buttons
     */
    private void createRadioGroup2() {
        Radio radio3 = new Radio();
        radio3.setBoxLabel("Inside");
        radio3.setValue(true);
        radiusMode = true;
        insideMode = true;

        radio3.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                insideMode = true;
            }
        });

        Radio radio4 = new Radio();
        radio4.setBoxLabel("Outside");

        radio4.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                insideMode = false;
            }
        });

        radioGroup2 = new MyRadioGroup();
        radioGroup2.add(radio3);
        radioGroup2.add(radio4);
    }

    /**
     * Create text box to type in the desired radius value
     */
    private void createRadiusBox() {
        radiusBox = new TextBox();
        radiusBox.setWidth("42px");
        radiusBox.setHeight("15px");
        radiusBox.setStyleName("textBox1");
        radiusBox.setValue("15");
    }

    /**
     * Create form title
     */
    private void createTitleLabel() {
        titleLabel = new LabelField("<b>Sensor with Position Values</b>");
        titleLabel.setHideLabel(true);
        titleLabel.setStyleName("titleLabel");
        this.add(titleLabel);

    }
    /**
     * Draws a circle with a given center, radius and number of points
     * 
     * @param center
     * @param radius
     * @param nbOfPoints
     * @return
     */
    private IndexPolygon drawCircleFromRadius(LatLng center, double radius, int nbOfPoints) {

        LatLngBounds bounds = LatLngBounds.newInstance();
        LatLng[] circlePoints = new LatLng[nbOfPoints + 1];

        double EARTH_RADIUS = 6371000;
        double d = radius / EARTH_RADIUS;
        double lat1 = Math.toRadians(center.getLatitude());
        double lng1 = Math.toRadians(center.getLongitude());

        double a = 0;
        double step = 360.0 / (double) nbOfPoints;

        for (int i = 0; i <= nbOfPoints; i++) {
            double tc = Math.toRadians(a);
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d)
                    * Math.cos(tc));
            double lng2 = lng1
                    + Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1),
                            Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
            LatLng point = LatLng.newInstance(Math.toDegrees(lat2), Math.toDegrees(lng2));
            circlePoints[i] = point;
            bounds.extend(point);
            a += step;
        }

        IndexPolygon circle = new IndexPolygon(circlePoints);
        circle.setPoints(circlePoints);
        circle.setFillStyle(normalFill);
        circle.setStrokeStyle(normalStroke);

        circle.setRadius(radius);

        return circle;
    }

    /**
     * Returns a positionTrigger with the circle or list of circles defined by the user
     * 
     * @return
     */
    public PositionTrigger getPositionTrigger() {

        PositionTrigger posTrigger = new PositionTrigger();

        if (circleList.size() == 0) {
            // LOG.fine ("CircleList is empty");
            return null;
        } else {
            posTrigger.setCircleList(circleList);
            posTrigger.setInsideMode(insideMode);
        }

        return posTrigger;
    }

    /**
     * Gets the circle radius entered by the user in the RadiusBox
     */
    private double getRadius(TextBox radius) {
        double rad = 15000;
        String text = radius.getText();
        if (!text.isEmpty()) {
            rad = Double.parseDouble(text) * 1000;
        }
        return rad;
    }

    /**
     * Create click and drag handlers for markers and polygons
     */
    private void initClickDragHandlers() {

        markerDragHandler = new MarkerDragHandler() {
            public void onDrag(MarkerDragEvent event) {
                IndexMarker sender = (IndexMarker) (event.getSender());
                moveCircle(sender);
            }
        };

        removeCircleHandler = new MarkerClickHandler() {
            public void onClick(MarkerClickEvent event) {
                IndexMarker sender = (IndexMarker) (event.getSender());
                map.removeOverlay(sender);

                for (int i = 0; i < circleList.size(); i++) {
                    if (i == sender.getIndex()) {
                        // LOG.fine ("CircleList length is " + circleList.size() +
                        // " Removing overlay " + sender.getIndex());
                        map.removeOverlay(circleList.get(i));
                        circleList.remove(i);
                        markerList.remove(i);
                        checkControlBox();
                        break;
                    }
                }
            }
        };

        polygonClickHandler = new PolygonClickHandler() {
            public void onClick(PolygonClickEvent event) {
                LatLng point = event.getLatLng();

                if (radiusMode && point != null) {
                    double rad = getRadius(radiusBox);
                    createCircle(point, rad);

                }
            }
        };
    }

    /**
     * Create a set of controls on the bottom
     */
    private void initControls() {

        createRadio1();
        createRadio2();
        createRadiusBox();

        RadioGroup radioGroup = new RadioGroup();
        radioGroup.setOrientation(Orientation.VERTICAL);
        radioGroup.add(radio1);
        radioGroup.add(radio2);

        createRadioGroup2();

        Grid g = new Grid(2, 1);
        g.setWidget(0, 0, radioGroup2);
        g.setStyleName("radioGrid");

        HorizontalPanel panel = new HorizontalPanel();
        HorizontalPanel outerPanel = new HorizontalPanel();

        panel.add(radioGroup);
        panel.add(radiusBox);

        outerPanel.add(panel);
        outerPanel.add(g);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 35);
        data.setMargins(new Margins(5, 5, 5, 5));
        layout();
        add(outerPanel, data);
    }

    /**
     * Initializes a Google map.
     */
    private void initMapWidget() {

        map = new MapWidget();
        map.setWidth("100%");
        // Add some controls for the zoom level
        map.setUIToDefault();

        setMarkerOptions();
        initPolygonOptions();
        initClickDragHandlers();

        // Add the map to the layout
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
        layoutData.setMargins(new Margins(15, 0, 0, 0));
        this.add(map, layoutData);

        points = new ArrayList<LatLng>();
        currentMarkerIndex = 0;
        circleList = new ArrayList<IndexPolygon>();
        markerList = new ArrayList<IndexMarker>();

        map.addMapClickHandler(new MapClickHandler() {

            public void onClick(MapClickEvent event) {

                LatLng point = event.getLatLng();
                // LOG.fine ("Latitude is " + point.getLatitude() + " longitude is " +
                // point.getLongitude());

                if (customMode) {
                    createPolygon(point);
                    checkControlBox();
                }

                if (radiusMode && point != null) {
                    double rad = getRadius(radiusBox);
                    createCircle(point, rad);
                    checkControlBox();

                }
            }
        });
    }

    /**
     * Sets polygon options
     */
    private void initPolygonOptions() {
        normalStroke = PolyStyleOptions.getInstance();
        normalStroke.setColor("red");
        normalStroke.setOpacity(1);
        normalStroke.setWeight(2);

        normalFill = PolyStyleOptions.getInstance();
        normalFill.setColor("red");
        normalFill.setOpacity(0.15);

        invertedStroke = PolyStyleOptions.getInstance();
        invertedStroke.setColor("red");
        invertedStroke.setOpacity(1);
        invertedStroke.setWeight(0);

        invertedFill = PolyStyleOptions.getInstance();
        invertedFill.setOpacity(0);
    }

    /**
     * Moves the circle when the marker is dragged
     */
    private void moveCircle(IndexMarker mark) {
        LatLng point = mark.getLatLng();

        for (int i = 0; i < circleList.size(); i++) {
            if (i == mark.getIndex()) {
                map.removeOverlay(circleList.get(i));
                double radius = circleList.get(i).getRadius();
                IndexPolygon updatedCircle = drawCircleFromRadius(point, radius, 40);
                updatedCircle.setIndex(i);
                circleList.remove(i);
                circleList.add(i, updatedCircle);

                updatedCircle.addPolygonClickHandler(polygonClickHandler);
                map.addOverlay(updatedCircle);
                break;
            }
        }
    }

    /**
     * Sets marker options for the circles
     */
    private void setMarkerOptions() {
        options = MarkerOptions.newInstance();
        options.setDraggable(true);
        options.setBouncy(false);
        options.setBounceGravity(4);
    }
}
