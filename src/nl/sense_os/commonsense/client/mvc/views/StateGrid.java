package nl.sense_os.commonsense.client.mvc.views;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;

public class StateGrid extends View {

    protected static final String TAG = "StateGrid";
    private ContentPanel panel;
    private TreeGrid<TreeModel> grid;
    private TreeStore<TreeModel> store;
    private Button createButton;
    private Button removeButton;

    public StateGrid(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowGrid)) {
            onShow(event);
        } else if (type.equals(StateEvents.ListNotUpdated)) {
            Log.w(TAG, "ListNotUpdated");
            onGroupsNotUpdated(event);
        } else if (type.equals(StateEvents.ListUpdated)) {
            Log.d(TAG, "ListUpdated");
            onGroupsUpdated(event);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (type.equals(StateEvents.RemoveComplete)) {
            Log.d(TAG, "RemoveComplete");
            onRemoveComplete(event);
        } else if (type.equals(StateEvents.RemoveFailed)) {
            Log.d(TAG, "RemoveFailed");
            onRemoveFailed(event);
        } else if (type.equals(StateEvents.Working)) {
            Log.d(TAG, "Working");
            setBusy(true);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void onRemoveFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to update sharing settings, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            removeService();
                        }
                    }
                });
    }

    private void onRemoveComplete(AppEvent event) {
        setBusy(false);
        Dispatcher.forwardEvent(StateEvents.ListRequested);
    }

    private void initGrid() {
        this.store = new TreeStore<TreeModel>();

        ColumnConfig id = new ColumnConfig("id", "Id", 50);
        ColumnConfig name = new ColumnConfig("name", "Name", 150);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, id));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("stateGrid");
        this.grid.setStateful(true);

        // add grid to panel
        this.panel.add(this.grid);
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.get().dispatch(StateEvents.ListRequested);
            }
        });

        // add to panel
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Manage states");
        this.panel.setAnimCollapse(false);

        initGrid();
        initHeaderTool();
        initToolBar();
    }

    private void initToolBar() {
        TreeGridSelectionModel<TreeModel> selectionModel = new TreeGridSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection && selection.get("service_name") == null) {
                    removeButton.enable();
                } else {
                    removeButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);
        
        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    onCreateClick();
                } else if (source.equals(removeButton)) {
                    confirmRemove();
                } else  {
                    Log.w(TAG, "Unexpected button clicked");
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.removeButton = new Button("Remove", l);
        this.removeButton.disable();

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.createButton);
        toolBar.add(this.removeButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    protected void onCreateClick() {
        Dispatcher.forwardEvent(StateEvents.ShowCreator);
    }

    private void onGroupsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        setBusy(false);
        this.store.removeAll();
    }

    private void onGroupsUpdated(AppEvent event) {
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        setBusy(false);
        this.store.removeAll();
        this.store.add(groups, true);
    }

    private void confirmRemove() {
        MessageBox.confirm(null, "Are you sure you want to remove this state sensor?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            removeService();
                        }
                    }
                });
    }

    protected void removeService() {
        TreeModel sensor = this.grid.getSelectionModel().getSelectedItem();
        TreeModel service = sensor.getParent();
        AppEvent event = new AppEvent(StateEvents.RemoveRequested);
        event.setData("sensorId", sensor.<String> get("id"));
        event.setData("serviceId", service.<String> get("id"));
        Dispatcher.forwardEvent(event);
        setBusy(true);
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
            
            Dispatcher.forwardEvent(StateEvents.ListRequested);
        } else {
            Log.e(TAG, "Failed to show states panel: parent=null");
        }
    }

    private void setBusy(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
