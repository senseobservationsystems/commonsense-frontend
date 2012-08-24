package nl.sense_os.commonsense.main.client.environment.component;

import java.util.Arrays;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.env.view.EnvViewEvents;
import nl.sense_os.commonsense.main.client.environment.EnvironmentManagementView;
import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvironmentManagementViewGxt extends Composite implements EnvironmentManagementView {

	private static final Logger LOG = Logger
			.getLogger(EnvironmentManagementViewGxt.class.getName());
	private Button createButton;
	private Button deleteButton;
	private Button editButton;
	private Button viewButton;
	private Grid<ExtEnvironment> grid;
	private ContentPanel panel;
	private ListStore<ExtEnvironment> store;
	private ListLoader<ListLoadResult<ExtEnvironment>> loader;
	private boolean isListDirty;
	private ToolButton refreshTool;
	private ToolBar toolBar;
	private Presenter presenter;

	public EnvironmentManagementViewGxt() {

		panel = new ContentPanel(new FitLayout());
		panel.setHeading("Manage environments");
		panel.setAnimCollapse(false);

		// track whether the panel is expanded
		panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				refreshLoader(false);
			}
		});

		initGrid();
		initHeaderTool();
		initToolBar();

		// add grid to panel
		panel.setTopComponent(toolBar);
		panel.add(grid);
		panel.getHeader().addTool(refreshTool);

		initComponent(panel);
	}

	private void initGrid() {
		// tree store
		DataProxy<ListLoadResult<ExtEnvironment>> proxy = new DataProxy<ListLoadResult<ExtEnvironment>>() {

			@Override
			public void load(DataReader<ListLoadResult<ExtEnvironment>> reader, Object loadConfig,
					AsyncCallback<ListLoadResult<ExtEnvironment>> callback) {

				// only load when the panel is not collapsed
				if (panel.isExpanded() && null != presenter) {
					presenter.loadData(callback);
				}
			}
		};
		loader = new BaseListLoader<ListLoadResult<ExtEnvironment>>(proxy);
		store = new ListStore<ExtEnvironment>(loader);

		ColumnConfig name = new ColumnConfig(ExtEnvironment.NAME, "Name", 100);
		ColumnConfig floors = new ColumnConfig(ExtEnvironment.FLOORS, "Floors", 100);
		ColumnConfig id = new ColumnConfig(ExtEnvironment.ID, "ID", 50);
		id.setHidden(true);
		ColumnConfig outline = new ColumnConfig(ExtEnvironment.OUTLINE, "Outline", 200);
		outline.setRenderer(new GridCellRenderer<ExtEnvironment>() {

			@Override
			public Object render(ExtEnvironment model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<ExtEnvironment> store,
					Grid<ExtEnvironment> grid) {
				Polygon outline = model.getOutline();
				String outString = "";
				if (outline != null) {
					for (int i = 0; i < outline.getVertexCount(); i++) {
						outString += outline.getVertex(i).toUrlValue() + "; ";
					}
				}
				return outString;
			}
		});
		outline.setHidden(true);
		ColumnConfig position = new ColumnConfig(ExtEnvironment.POSITION, "Position", 100);
		position.setRenderer(new GridCellRenderer<ExtEnvironment>() {

			@Override
			public Object render(ExtEnvironment model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<ExtEnvironment> store,
					Grid<ExtEnvironment> grid) {
				LatLng position = model.getPosition();
				if (null != position) {
					return model.getPosition().toUrlValue();
				} else {
					return "";
				}
			}
		});
		position.setHidden(true);

		ColumnModel cm = new ColumnModel(Arrays.asList(id, name, floors, position, outline));

		grid = new Grid<ExtEnvironment>(store, cm);
		grid.setId("buildingGrid");
		grid.setStateful(true);
		grid.setLoadMask(true);
		grid.setAutoExpandColumn(ExtEnvironment.NAME);
	}

	private void initHeaderTool() {
		refreshTool = new ToolButton("x-tool-refresh");
		refreshTool.addSelectionListener(new SelectionListener<IconButtonEvent>() {

			@Override
			public void componentSelected(IconButtonEvent ce) {
				refreshLoader(true);
			}
		});
	}

	private void initToolBar() {

		final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Button source = ce.getButton();
				if (source.equals(createButton)) {
					presenter.onCreateClick();
				} else if (source.equals(deleteButton)) {
					ExtEnvironment selected = grid.getSelectionModel().getSelectedItem();
					presenter.onDeleteClick(selected);
				} else if (source.equals(viewButton)) {
					ExtEnvironment selected = grid.getSelectionModel().getSelectedItem();
					presenter.onViewClick(selected);
				} else if (source.equals(editButton)) {
					ExtEnvironment selected = grid.getSelectionModel().getSelectedItem();
					presenter.onEditClick(selected);
				} else {
					LOG.warning("Unexpected button pressed");
				}
			}
		};

		viewButton = new Button("View", l);
		viewButton.disable();

		createButton = new Button("Create", l);

		editButton = new Button("Edit", l);
		editButton.disable();

		deleteButton = new Button("Remove", l);
		deleteButton.disable();

		// create tool bar
		toolBar = new ToolBar();
		toolBar.add(viewButton);
		toolBar.add(createButton);
		toolBar.add(editButton);
		toolBar.add(deleteButton);

		// enable/disable buttons according to grid selection
		GridSelectionModel<ExtEnvironment> selectionModel = new GridSelectionModel<ExtEnvironment>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<ExtEnvironment>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<ExtEnvironment> se) {
				ExtEnvironment selection = se.getSelectedItem();
				if (null != selection) {
					deleteButton.enable();
					viewButton.enable();
				} else {
					deleteButton.disable();
					viewButton.disable();
				}
			}
		});
		grid.setSelectionModel(selectionModel);
	}

	protected void onViewClick() {
		AppEvent viewEvent = new AppEvent(EnvViewEvents.Show);
		viewEvent.setData("environment", grid.getSelectionModel().getSelectedItem());
		Dispatcher.forwardEvent(viewEvent);
	}

	private void refreshLoader(boolean force) {
		if (force || (store.getCount() == 0 || isListDirty) && panel.isExpanded()) {
			loader.load();
		}
	}

	@Override
	public void onListUpdate() {
		refreshLoader(true);
	}

	@Override
	public void onLibChanged() {
		isListDirty = true;
		refreshLoader(false);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setBusy(boolean busy) {
		if (busy) {
			panel.getHeader().setIconStyle("sense-btn-icon-loading");
		} else {
			panel.getHeader().setIconStyle("");
		}
	}

}
