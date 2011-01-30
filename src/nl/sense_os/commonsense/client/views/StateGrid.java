package nl.sense_os.commonsense.client.views;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
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
    private Button createButton;
    private TreeGrid<TreeModel> grid;
    private ContentPanel panel;
    private Button removeButton;
    private Button addButton;
    private Button editButton;
    private TreeStore<TreeModel> store;

    public StateGrid(Controller controller) {
        super(controller);
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
        } else if (type.equals(MainEvents.ShowVisualization)) {
            // Log.d(TAG, "ShowVisualization");
            Dispatcher.forwardEvent(StateEvents.ListRequested);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (type.equals(LoginEvents.LoggedIn)) {
            // Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
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
                if (null != selection) {
                    if (selection.get("service_name") == null) {
                        editButton.enable();
                        addButton.enable();
                        removeButton.enable();
                    } else {
                        editButton.enable();
                        addButton.enable();
                        removeButton.disable();
                    }
                } else {
                    editButton.enable();
                    addButton.disable();
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
                } else if (source.equals(editButton)) {
                    onEditClick();
                } else if (source.equals(addButton)) {
                    onAddClick();
                } else if (source.equals(removeButton)) {
                    confirmRemove();
                } else {
                    Log.w(TAG, "Unexpected button clicked");
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.editButton = new Button("Edit", l);
        this.editButton.disable();

        this.addButton = new Button("Connect sensor", l);
        this.addButton.disable();

        this.removeButton = new Button("Remove sensor", l);
        this.removeButton.disable();

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.createButton);
        toolBar.add(this.editButton);
        toolBar.add(this.addButton);
        toolBar.add(this.removeButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    protected void onAddClick() {
        TreeModel selectedService = this.grid.getSelectionModel().getSelectedItem();
        if (selectedService.get("service_name") == null) {
            selectedService = selectedService.getParent();
        }
        Dispatcher.forwardEvent(StateEvents.ShowSensorConnecter, selectedService);
    }

    protected void onCreateClick() {
        Dispatcher.forwardEvent(StateEvents.ShowCreator);
    }

    protected void onEditClick() {
        TreeModel selectedService = this.grid.getSelectionModel().getSelectedItem();
        if (selectedService.get("service_name") == null) {
            selectedService = selectedService.getParent();
        }
        AppEvent event = new AppEvent(StateEvents.ShowEditor);
        event.setData(selectedService);
        Dispatcher.forwardEvent(event);
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

    private void onLoggedIn(AppEvent event) {
        // this request fails immediately in Google Chrome (?)
        // Dispatcher.forwardEvent(StateEvents.ListRequested);
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onRemoveComplete(AppEvent event) {
        setBusy(false);
        Dispatcher.forwardEvent(StateEvents.ListRequested);
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

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();

            // Dispatcher.forwardEvent(StateEvents.ListRequested);
        } else {
            Log.e(TAG, "Failed to show states panel: parent=null");
        }
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

    private void setBusy(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
