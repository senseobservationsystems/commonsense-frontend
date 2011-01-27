package nl.sense_os.commonsense.client.mvc.views;

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

import java.util.Arrays;
import java.util.List;

public class StateGrid extends View {

    protected static final String TAG = "StateGrid";
    private ContentPanel panel;
    private TreeGrid<TreeModel> grid;
    private TreeStore<TreeModel> store;
    private Button createButton;
    private Button importButton;
    private Button deleteButton;

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
        } else if (type.equals(StateEvents.Working)) {
            Log.d(TAG, "Working");
            setBusyIcon(true);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initGrid() {
        this.store = new TreeStore<TreeModel>();

        ColumnConfig email = new ColumnConfig("id", "Id", 100);
        ColumnConfig name = new ColumnConfig("name", "Name", 100);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, email));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("stateGrid");
        this.grid.setStateful(true);

        TreeGridSelectionModel<TreeModel> selectionModel = new TreeGridSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    deleteButton.enable();
                    importButton.enable();
                } else {
                    deleteButton.disable();
                    importButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);

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

        initHeaderTool();
        initToolBar();
        initGrid();
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    onCreateClick();
                } else if (source.equals(deleteButton)) {
                    onDeleteClick();
                } else if (source.equals(importButton)) {
                    onImportClick();
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.importButton = new Button("Import", l);
        this.importButton.disable();

        this.deleteButton = new Button("Remove", l);
        this.deleteButton.disable();

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.importButton);
        toolBar.add(this.createButton);
        toolBar.add(this.deleteButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    protected void onCreateClick() {
        // TODO Auto-generated method stub
        
    }

    private void onGroupsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        setBusyIcon(false);
        this.store.removeAll();
    }

    private void onGroupsUpdated(AppEvent event) {
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        setBusyIcon(false);
        this.store.removeAll();
        this.store.add(groups, true);
    }

    private void onDeleteClick() {
        MessageBox.confirm(null, "Are you sure you want to remove this environment?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            // TODO
                        }
                    }
                });
    }

    private void onImportClick() {
        // TODO
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

    private void setBusyIcon(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
