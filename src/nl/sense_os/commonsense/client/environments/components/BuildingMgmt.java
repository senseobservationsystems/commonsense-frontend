package nl.sense_os.commonsense.client.environments.components;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.grid.LoadingPanel;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.building.BuildingModel;

public class BuildingMgmt extends ContentPanel {

    private static final String TAG = BuildingMgmt.class.getName();
    private Grid<BuildingModel> buildingGrid;
    private final ListStore<BuildingModel> buildingStore = new ListStore<BuildingModel>();
    private Component centerComponent;
    private BuildingCreator creator;
    private BuildingDetails details;
    private BuildingEditor editor;
    private BuildingModel lastSelected;
    private final BuildingServiceAsync buildingService;
    
    public BuildingMgmt() {

        buildingService = Registry.<BuildingServiceAsync> get(Constants.REG_BUILDING_SVC);
        
        // building selection panel for west part of widget
        Component gridPanel = createSelectionPanel();
        gridPanel.setItemId("west_gridpanel");

        // tool bar
        ToolBar toolBar = createToolBar();
        this.setTopComponent(toolBar);

        // add panels to the layout
        this.setLayout(new BorderLayout());
        this.add(gridPanel, new BorderLayoutData(LayoutRegion.WEST));

        this.setHeading("Building management");
        
        getRecentBuildings();
    }

    private BuildingCreator createCreator() {
        final BuildingCreator creator = new BuildingCreator();
        creator.addListener(Events.Complete, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                BuildingModel b = creator.getCreatedBuilding();
                buildingStore.add(b);
                buildingStore.sort(BuildingModel.KEY_NAME, SortDir.ASC);
                buildingGrid.getSelectionModel().select(false, b);
            }
        });
        creator.addListener(Events.CancelEdit, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                // show the last selected building before the creator was shown
                buildingGrid.getSelectionModel().select(lastSelected, false);
            }
        });
        return creator;
    }

    private BuildingDetails createDetails() {
        final BuildingDetails details = new BuildingDetails();
        details.addListener(Events.BeforeRemove, new Listener<BoxComponentEvent>() {
            @Override
            public void handleEvent(BoxComponentEvent be) {
                buildingStore.remove(details.getBuilding());
                if (buildingStore.getCount() > 0) {
                    buildingGrid.getSelectionModel().select(0, false);
                } else {
                    showCreator();
                }
            }
        });
        details.addListener(Events.BeforeEdit, new Listener<BoxComponentEvent>() {
            @Override
            public void handleEvent(BoxComponentEvent be) {
                showEditor(details.getBuilding());
            }
        });
        return details;
    }

    private BuildingEditor createEditor() {
        final BuildingEditor editor = new BuildingEditor();
        editor.addListener(Events.Complete, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {

                // add created building to the list
                BuildingModel b = editor.getBuilding();
                buildingStore.update(b);

                showDetails(editor.getBuilding());
            }
        });
        editor.addListener(Events.CancelEdit, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                // show the last selected building before the creator was shown
                showDetails(editor.getBuilding());
            }
        });
        return editor;
    }

    private Component createSelectionPanel() {

        buildingStore.setDefaultSort(BuildingModel.KEY_NAME, SortDir.ASC);

        // simple ColumnConfig to show the building name
        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        ColumnConfig labelCol = new ColumnConfig(BuildingModel.KEY_NAME, "Building", 198);
        columns.add(labelCol);
        ColumnModel buildingCols = new ColumnModel(columns);

        // create the Grid
        buildingGrid = new Grid<BuildingModel>(buildingStore, buildingCols);
        buildingGrid.setAutoExpandColumn(BuildingModel.KEY_NAME);
        buildingGrid.setAutoHeight(true);
        buildingGrid.setHideHeaders(true);

        GridSelectionModel<BuildingModel> sm = new GridSelectionModel<BuildingModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        sm.addSelectionChangedListener(new SelectionChangedListener<BuildingModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<BuildingModel> se) {
                final BuildingModel b = se.getSelectedItem();
                if (null != b) {
                    showDetails(b);
                }
            }
        });
        buildingGrid.setSelectionModel(sm);

        // ContentPanel to wrap the grid
        ContentPanel gridPanel = new ContentPanel();
        gridPanel.setHeaderVisible(false);
        gridPanel.setScrollMode(Scroll.AUTOY);
        gridPanel.add(buildingGrid);

        return gridPanel;
    }

    private ToolBar createToolBar() {
        final ToolBar bar = new ToolBar();
        final Button newBld = new Button("Add building", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                showCreator();
            }
        });
        bar.add(newBld);

        return bar;
    }

    private void getRecentBuildings() {

        // show loading message
        showLoading();

        // do request
        UserModel user = Registry.get(Constants.REG_USER);
        if (null == user) {
            Log.e(TAG, "No user object in Registry");
            return;
        }
        buildingService.getUserBuildings("" + user.getId(), new AsyncCallback<List<BuildingModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                removeAll();
                final Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {
                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        getRecentBuildings();
                    }
                };
                MessageBox.alert("CommonSense Web Application",
                        "Failure getting building data:\n" + caught.getMessage() + ".\nRetry?", l);
            }

            @Override
            public void onSuccess(List<BuildingModel> buildings) {
                
                showSelectionGrid();
                
                // update building grid
                buildingStore.removeAll();
                buildingStore.add(buildings);

                // select the first model
                if (buildings.size() > 0) {
                    buildingGrid.getSelectionModel().select(0, false);
                } else {
                    showCreator();
                }
            }
        });
    }

    private void showCreator() {

        if ((null == creator) || (centerComponent != creator)) {

            buildingGrid.getSelectionModel().deselectAll();

            // remove whatever is currently in the center panel
            if (null != centerComponent) {
                remove(centerComponent);
            }

            // lazy create the building creator
            if (null == creator) {
                creator = createCreator();
            }

            // put creator in the center panel
            centerComponent = creator;
            add(creator, new BorderLayoutData(LayoutRegion.CENTER));
            layout();
        }
    }

    private void showEditor(BuildingModel building) {

        if ((null == editor) || (centerComponent != editor)) {

            // remove whatever is currently in the center panel
            if (null != centerComponent) {
                remove(centerComponent);
            }

            // lazy create the BuildingDetails widget
            if (null == editor) {
                editor = createEditor();
            }

            // set the correct building
            editor.setBuilding(building);

            // put details in center panel
            centerComponent = editor;
            add(editor, new BorderLayoutData(LayoutRegion.CENTER));
            layout();
        }
    }

    private void showDetails(final BuildingModel building) {

        lastSelected = building;

        if ((null == details) || (centerComponent != details)) {
            // remove whatever is currently in the center panel
            if (null != centerComponent) {
                remove(centerComponent);
            }

            // lazy create the BuildingDetails widget
            if (null == details) {
                details = createDetails();
            }

            // set the correct building
            details.setBuilding(building);

            // put details in center panel
            centerComponent = details;
            add(details, new BorderLayoutData(LayoutRegion.CENTER));
            layout();
        } else {
            // set the correct building
            details.setBuilding(building);
        }
    }

    private void showLoading() {
        Component gridPanel = getItemByItemId("west_gridpanel");
        if (null != gridPanel) {
            remove(gridPanel);
        }
        Component oldLoading = getItemByItemId("west_loading");
        if (null != oldLoading) {
            // loading is already shown
            return;
        }
        
        LoadingPanel loading = new LoadingPanel();
        loading.setTitle("Sense");
        loading.setItemId("west_loading");
        
        add(loading, new BorderLayoutData(LayoutRegion.WEST));
    }
    
    private void showSelectionGrid() {

        Component loading = getItemByItemId("west_loading");
        if (null != loading) {
            remove(loading);
        }
        Component oldGrid = getItemByItemId("west_gridpanel");
        if (null != oldGrid) {
            // gridpanel is already shown
            return;
        }
        
        // building selection panel for west part of widget
        Component gridPanel = createSelectionPanel();
        gridPanel.setItemId("west_gridpanel");
        
        add(gridPanel, new BorderLayoutData(LayoutRegion.WEST));
    }
}
