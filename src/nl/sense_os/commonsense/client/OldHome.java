package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
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
import nl.sense_os.commonsense.client.widgets.PhoneStateGrid;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.UserModel;

public class OldHome extends LayoutContainer {

    private static final String TAG = "Home";
    private DataServiceAsync dataSvc;
    private AsyncCallback<Void> mainCallback;
    private UserModel user;
    private LayoutContainer centerContainer;
    private LayoutContainer westContainer;
    private Text phoneMsg;
    private ListStore<SenseTreeModel> phoneStore;
    private PhoneStateGrid phoneGrid;
    private List<SenseTreeModel> phones;
    // private PhoneModel visiblePhone;
    // private SensorModel visibleSensor;
    private List<SenseTreeModel> sensors;
    private TabPanel tabPanel;
    private boolean isVizLoaded;

    public OldHome(UserModel user, AsyncCallback<Void> callback) {
        this.dataSvc = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;
        this.phoneStore = new ListStore<SenseTreeModel>();

        // Load the visualization api, passing the onLoadCallback to be called
        // when loading is done.
        Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Visualization loaded");
                OldHome.this.isVizLoaded = true;
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
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setAutoHeight(true);
        this.tabPanel.setAutoWidth(true);
        this.tabPanel.setPlain(true);

        // Welcome tab item
        TabItem item = new TabItem("Welcome");
        item.add(new WelcomeTab(this.user.getName()));
        item.setClosable(true);
        this.tabPanel.add(item);
        panel.add(this.tabPanel, new VBoxLayoutData(0, 0, 0, 0));

        return panel;
    }

    private LayoutContainer createWestPanel() {

        LayoutContainer panel = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        // Number of phones message
        if (this.phoneMsg == null) {
            this.phoneMsg = new Text();
        }
        panel.add(this.phoneMsg, new VBoxLayoutData(new Margins(10, 0, 10, 0)));

        // Phone selection ComboBox
        ComboBox<SenseTreeModel> phoneCombo = new ComboBox<SenseTreeModel>();
        phoneCombo.setDisplayField("text");
        phoneCombo.setTriggerAction(TriggerAction.ALL);
        phoneCombo.setEmptyText("Select a phone...");
        phoneCombo.setStore(this.phoneStore);
        phoneCombo.addSelectionChangedListener(new SelectionChangedListener<SenseTreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SenseTreeModel> se) {
                showPhoneInfo(se.getSelectedItem());
            }
        });
        panel.add(phoneCombo, new VBoxLayoutData(new Margins(10, 0, 10, 0)));

        // spacer
        VBoxLayoutData flex = new VBoxLayoutData(new Margins(0, 0, 5, 0));
        flex.setFlex(1);
        panel.add(new Text(), flex);

        // Log out button
        Button btnLogout = new Button("logout");
        btnLogout.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent be) {
                dataSvc.logout(new AsyncCallback<Void>() {
                    public void onFailure(Throwable ex) {
                        mainCallback.onFailure(ex);
                    }

                    public void onSuccess(Void result) {
                        mainCallback.onSuccess(null);
                    }
                });
            }
        });
        panel.add(btnLogout, new VBoxLayoutData(new Margins(0, 0, 10, 0)));

        return panel;
    }

    private void getPhoneDetails() {
        AsyncCallback<List<SenseTreeModel>> callback = new AsyncCallback<List<SenseTreeModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getPhoneDetails: " + ex.getMessage());
                onPhonesReceived(false);
            }

            public void onSuccess(List<SenseTreeModel> result) {
                OldHome.this.phones = result;
                onPhonesReceived(true);
            }
        };
        this.dataSvc.getPhoneDetails(callback);
    }

    private void getSensors(final PhoneModel phone) {
        AsyncCallback<List<SenseTreeModel>> callback = new AsyncCallback<List<SenseTreeModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensors: " + ex.getMessage());
                onSensorsReceived(false, null, null);
            }

            public void onSuccess(List<SenseTreeModel> result) {
                OldHome.this.sensors = result;
                onSensorsReceived(true, result, phone);
            }
        };

        this.dataSvc.getSensors(phone.getId(), callback);
    }

    private void getSensorValues(PhoneModel phone, final SensorModel sensor) {
        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensorValues: " + ex.getMessage());
                onSensorValuesReceived(false, null, null);
            }

            public void onSuccess(List<SensorValueModel> result) {
                onSensorValuesReceived(true, sensor, result);
            }
        };
        this.dataSvc.getSensorValues(phone.getId(), sensor.getId(),
                new Timestamp((new Date().getTime() - 365 * 24 * 60 * 60 * 1000)), new Timestamp(
                        new Date().getTime()), callback);
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

        this.westContainer = createWestPanel();
        BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST);
        west.setMargins(new Margins(5));
        contentPanel.add(this.westContainer, west);

        this.centerContainer = createCenterPanel();
        BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
        center.setMargins(new Margins(5));
        contentPanel.add(this.centerContainer, center);

        this.setLayout(new FitLayout());
        this.add(contentPanel);

        getPhoneDetails();
    }

    private void onPhonesReceived(boolean success) {
        Log.d(TAG, "onPhonesReceived");

        if (success) {
            if (this.phones.size() > 0) {

                // update
                this.phoneMsg.setText("Found " + phones.size() + " registered phones.");

                for (SenseTreeModel treeModel : phones) {
                    PhoneModel p = (PhoneModel) treeModel;
                    Log.d(TAG, p.getType() + " " + p.getImei());
                }

                this.phoneStore.removeAll();
                this.phoneStore.add(phones);

            } else {
                phoneMsg.setText("Error: no registered phones found.");
            }
            this.doLayout();
        }
    }

    private void onSensorValuesReceived(boolean success, SensorModel sensor,
            List<SensorValueModel> values) {

        if (true == success) {
            Log.d(TAG, "Received sensor values");

            TabItem item = new TabItem(sensor.getName());
            if (isVizLoaded) {
                DataTable data = DataTable.create();
                data.addColumn(ColumnType.DATETIME, "Date");
                data.addColumn(ColumnType.NUMBER, "Things per Day");

                data.addRows(values.size());
                for (int i = 0; i < values.size(); i++) {
                    SensorValueModel value = values.get(i);
                    Log.d(TAG, "SensorValue: " + value.getTimestamp() + ", " + value.getValue());
                    data.setValue(i, 0, value.getTimestamp());
                    Double d = 0.0;
                    try	{
                    	d = Double.valueOf(value.getValue());
                    } catch (NumberFormatException e) {
                    }
                    data.setValue(i, 1, d);
                }

                LineChart.Options options = LineChart.Options.create();
                options.setWidth(400);
                options.setHeight(240);
                options.setTitle("Chart");

                LineChart lineChart = new LineChart(data, options);
                item.add(lineChart);
            }

            this.tabPanel.add(item);
        }
    }

    private void onSensorsReceived(boolean success, List<SenseTreeModel> sensors, PhoneModel phone) {

        if (success) {
            Log.d(TAG, "onSensorsReceived");

            this.tabPanel.removeAll();

            // welcome tab
            TabItem welcome = new TabItem("Welcome");
            welcome.add(new WelcomeTab(this.user.getName()));
            welcome.setClosable(true);
            this.tabPanel.add(welcome);

            // sensor tabs
            if (this.sensors.size() > 0) {

                for (SenseTreeModel treeModel : sensors) {
                    SensorModel sensor = (SensorModel) treeModel;
                    Log.d(TAG, "Sensor: " + sensor.getId() + " " + sensor.getName());

                    getSensorValues(phone, sensor);
                }
            } else {
                // Dummy tab item
                TabItem dummy = new TabItem("None");
                dummy.setEnabled(false);
                this.tabPanel.add(dummy);
            }
        } else {
            Log.e(TAG, "Failed gettings Sensors");
        }
    }

    private void showPhoneInfo(SenseTreeModel phone) {
        Log.d(TAG, "showSelectedPhoneInfo");

        if (null == this.phoneGrid) {
            this.phoneGrid = new PhoneStateGrid((PhoneModel) phone);
            this.westContainer.insert(this.phoneGrid, 2, new VBoxLayoutData(new Margins(10, 0, 10,
                    0)));
            this.layout();
        } else {
            this.phoneGrid.setPhone((PhoneModel) phone);
        }

        getSensors((PhoneModel) phone);
    }
}
