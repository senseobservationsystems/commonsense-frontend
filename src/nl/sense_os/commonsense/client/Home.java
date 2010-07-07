package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
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
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.LineChart;

import nl.sense_os.commonsense.client.widgets.LineChartTab;
import nl.sense_os.commonsense.client.widgets.MyriaTab;
import nl.sense_os.commonsense.client.widgets.PhoneTreePanel;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "Home";
    private final AsyncCallback<Void> mainCallback;
    private PhoneTreePanel phoneTreePanel;
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
        VisualizationUtils.loadVisualizationApi(vizCallback, LineChart.PACKAGE,
                AnnotatedTimeLine.PACKAGE);

        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense");
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

        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setPlain(true);

        // Welcome tab item
        final TabItem item = new TabItem("Welcome");
        item.add(new WelcomeTab(this.user.getName()));
        item.setClosable(false);
        this.tabPanel.add(item);
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

        final LayoutContainer panel = new LayoutContainer();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        this.phoneTreePanel = new PhoneTreePanel();
        TreePanelSelectionModel<SenseTreeModel> selectMdl = new TreePanelSelectionModel<SenseTreeModel>();
        selectMdl.addSelectionChangedListener(new SelectionChangedListener<SenseTreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SenseTreeModel> se) {
                SenseTreeModel node = se.getSelectedItem();
                // Log.d(TAG, "SelectionChanged. Selected node: " + node.get("text"));

                if (node instanceof SensorModel) {
                    onSensorClick((SensorModel) node);
                }
            }
        });
        selectMdl.bindTree(this.phoneTreePanel.getTree());

        panel.add(this.phoneTreePanel, new VBoxLayoutData());

        // spacer
        final VBoxLayoutData flex = new VBoxLayoutData();
        flex.setFlex(1);
        panel.add(new Text(), flex);

        // Log out button
        final Button logoutBtn = new Button("logout");
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
        panel.add(logoutBtn, new VBoxLayoutData(new Margins(0, 10, 10, 10)));

        return panel;
    }

    /**
     * Handles clicks on sensors in the TreePanel of the west panel. Selects the sensor's tab is it
     * is already present, otherwise the sensor's tab is created.
     * 
     * @param sensor
     *            the SensorModel of the sensor that was clicked.
     */
    private void onSensorClick(SensorModel sensor) {

        // try to reuse TabItem for this sensor
        String id = sensor.getPhoneId() + ". " + sensor.getName();
        TabItem item = this.tabPanel.getItemByItemId(id);
        if (null == item) {
            item = new TabItem(sensor.getName());
            item.setLayout(new FitLayout());

            if (sensor.getName().equals("temperature")) {
                item.add(new MyriaTab(sensor));
            } else {
                item.add(new LineChartTab(sensor));
            }
            item.setClosable(true);
            item.setId(id);
            this.tabPanel.add(item);
        }

        // select the appropriate tab item
        this.tabPanel.setSelection(item);
    }
}
