package nl.sense_os.commonsense.client;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.axis.XAxis;
import com.extjs.gxt.charts.client.model.charts.DataConfig;
import com.extjs.gxt.charts.client.model.charts.LineChart;
import com.extjs.gxt.charts.client.model.charts.dots.Dot;
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
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.PhoneStateGrid;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    private static final String TAG = "Home";
    private DataServiceAsync dataSvc;
    private AsyncCallback<Void> mainCallback;
    private UserModel user;
    private LayoutContainer centerContainer;
    private LayoutContainer westContainer;
    private Text phoneMsg;
    private ListStore<PhoneModel> phoneStore;
    private PhoneStateGrid phoneGrid;
    private List<PhoneModel> phones;
    private List<SensorModel> sensors;
    private TabPanel tabPanel;
    
    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.dataSvc = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;
        this.phoneStore = new ListStore<PhoneModel>();
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
        ComboBox<PhoneModel> phoneCombo = new ComboBox<PhoneModel>();
        phoneCombo.setDisplayField("text");
        phoneCombo.setTriggerAction(TriggerAction.ALL);
        phoneCombo.setEmptyText("Select a phone...");
        phoneCombo.setStore(this.phoneStore);
        phoneCombo.addSelectionChangedListener(new SelectionChangedListener<PhoneModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<PhoneModel> se) {
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
        AsyncCallback<List<PhoneModel>> callback = new AsyncCallback<List<PhoneModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Phone details fetching failed: " + ex.getMessage());
                onPhonesReceived(false);
            }

            public void onSuccess(List<PhoneModel> result) {
                Home.this.phones = result;
                onPhonesReceived(true);
            }
        };
        this.dataSvc.getPhoneDetails(callback);
    }
    
	private void getSensorDetails(final PhoneModel phone) {
        AsyncCallback<List<SensorModel>> callback = new AsyncCallback<List<SensorModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Phone details fetching failed: " + ex.getMessage());
                onSensorsReceived(false, null);
            }

            public void onSuccess(List<SensorModel> result) {
                Home.this.sensors = result;                
                onSensorsReceived(true, phone);
            }
        };

        this.dataSvc.getSensors(phone.getId(), callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        Log.d(TAG, "onRender");
        super.onRender(parent, index);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense");
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setSize(1024, 768);
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

        this.setLayout(new CenterLayout());
        this.add(contentPanel);

        getPhoneDetails();
    }

    private void onPhonesReceived(boolean success) {
        Log.d(TAG, "onPhonesReceived");

        if (success) {
            if (this.phones.size() > 0) {

                // update
                this.phoneMsg.setText("Found " + phones.size() + " registered phones.");

                for (PhoneModel p : phones) {
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
    
    private void onSensorsReceived(boolean success, PhoneModel phone) {
        Log.d(TAG, "onSensorsReceived");
        
        if (success) {

            this.tabPanel.removeAll();

            // welcome tab
            TabItem welcome = new TabItem("Welcome");
            welcome.add(new WelcomeTab(this.user.getName()));
            welcome.setClosable(true);
            this.tabPanel.add(welcome);
            
            // sensor tabs
            if (this.sensors.size() > 0) {
                
                for (SensorModel sensor : this.sensors) {
                    Log.d(TAG, "Sensor: " + sensor.getId() + " " + sensor.getName());                    
                    
                    final TabItem item = new TabItem(sensor.getName());
                    item.addText(sensor.getId() + ". " + sensor.getName());
                    item.setHeight("100%");                    

                    // sensor test stuff
                    /*
                    this.dataSvc.getSensorValues(phone.getId(), sensor.getId(), new Timestamp((new Date().getTime()-1000000000)), new Timestamp (new Date().getTime()), new AsyncCallback<List<SensorValueModel>>() {
                        public void onFailure(Throwable ex) {
                        }
                        public void onSuccess(List<SensorValueModel> result) {
                        	item.addText("Received " + result.size() + " sensor values");
                        }
                    });
                    */
                    
                    ContentPanel cp = new ContentPanel();                    
                    cp.setHeading("Chart ContentPanel");  
                    cp.setFrame(true);  
                    cp.setSize(400, 400);  
                    cp.setLayout(new FitLayout());
                    
                    Chart chart = new Chart("gxt/chart/open-flash-chart.swf");
                    
                    ChartModel chartModel = new ChartModel("ChartModel");
                    XAxis xAxis = new XAxis();
                    xAxis.setLabels("0", "1", "2", "3", "4");
                    xAxis.setRange(0, 8);
                    chartModel.setXAxis(xAxis);
                    
                    LineChart lineChart = new LineChart();
                    
                    ArrayList<DataConfig> values = new ArrayList<DataConfig>();
                    for (int i=0; i<5; i++) {
                        Dot dot = new Dot();
                        dot.setXY(i, Random.nextDouble());
                        values.add(dot);
                    }
                    lineChart.setValues(values);
                    chartModel.addChartConfig(lineChart);

                    chart.setChartModel(chartModel);
                    
                    cp.add(chart);
                    
                    item.add(cp);
                    
                    this.tabPanel.add(item);
                }
            } else {
                // Dummy tab item
                TabItem dummy = new TabItem("None");
                dummy.setEnabled(false);
                this.tabPanel.add(dummy);
            }
        }
    }

    private void showPhoneInfo(PhoneModel phone) {
        Log.d(TAG, "showSelectedPhoneInfo");

        if (null == this.phoneGrid) {
            this.phoneGrid = new PhoneStateGrid(phone);
            this.westContainer.insert(this.phoneGrid, 2, new VBoxLayoutData(new Margins(10, 0, 10,
                    0)));
            this.layout();
        } else {
            this.phoneGrid.setPhone(phone);
        }

        getSensorDetails(phone);
    }
}
