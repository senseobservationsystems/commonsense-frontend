package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.widgets.LineChartTab;
import nl.sense_os.commonsense.client.widgets.MyriaTab;
import nl.sense_os.commonsense.client.widgets.PeriodSelection;
import nl.sense_os.commonsense.client.widgets.PhoneTreePanel;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "Home";
    private final AsyncCallback<Void> mainCallback;
    private final PeriodSelection periodSelection = new PeriodSelection();
    private final PhoneTreePanel phoneTreePanel = new PhoneTreePanel();
    private final DataServiceAsync service;
    private TabPanel tabPanel;
    private final UserModel user;

    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.service = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;

        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                // do nothing
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, AnnotatedTimeLine.PACKAGE, MotionChart.PACKAGE);

        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense Web Application");
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setSize("100%", "100%");
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);

        final LayoutContainer west = createWestPanel();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        westLayout.setMargins(new Margins(5));
        contentPanel.add(west, westLayout);

        final LayoutContainer center = createCenterPanel();
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));
        contentPanel.add(center, centerLayout);

        setLayout(new FitLayout());
        this.add(contentPanel);
    }

    /**
     * Creates the "center" panel of the main BorderLayout. Contains only the tabPanel for the
     * sensor values.
     * 
     * @return the panel's LayoutContainer.
     */
    private LayoutContainer createCenterPanel() {

        // Welcome tab item
        final TabItem item = new TabItem("Welcome");
        item.setLayout(new FitLayout());
        item.add(new WelcomeTab(this.user.getName()));
        item.setClosable(false);
        item.setScrollMode(Scroll.AUTO);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setPlain(true);
        this.tabPanel.add(item);

        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        panel.add(this.tabPanel);

        return panel;
    }

    /**
     * Creates the "west" panel of the main BorderLayout. Contains the TreePanel with phones and
     * sensor, and the logout button.
     * 
     * @return the panel's LayoutContainer
     */
    private LayoutContainer createWestPanel() {

        final Image logo = new Image("/img/logo_sense-150.png");
        final LayoutContainer logoContainer = new LayoutContainer();
        logoContainer.setLayout(new CenterLayout());
        logo.addLoadHandler(new LoadHandler() {            
            @Override
            public void onLoad(LoadEvent event) {
                logoContainer.setHeight(logo.getHeight());
            }
        });
        logoContainer.add(logo);

        // Generate button
        final Button generateBtn = new Button("Generate charts");
        generateBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                List<SensorModel> sensors = new ArrayList<SensorModel>();
                for (SenseTreeModel model : phoneTreePanel.getSelection()) {
                    if (model instanceof SensorModel) {
                        sensors.add((SensorModel) model);
                    }
                }
                onGenerate(sensors);
            }
        });

        // Log out button
        final LayoutContainer logoutContainer = new LayoutContainer(new VBoxLayout());
        final Button logoutBtn = new Button("Log out");
        logoutBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                Home.this.service.logout(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        Home.this.mainCallback.onFailure(ex);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Home.this.mainCallback.onSuccess(null);
                    }
                });
            }
        });
        
        VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
        final LayoutContainer panel = new LayoutContainer(layout);
//        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setScrollMode(Scroll.AUTO);
        panel.add(logoContainer, new VBoxLayoutData(0,0,0,0));//, new RowData(1, -1, new Margins(0)));
        panel.add(this.phoneTreePanel, new VBoxLayoutData(10,0,10,0));//, new RowData(1, -1, new Margins(0)));
        panel.add(this.periodSelection, new VBoxLayoutData(10,0,10,0));//, new RowData(1, -1, new Margins(0)));
        panel.add(generateBtn, new VBoxLayoutData(10,10,10,10));//, new RowData(1, -1, new Margins(5)));
        panel.add(logoutBtn, new VBoxLayoutData(10,10,10,10));//, new RowData(1, -1, new Margins(5)));

        LayoutContainer wrapper = new LayoutContainer(new FitLayout());
        wrapper.setSize("100%", "100%");
        wrapper.add(panel);
        
        return wrapper;
    }

    /**
     * Handles clicks on sensors in the TreePanel of the west panel. Selects the sensor's tab is it
     * is already present, otherwise the sensor's tab is created.
     * 
     * @param sensors
     *            list of the sensors that are selected
     */
    private void onGenerate(List<SensorModel> sensors) {

        for (SensorModel sensor : sensors) {
            // close TabItem for this sensor
            String id = sensor.getPhoneId() + ". " + sensor.getName();
            
            TabItem item = new TabItem(sensor.getName());
            item.setLayout(new FitLayout());

            if ((sensor.getName().equals("temperature")) || sensor.getName().equals("humidity")) {
                long[] timeRange = this.periodSelection.getTimeRange();
                item.add(new MyriaTab(sensor, timeRange));
            } else {
                item.add(new LineChartTab(sensor));
            }
            item.setClosable(true);
            item.setId(id);
            this.tabPanel.add(item);

            // select the appropriate tab item
            this.tabPanel.setSelection(item);
        }
    }
}
