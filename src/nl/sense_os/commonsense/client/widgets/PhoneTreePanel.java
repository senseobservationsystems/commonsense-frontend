package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;

public class PhoneTreePanel extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "PhoneTreePanel";
    private TreePanel<SenseTreeModel> tree;

    /**
     * Constructs the ContentPanel with a TreePanel and Buttons to expand and collapse the tree.
     */
    public PhoneTreePanel() {

        this.tree = createTreePanel();

        TreePanelSelectionModel<SenseTreeModel> selectMdl = new TreePanelSelectionModel<SenseTreeModel>();
        selectMdl.bindTree(this.tree);
        // ButtonBar buttonBar = createButtonBar(this.tree);

        this.setLayout(new RowLayout(Orientation.VERTICAL));
        this.setHeading("Device explorer");
        this.setCollapsible(true);
        this.add(this.tree, new RowData(1, -1, new Margins(0, 0, 10, 0)));
        // this.add(buttonBar, new RowData(1, -1, new Margins(10,0,10,0)));
    }

    /**
     * Creates bar with buttons to expand and collapse all tree elements
     * 
     * @param tree
     * @return the button bar
     */
    @SuppressWarnings("unused")
    private ButtonBar createButtonBar(final TreePanel<SenseTreeModel> tree) {
        ButtonBar bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);
        bar.add(new Button("Expand All", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                tree.expandAll();
            }
        }));
        bar.add(new Button("Collapse All", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                tree.collapseAll();
            }
        }));

        return bar;
    }

    /**
     * Creates an tree of PhoneModels and SensorModels, which are fetched asynchronously.
     * 
     * @return the tree
     */
    private TreePanel<SenseTreeModel> createTreePanel() {

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        // data proxy
        RpcProxy<List<SenseTreeModel>> proxy = new RpcProxy<List<SenseTreeModel>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<SenseTreeModel>> callback) {

                if (loadConfig == null) {
                    service.getPhoneDetails(callback);
                } else if (loadConfig instanceof PhoneModel) {
                    String phoneId = ((PhoneModel) loadConfig).getId();
                    service.getSensors(phoneId, callback);
                } else if (loadConfig instanceof SensorModel) {

                } else {
                    Log.e("RpcProxy", "loadConfig unexpected type");
                }
            }
        };

        // tree loader
        TreeLoader<SenseTreeModel> loader = new BaseTreeLoader<SenseTreeModel>(proxy) {
            @Override
            public boolean hasChildren(SenseTreeModel parent) {
                return parent instanceof PhoneModel;
            }
        };

        // trees store
        TreeStore<SenseTreeModel> store = new TreeStore<SenseTreeModel>(loader);
        store.setKeyProvider(new ModelKeyProvider<SenseTreeModel>() {
            public String getKey(SenseTreeModel model) {
                if (model instanceof SensorModel) {
                    SensorModel sensor = (SensorModel) model;
                    return "node_" + sensor.getPhoneId() + "-" + sensor.getId();
                } else if (model instanceof PhoneModel) {
                    PhoneModel phone = (PhoneModel) model;
                    return "phone_" + phone.getId();
                } else {
                    return "UNKNOWN_INSTANCE";
                }
            }
        });

        TreePanel<SenseTreeModel> treePanel = new TreePanel<SenseTreeModel>(store);
        treePanel.setStateful(true);
        treePanel.setId("idNecessaryForStatefulSetting");
        treePanel.setDisplayProperty("text");
        treePanel.getStyle().setLeafIcon(IconHelper.create("gxt/images/default/tree/leaf.gif"));
        treePanel.setCheckable(true);

        return treePanel;
    }

    public List<SenseTreeModel> getSelection() {
        return this.tree.getCheckedSelection();
    }
}
