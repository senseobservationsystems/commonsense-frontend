package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.PieChart;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.PhoneTreePanel;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    private static final String TAG = "Home";
    private DataServiceAsync service;
    private boolean isVizLoaded;
    private AsyncCallback<Void> mainCallback;
    PhoneTreePanel phoneTreePanel;
    private TabPanel tabPanel;
    private UserModel user;

    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.service = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;

        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
        Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Visualization loaded");
                Home.this.isVizLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, PieChart.PACKAGE);
    }

    private LayoutContainer createCenterPanel() {
        Log.d(TAG, "createCenterPanel");

        LayoutContainer panel = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setPlain(true);

        // Welcome tab item
        TabItem item = new TabItem("Welcome");
        item.add(new WelcomeTab(this.user.getName()));
        item.setClosable(true);
        this.tabPanel.add(item);
        panel.add(this.tabPanel);
        
        return panel;
    }

    private LayoutContainer createWestPanel() {

        LayoutContainer panel = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        this.phoneTreePanel = new PhoneTreePanel();
        this.phoneTreePanel.getTree().addListener(Events.OnClick, new Listener<TreePanelEvent<SenseTreeModel>>() {

            @Override
            public void handleEvent(TreePanelEvent<SenseTreeModel> be) {                
                SenseTreeModel node = be.getTreePanel().getSelectionModel().getSelectedItem();
                Log.d(TAG, "SelectionChange: " + node.get("text"));
                
                if (node instanceof SensorModel) {
                    SensorModel sensor = (SensorModel) node;
                    getSensorValues(sensor);
                }
            }
        });
        panel.add(this.phoneTreePanel, new VBoxLayoutData());

        // spacer
        VBoxLayoutData flex = new VBoxLayoutData();
        flex.setFlex(1);
        panel.add(new Text(), flex);

        // Log out button
        Button logoutBtn = new Button("logout");
        logoutBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent be) {
                service.logout(new AsyncCallback<Void>() {
                    public void onFailure(Throwable ex) {
                        mainCallback.onFailure(ex);
                    }

                    public void onSuccess(Void result) {
                        mainCallback.onSuccess(null);
                    }
                });
            }
        });
        panel.add(logoutBtn, new VBoxLayoutData(new Margins(0, 10, 10, 10)));

        return panel;
    }

    private void getSensorValues(final SensorModel sensor) {
        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensorValues: " + ex.getMessage());
                onSensorValuesReceived(false, null, null);
            }

            public void onSuccess(List<SensorValueModel> result) {
                onSensorValuesReceived(true, sensor, result);
            }
        };

        Timestamp start = new Timestamp((new Date().getTime() - 365 * 24 * 60 * 60 * 1000));
        Timestamp end = new Timestamp(new Date().getTime());
        this.service.getSensorValues(sensor.getPhone(), sensor.getId(), start, end, callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        Log.d(TAG, "onRender");
        super.onRender(parent, index);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense");
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setSize("100%", "100%");
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);

        LayoutContainer westContainer = createWestPanel();
        BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST);
        west.setMargins(new Margins(5));
        contentPanel.add(westContainer, west);

        LayoutContainer centerContainer = createCenterPanel();
        BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
        center.setMargins(new Margins(5));
        contentPanel.add(centerContainer, center);

        this.setLayout(new FitLayout());
        this.add(contentPanel);

        // getPhoneDetails(); // removed after AsyncTreePanel
    }

    private void onSensorValuesReceived(boolean success, SensorModel sensor,
            List<SensorValueModel> values) {

        if (true == success) {
            Log.d(TAG, "Received sensor values");

            TabItem item = new TabItem(sensor.getName());
            item.setLayout(new FitLayout());
            item.setClosable(true);
            if (isVizLoaded) {
                DataTable data = DataTable.create();
                data.addColumn(ColumnType.DATETIME, "Date");
                data.addColumn(ColumnType.NUMBER, "Things per Day");

                data.addRows(values.size());
                for (int i = 0; i < values.size(); i++) {
                    SensorValueModel value = values.get(i);
                    data.setValue(i, 0, value.getTimestamp());
                    Double d = 0.0;
                    try {
                        d = Double.valueOf(value.getValue());
                    } catch (NumberFormatException e) {
                    }
                    data.setValue(i, 1, d);
                }

                LineChart.Options options = LineChart.Options.create();
                options.set("width", "75%");
                options.set("height", "75%");
                options.setTitle("Chart");

                LineChart lineChart = new LineChart(data, options);
                item.add(lineChart);
            }

            this.tabPanel.add(item);
        } else {
            Log.e(TAG, "Failed getting sensor values.");
        }
    }
}
