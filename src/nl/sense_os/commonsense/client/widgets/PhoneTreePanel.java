package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorModel;

public class PhoneTreePanel extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "PhoneTreePanel";    
    private TreePanel<SenseTreeModel> tree;      

    /**
     * Constructs the ContentPanel with a TreePanel and Buttons to expand and collapse the tree.
     */
    public PhoneTreePanel() {
                
        VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        this.setLayout(new FlowLayout());
        this.setSize(315, 400);
        
        ContentPanel cp = createTreePanel();        
        cp.setHeading("Device explorer");  
        cp.setLayout(new FitLayout());
        cp.setSize(200, 400);
        
        ButtonBar buttonBar = createButtonBar(tree);
        
        this.add(buttonBar, new FlowData(10));
        this.add(cp);
    }
    
    /**
     * Creates bar with buttons to expand and collapse all tree elements
     * @param tree
     * @return the button bar
     */
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
     * @return the tree
     */
    private ContentPanel createTreePanel() {
        
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
                    return "node_" + model.get("phone") + "-" + model.get("id");
                } else if (model instanceof PhoneModel) {
                    return "phone_" + model.<String> get("id");
                } else {
                    return "foo";
                }
            }
        });

        tree = new TreePanel<SenseTreeModel>(store);
        tree.setStateful(true);
        tree.setId("idNecessaryForStatefulSetting");
        tree.setDisplayProperty("text");
        
        
        ContentPanel cp = new ContentPanel();
        cp.add(tree);  
        
        return cp;
    }
    
    public TreePanel<SenseTreeModel> getTree() {
        return tree;
    }
}
