package nl.sense_os.commonsense.main.client.states.list;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.util.SenseIconProvider;
import nl.sense_os.commonsense.main.client.ext.util.SensorComparator;
import nl.sense_os.commonsense.main.client.ext.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.states.connect.StateConnectEvents;
import nl.sense_os.commonsense.main.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.main.client.states.edit.StateEditEvents;
import nl.sense_os.commonsense.main.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreeGridDragSource;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuBar;
import com.extjs.gxt.ui.client.widget.menu.MenuBarItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateGrid extends View {

	private static final Logger LOG = Logger.getLogger(StateGrid.class.getName());
	private ContentPanel panel;
	private TreeGrid<ExtSensor> grid;
	private TreeStore<ExtSensor> store;
	private TreeLoader<ExtSensor> loader;
	private boolean isListDirty = false;
	private MenuItem createButton;
	private MenuItem deleteButton;
	private MenuItem disconnectButton;
	private MenuItem connectButton;
	private MenuItem editButton;
	private MenuItem feedbackButton;
	private MenuItem defaultsButton;
	private ToolBar filterBar;
	private ToolButton refreshButton;
	private MenuBar toolBar;
	private StoreFilterField<ExtSensor> filter;

	public StateGrid(Controller controller) {
		super(controller);
		// LOG.setLevel(Level.ALL);
	}

	private void checkDefaultStates() {
		Dispatcher.forwardEvent(StateDefaultsEvents.CheckDefaults);
	}

	private void confirmDisconnect() {
		MessageBox.confirm(null,
				"Are you sure you want to disconnect this sensor from this state?",
				new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getHtml())) {
							disconnectSensor();
						}
					}
				});
	}

	/**
	 * Dispatches request to show "delete dialog" for the selected state.
	 */
	private void deleteState() {
		ExtSensor state = getSelectedState();
		AppEvent delete = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
		delete.setData("sensors", Arrays.asList(state));
		Dispatcher.forwardEvent(delete);
	}

	private void disconnectSensor() {
		ExtSensor sensor = grid.getSelectionModel().getSelectedItem();
		ExtSensor stateSensor = store.getParent(sensor);

		AppEvent event = new AppEvent(StateListEvents.RemoveRequested);
		event.setData("sensor", sensor);
		event.setData("stateSensor", stateSensor);
		Dispatcher.forwardEvent(event);
		setBusy(true);
	}

	private ExtSensor getSelectedState() {
		ExtSensor selection = grid.getSelectionModel().getSelectedItem();
		while (store.getParent(selection) instanceof ExtSensor) {
			selection = store.getParent(selection);
		}
		LOG.finest("Selected state: " + selection);
		return selection;
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type.equals(StateListEvents.ShowGrid)) {
			// LOG.fine( "ShowGrid");
			final LayoutContainer parent = event.getData("parent");
			showPanel(parent);

		} else if (type.equals(VizEvents.Show)) {
			// LOG.fine( "Show Visualization");
			refreshLoader(false);

		} else if (type.equals(StateListEvents.Done)) {
			// LOG.fine( "TreeUpdated");
			setBusy(false);

		} else if (type.equals(StateListEvents.Working)) {
			// LOG.fine( "Working");
			setBusy(true);

		} else if (type.equals(StateListEvents.LoadComplete)) {
			// LOG.fine( "TreeUpdated");
			onLoadComplete();

		} else if (type.equals(StateListEvents.RemoveComplete)) {
			// LOG.fine( "RemoveComplete");
			onRemoveComplete(event);

		} else if (type.equals(StateListEvents.RemoveFailed)) {
			LOG.warning("RemoveFailed");
			onRemoveFailed(event);

		} else if (type.equals(StateConnectEvents.ConnectSuccess)
				|| type.equals(StateCreateEvents.CreateServiceComplete)
				|| type.equals(StateDefaultsEvents.CheckDefaultsSuccess)
				|| type.equals(SensorDeleteEvents.DeleteSuccess)) {
			// LOG.fine( "External trigger for update");
			refreshLoader(true);

		} else {
			LOG.severe("Unexpected event type: " + type);
		}
	}

	private void initFilter() {
		filterBar = new ToolBar();
		filterBar.add(new LabelToolItem("Filter: "));
		filter = new StoreFilterField<ExtSensor>() {

			@Override
			protected boolean doSelect(Store<ExtSensor> store, ExtSensor parent, ExtSensor record,
					String property, String filter) {
				filter = filter.toLowerCase();
				if (record.getName().toLowerCase().contains(filter)) {
					return true;
				} else if (record.getDescription().toLowerCase().contains(filter)) {
					return true;
				} else if (record.getDevice() != null
						&& record.getDevice().getType().toLowerCase().contains(filter)) {
					return true;
				} else if (record.getDataType().toLowerCase().contains(filter)) {
					return true;
				} else {
					return false;
				}
			}
		};
		filter.bind(store);
		filterBar.add(filter);

		// TODO fix filtering
		filter.setEnabled(false);
	}

	private void initGrid() {

		// proxy
		DataProxy<List<ExtSensor>> proxy = new DataProxy<List<ExtSensor>>() {

			@Override
			public void load(DataReader<List<ExtSensor>> reader, Object loadConfig,
					AsyncCallback<List<ExtSensor>> callback) {

				if (panel.isExpanded()) {
					AppEvent request = new AppEvent(StateListEvents.LoadRequest);
					request.setData("loadConfig", loadConfig);
					request.setData("callback", callback);
					Dispatcher.forwardEvent(request);
				} else {
					callback.onFailure(null);
				}
			}
		};

		// tree loader
		loader = new BaseTreeLoader<ExtSensor>(proxy) {

			@Override
			public boolean hasChildren(ExtSensor parent) {
				// only state sensors have children
				return parent.getType() == 2;
			};
		};

		// tree store
		store = new TreeStore<ExtSensor>(loader);
		store.setStoreSorter(new StoreSorter<ExtSensor>(new SensorComparator<ExtSensor>()));

		// column model, make sure you add a TreeGridCellRenderer
		List<ColumnConfig> columns = LibraryColumnsFactory.create().getColumns();
		ColumnModel cm = new ColumnModel(columns);
		ColumnConfig type = cm.getColumnById(ExtSensor.TYPE);
		if (type != null) {
			type.setRenderer(new TreeGridCellRenderer<ExtSensor>() {

				@Override
				protected String getText(TreeGrid<ExtSensor> grid, ExtSensor model,
						String property, int rowIndex, int colIndex) {
					// type text is always empty, use SenseIconProvider to differentiate
					return "";
				}
			});
			type.setWidth(65);
		}

		grid = new TreeGrid<ExtSensor>(store, cm);
		grid.setModelProcessor(new SensorProcessor<ExtSensor>());
		grid.setId("stateGrid");
		grid.setStateful(true);
		grid.setAutoLoad(true);
		grid.setAutoExpandColumn(ExtSensor.ENVIRONMENT_NAME);
		grid.setIconProvider(new SenseIconProvider<ExtSensor>());
	}

	private void initHeaderTool() {
		refreshButton = new ToolButton("x-tool-refresh");
		refreshButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {

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
        panel.setHeadingText("Manage states");
		panel.setAnimCollapse(false);

		// track whether the panel is expanded
		panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				refreshLoader(false);
			}
		});

		initGrid();
		initFilter();
		initHeaderTool();
		initToolBar();

		// do layout
		panel.getHeader().addTool(refreshButton);
		panel.setTopComponent(toolBar);
		ContentPanel content = new ContentPanel(new FitLayout());
		content.setBodyBorder(false);
		content.setHeaderVisible(false);
		content.setTopComponent(filterBar);
		content.add(grid);
		panel.add(content);

		setupDragDrop();
	}

	private void initToolBar() {
		TreeGridSelectionModel<ExtSensor> selectionModel = new TreeGridSelectionModel<ExtSensor>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<ExtSensor>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<ExtSensor> se) {
				ExtSensor selection = se.getSelectedItem();
				if (null != selection) {
					deleteButton.enable();
					editButton.enable();
					connectButton.enable();

					// only able to disconnect if sensor is selected
					TreeModel parent = selection.getParent();
					if (parent != null) {
						disconnectButton.enable();
					} else {
						disconnectButton.disable();
					}

					// only able to give feedback if state has manualLearn method
					ExtSensor state = getSelectedState();
					List<ModelData> methods = state.get("methods");
					boolean canHazFeedback = true; // assume we can give feedback until proven wrong
					if (null != methods) {
						canHazFeedback = false;
						for (ModelData method : methods) {
							if (method.get("name").equals("GetManualInputMode")) {
								canHazFeedback = true;
								break;
							}
						}
					}
					feedbackButton.setEnabled(canHazFeedback);

				} else {
					editButton.enable();
					feedbackButton.enable();
					deleteButton.disable();
					connectButton.disable();
					disconnectButton.disable();
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		final SelectionListener<MenuEvent> l = new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent me) {
				MenuItem source = (MenuItem) me.getItem();
				if (source.equals(createButton)) {
					onCreateClick();
				} else if (source.equals(deleteButton)) {
					deleteState();
				} else if (source.equals(editButton)) {
					onEditClick();
				} else if (source.equals(connectButton)) {
					onConnectClick();
				} else if (source.equals(disconnectButton)) {
					confirmDisconnect();
				} else if (source.equals(feedbackButton)) {
					showFeedback();
				} else if (source.equals(defaultsButton)) {
					checkDefaultStates();
				} else {
					LOG.warning("Unexpected button clicked");
				}
			}
		};

		// menu item for editing service stuff
		Menu serviceMenu = new Menu();

		createButton = new MenuItem("New State", l);
		serviceMenu.add(createButton);

		defaultsButton = new MenuItem("Default States", l);
		serviceMenu.add(defaultsButton);

		SeparatorMenuItem separator = new SeparatorMenuItem();
		serviceMenu.add(separator);

		deleteButton = new MenuItem("Delete State", l);
		deleteButton.disable();
		serviceMenu.add(deleteButton);

		SeparatorMenuItem separator2 = new SeparatorMenuItem();
		serviceMenu.add(separator2);

		editButton = new MenuItem("Algorithm Parameters", l);
		editButton.disable();
		serviceMenu.add(editButton);

		feedbackButton = new MenuItem("Give Algorithm Feedback", l);
		feedbackButton.disable();
		serviceMenu.add(feedbackButton);

		// menu item for editing sensor stuff
		Menu sensorsMenu = new Menu();

		connectButton = new MenuItem("Connect Sensor", l);
		connectButton.disable();
		sensorsMenu.add(connectButton);

		disconnectButton = new MenuItem("Disconnect Sensor", l);
		disconnectButton.disable();
		sensorsMenu.add(disconnectButton);

		// create tool bar
		toolBar = new MenuBar();
		toolBar.add(new MenuBarItem("State", serviceMenu));
		toolBar.add(new MenuBarItem("Sensors", sensorsMenu));

		// add to panel
		panel.setTopComponent(toolBar);
	}

	private void onConnectClick() {
		ExtSensor selectedService = getSelectedState();
		Dispatcher.forwardEvent(StateConnectEvents.ShowSensorConnecter, selectedService);
	}

	private void onCreateClick() {
		Dispatcher.forwardEvent(StateCreateEvents.ShowCreator);
	}

	private void onEditClick() {
		ExtSensor selectedService = getSelectedState();
		AppEvent event = new AppEvent(StateEditEvents.ShowEditor);
		event.setData(selectedService);
		Dispatcher.forwardEvent(event);
	}

	private void onLoadComplete() {
		isListDirty = false;
		// this.filter.clear(); // TODO: does not work well with the tree loader
	}

	private void onRemoveComplete(AppEvent event) {
		setBusy(false);
		refreshLoader(true);
	}

	private void onRemoveFailed(AppEvent event) {
		setBusy(false);
		MessageBox.confirm(null, "Failed to disconnect sensor. Retry?",
				new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getHtml().equalsIgnoreCase("yes")) {
							disconnectSensor();
						}
					}
				});
	}

	private void refreshLoader(boolean force) {
		if (force || (store.getChildCount() == 0 || isListDirty) && panel.isExpanded()) {
			// LOG.fine( "Refresh loader...");
			loader.load();
		}
	}

	private void setBusy(boolean busy) {
		if (busy) {
			panel.getHeader().setIconStyle("sense-btn-icon-loading");
		} else {
			panel.getHeader().setIconStyle("");
		}
	}

	/**
	 * Sets up the sensor list for drag and drop.
	 */
	private void setupDragDrop() {
		new TreeGridDragSource(grid);
	}

	private void showFeedback() {
		ExtSensor state = getSelectedState();
		List<ExtSensor> sensors = store.getChildren(state);

		AppEvent event = new AppEvent(FeedbackEvents.FeedbackInit);
		event.setData("state", state);
		event.setData("sensors", sensors);
		Dispatcher.forwardEvent(event);
	}

	private void showPanel(LayoutContainer parent) {
		if (null != parent) {
			parent.add(panel);
			parent.layout();
		} else {
			LOG.severe("Failed to show states panel: parent=null");
		}
	}
}
