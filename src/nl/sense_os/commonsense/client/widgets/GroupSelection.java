package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;

public class GroupSelection extends ContentPanel {

    public class GenerateEvent extends BaseEvent {

        public List<SensorModel> sensors;

        public GenerateEvent(EventType type) {
            super(type);
        }
    }

    private static final String TAG = "GroupSelection";
    private final TreePanel<SenseTreeModel> tree;
    private final DatePicker picker = new DatePicker();
    private final RadioGroup radioGroup = new RadioGroup();

    public GroupSelection() {
        // tree panel with selectable groups
        this.tree = createTreePanel();

        // button bar to collapse / expand all groups
        // ButtonBar buttonBar = createButtonBar(this.tree);

        // time range radio buttons
        final Text textRange = new Text("Select time range:");
        createRangeRadioGroup();

        // end date picker
        final Text textDate = new Text("Select END date for sensor values:");
        this.picker.setValue(new Date());

        setLayout(new RowLayout(Orientation.VERTICAL));
        setHeading("Group/time selection");
        setCollapsible(true);

        // Generate button
        final Button generateBtn = new Button("Generate charts");
        generateBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                onGenerate();
            }
        });

        this.add(this.tree, new RowData(1, -1, new Margins(0, 0, 10, 0)));
        // this.add(buttonBar, new RowData(1, -1, new Margins(10,0,10,0)));
        this.add(textRange, new RowData(1, -1, new Margins(10, 5, 0, 5)));
        this.add(this.radioGroup, new RowData(1, -1, new Margins(0, 5, 10, 5)));
        this.add(textDate, new RowData(1, -1, new Margins(10, 5, 0, 5)));
        this.add(this.picker, new RowData(1, -1, new Margins(0, 5, 10, 5)));
        this.add(generateBtn, new RowData(1, -1, new Margins(5, 5, 10, 5)));
    }

    /**
     * Creates bar with buttons to expand and collapse all tree elements
     * 
     * @param tree
     * @return the button bar
     */
    @SuppressWarnings("unused")
    private ButtonBar createButtonBar(final TreePanel<SenseTreeModel> tree) {
        final ButtonBar bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);
        bar.add(new Button("Expand All", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                tree.expandAll();
            }
        }));
        bar.add(new Button("Collapse All", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                tree.collapseAll();
            }
        }));

        return bar;
    }

    public void createRangeRadioGroup() {

        final Radio radio1d = new Radio();
        radio1d.setId("1d");
        radio1d.setBoxLabel("1d");

        final Radio radio7d = new Radio();
        radio7d.setId("7d");
        radio7d.setBoxLabel("7d");
        radio7d.setValue(true);

        final Radio radio1m = new Radio();
        radio1m.setId("1m");
        radio1m.setBoxLabel("1m");

        final Radio radio3m = new Radio();
        radio3m.setId("3m");
        radio3m.setBoxLabel("3m");

        this.radioGroup.add(radio1d);
        this.radioGroup.add(radio7d);
        this.radioGroup.add(radio1m);
        this.radioGroup.add(radio3m);
        this.radioGroup.setOriginalValue(radio7d);
    }

    /**
     * Creates an tree of PhoneModels and SensorModels, which are fetched asynchronously.
     * 
     * @return the tree
     */
    private TreePanel<SenseTreeModel> createTreePanel() {

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        // data proxy
        final RpcProxy<List<SenseTreeModel>> proxy = new RpcProxy<List<SenseTreeModel>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<SenseTreeModel>> callback) {

                if (loadConfig == null) {
                    service.getPhoneDetails(callback);
                } else if (loadConfig instanceof PhoneModel) {
                    final String phoneId = ((PhoneModel) loadConfig).getId();
                    service.getSensors(phoneId, callback);
                } else if (loadConfig instanceof SensorModel) {

                } else {
                    Log.e("RpcProxy", "loadConfig unexpected type");
                }
            }
        };

        // tree loader
        final TreeLoader<SenseTreeModel> loader = new BaseTreeLoader<SenseTreeModel>(proxy) {
            @Override
            public boolean hasChildren(SenseTreeModel parent) {
                return parent instanceof PhoneModel;
            }
        };

        // trees store
        final TreeStore<SenseTreeModel> store = new TreeStore<SenseTreeModel>(loader);
        store.setKeyProvider(new ModelKeyProvider<SenseTreeModel>() {
            @Override
            public String getKey(SenseTreeModel model) {
                if (model instanceof SensorModel) {
                    final SensorModel sensor = (SensorModel) model;
                    return "node_" + sensor.getPhoneId() + "-" + sensor.getId();
                } else if (model instanceof PhoneModel) {
                    final PhoneModel phone = (PhoneModel) model;
                    return "phone_" + phone.getId();
                } else {
                    return "UNKNOWN_INSTANCE";
                }
            }
        });

        final TreePanel<SenseTreeModel> treePanel = new TreePanel<SenseTreeModel>(store);
        treePanel.setStateful(true);
        treePanel.setId("idNecessaryForStatefulSetting");
        treePanel.setDisplayProperty("text");
        treePanel.getStyle().setLeafIcon(IconHelper.create("gxt/images/default/tree/leaf.gif"));
        treePanel.setCheckable(true);
        final TreePanelSelectionModel<SenseTreeModel> selectMdl = new TreePanelSelectionModel<SenseTreeModel>();
        selectMdl.bindTree(treePanel);

        return treePanel;
    }

    private long[] getTimeRange() {

        // contants
        final long day = 1 * 24 * 60 * 60 * 1000;
        final long week = 7 * day;
        final long month = 31 * day; // explicit calculation in long type
        final long quarter = 3 * month;

        // read off selected date
        final Date endDate = this.picker.getValue();
        final Date today = new Date();
        final boolean isToday = ((endDate.compareTo(today) < 0) && (today.getTime()
                - endDate.getTime() < day));
        final long end = isToday ? today.getTime() : endDate.getTime() + day;
        long start = 0;
        final String radioId = this.radioGroup.getValue().getId();
        if (radioId.equals("1d")) {
            start = end - day;
        } else if (radioId.equals("7d")) {
            start = end - week;
        } else if (radioId.equals("1m")) {
            start = end - month;
        } else if (radioId.equals("3m")) {
            start = end - quarter;
        } else {
            Log.w(TAG, "Unexpected time range: " + radioId);
        }

        return new long[] { start, end };
    }

    private void onGenerate() {

        // get selected sensors
        final List<SenseTreeModel> selected = this.tree.getCheckedSelection();
        final List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (final SenseTreeModel model : selected) {
            if (model instanceof SensorModel) {
                sensors.add((SensorModel) model);
            }
        }

        // get selected time range
        final long[] timeRange = getTimeRange();

        fireEvent(Events.Activate, new AppEvent(Events.Activate,
                new Object[] { sensors, timeRange }));
    }
}
