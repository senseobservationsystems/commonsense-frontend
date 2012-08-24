package nl.sense_os.commonsense.main.client.env.list;

import java.util.Arrays;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.main.client.env.view.EnvViewEvents;
import nl.sense_os.commonsense.main.client.gxt.model.GxtEnvironment;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
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

public class EnvGrid extends View {

	private static final Logger LOG = Logger.getLogger(EnvGrid.class.getName());
	private Button createButton;
	private Button deleteButton;
	private Button editButton;
	private Button viewButton;
	private Grid<GxtEnvironment> grid;
	private ContentPanel panel;
	private ListStore<GxtEnvironment> store;
	private ListLoader<ListLoadResult<GxtEnvironment>> loader;
	private boolean isListDirty;
	private ToolButton refreshTool;
	private ToolBar toolBar;

	public EnvGrid(Controller controller) {
		super(controller);
	}

	private void createEnvironment() {
		Dispatcher.forwardEvent(EnvCreateEvents.ShowCreator);
	}

	private void deleteEnvironment() {
		AppEvent delete = new AppEvent(EnvEvents.DeleteRequest);
		delete.setData("environment", grid.getSelectionModel().getSelectedItem());
		fireEvent(delete);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type.equals(EnvEvents.ShowGrid)) {
			// LOG.fine( "ShowGrid");
			final LayoutContainer parent = event.getData("parent");
			showPanel(parent);

		} else if (type.equals(EnvEvents.ListUpdated)) {
			// LOG.fine( "ListUpdated");
			onListUpdated(event);

		} else if (type.equals(VizEvents.Show)) {
			// LOG.fine( "Show Visualization");
			refreshLoader(false);

		} else if (type.equals(EnvEvents.Working)) {
			// LOG.fine( "Working");
			setBusyIcon(true);

		} else if (type.equals(EnvEvents.Done)) {
			// LOG.fine( "Working");
			setBusyIcon(false);

		} else if (type.equals(EnvCreateEvents.CreateSuccess)
				|| type.equals(EnvEvents.DeleteSuccess)) {
			// LOG.fine( "Done");
			isListDirty = true;
			refreshLoader(false);

		} else {
			LOG.severe("Unexpected event type: " + type);
		}
	}

	private void initGrid() {
		// tree store
		DataProxy<ListLoadResult<GxtEnvironment>> proxy = new DataProxy<ListLoadResult<GxtEnvironment>>() {

			@Override
			public void load(DataReader<ListLoadResult<GxtEnvironment>> reader, Object loadConfig,
					AsyncCallback<ListLoadResult<GxtEnvironment>> callback) {

				// only load when the panel is not collapsed
				if (loadConfig instanceof ListLoadConfig) {
					fireEvent(new AppEvent(EnvEvents.ListRequested, callback));
				} else {
					LOG.warning("Unexpected loadconfig: " + loadConfig);
					callback.onFailure(null);
				}
			}
		};
		loader = new BaseListLoader<ListLoadResult<GxtEnvironment>>(proxy);
		store = new ListStore<GxtEnvironment>(loader);

		ColumnConfig name = new ColumnConfig(GxtEnvironment.NAME, "Name", 100);
		ColumnConfig floors = new ColumnConfig(GxtEnvironment.FLOORS, "Floors", 100);
		ColumnConfig id = new ColumnConfig(GxtEnvironment.ID, "ID", 50);
		id.setHidden(true);
		ColumnConfig outline = new ColumnConfig(GxtEnvironment.OUTLINE, "Outline", 200);
		outline.setRenderer(new GridCellRenderer<GxtEnvironment>() {

			@Override
			public Object render(GxtEnvironment model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<GxtEnvironment> store,
					Grid<GxtEnvironment> grid) {
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
		ColumnConfig position = new ColumnConfig(GxtEnvironment.POSITION, "Position", 100);
		position.setRenderer(new GridCellRenderer<GxtEnvironment>() {

			@Override
			public Object render(GxtEnvironment model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<GxtEnvironment> store,
					Grid<GxtEnvironment> grid) {
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

		grid = new Grid<GxtEnvironment>(store, cm);
		grid.setId("buildingGrid");
		grid.setStateful(true);
		grid.setLoadMask(true);
		grid.setAutoExpandColumn(GxtEnvironment.NAME);
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

	@Override
	protected void initialize() {
		super.initialize();

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
	}

	private void initToolBar() {

		final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Button source = ce.getButton();
				if (source.equals(createButton)) {
					createEnvironment();
				} else if (source.equals(deleteButton)) {
					onDeleteClick();
				} else if (source.equals(viewButton)) {
					onViewClick();
				} else if (source.equals(editButton)) {
					onEditClick();
				} else {
					LOG.warning("Unexpected buttons pressed");
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
		GridSelectionModel<GxtEnvironment> selectionModel = new GridSelectionModel<GxtEnvironment>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<GxtEnvironment>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<GxtEnvironment> se) {
				GxtEnvironment selection = se.getSelectedItem();
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

	private void onDeleteClick() {
		MessageBox.confirm(null, "Are you sure you want to remove this environment?",
				new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button clicked = be.getButtonClicked();
						if ("yes".equalsIgnoreCase(clicked.getText())) {
							deleteEnvironment();
						}
					}
				});
	}

	protected void onEditClick() {
		// TODO Auto-generated method stub

	}

	private void onListUpdated(AppEvent event) {
		isListDirty = false;
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

	private void setBusyIcon(boolean busy) {
		if (busy) {
			panel.getHeader().setIconStyle("sense-btn-icon-loading");
		} else {
			panel.getHeader().setIconStyle("");
		}
	}

	private void showPanel(LayoutContainer parent) {
		if (null != parent) {
			parent.add(panel);
			parent.layout();
		} else {
			LOG.severe("Failed to show buildings panel: parent=null");
		}
	}
}
