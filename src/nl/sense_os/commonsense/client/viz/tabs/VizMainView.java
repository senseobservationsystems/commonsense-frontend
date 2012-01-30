package nl.sense_os.commonsense.client.viz.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.main.MainEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;

public class VizMainView extends View {

    private static final Logger LOG = Logger.getLogger(VizMainView.class.getName());
    private VizTabPanel tabPanel;

    public VizMainView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(VizEvents.Show)) {
            LOG.finest("Show");
            final LayoutContainer parent = event.<LayoutContainer> getData("parent");
            showPanel(parent);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            LOG.finest("LoggedOut");
            onLoggedOut(event);

        } else {
            LOG.severe("Unexpected event type: " + type);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        tabPanel = new VizTabPanel();
        setupDragDrop();

        Registry.register(Constants.REG_VIZPANEL, tabPanel);
    }

    private void onLoggedOut(AppEvent event) {
        resetTabs();
    }

    /**
     * Handles a visualization request by displaying a dialog for the preferred action to take.
     * 
     * @param treeStoreModels
     *            list of dropped tags
     * @see #setupDragDrop()
     */
    private void onTagsDropped(List<TreeStoreModel> treeStoreModels) {

        // get the children of node tags
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (TreeStoreModel tsm : treeStoreModels) {
            final TreeModel tag = (TreeModel) tsm.getModel();
            if (false == sensors.contains(tag)) {

                if (tag instanceof SensorModel) {
                    sensors.add((SensorModel) tag);
                } else {
                    // add any children
                    for (ModelData model : tsm.getChildren()) {
                        TreeStoreModel tm = (TreeStoreModel) model;
                        TreeModel child = (TreeModel) tm.getModel();
                        if (false == sensors.contains(child)) {
                            if (child instanceof SensorModel) {
                                sensors.add((SensorModel) child);
                            }
                        }
                    }
                }
            }
        }

        showTypeChoice(sensors);
    }

    private void resetTabs() {
        for (TabItem item : tabPanel.getItems()) {
            if (!item.equals(tabPanel.getTabItemWelcome())) {
                tabPanel.remove(item);
            }
        }
    }

    /**
     * Sets up the tab panel for drag and drop of the tags.
     * 
     * @see #onTagsDropped(ArrayList)
     */
    private void setupDragDrop() {
        final DropTarget dropTarget = new DropTarget(tabPanel);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {

            @Override
            public void dragDrop(DNDEvent e) {
                Object data = e.getData();
                if (data instanceof List) {
                    Object listEntry = ((List<?>) data).get(0);
                    if (listEntry instanceof TreeStoreModel) {
                        @SuppressWarnings("unchecked")
                        List<TreeStoreModel> list = (List<TreeStoreModel>) data;
                        onTagsDropped(list);
                    } else if (listEntry instanceof SensorModel) {
                        @SuppressWarnings("unchecked")
                        List<SensorModel> list = (List<SensorModel>) data;
                        showTypeChoice(list);
                    } else {
                        LOG.fine("Unknown list type: " + listEntry);
                    }
                } else {
                    LOG.warning("Cannot handle dropped data: " + data);
                }
            }
        });
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(tabPanel);
            parent.layout();
        } else {
            LOG.severe("Failed to show visualization panel: parent=null");
        }
    }

    private void showTypeChoice(List<SensorModel> sensors) {
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, sensors);
    }
}
