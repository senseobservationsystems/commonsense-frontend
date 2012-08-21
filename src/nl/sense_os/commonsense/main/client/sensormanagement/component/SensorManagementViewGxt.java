package nl.sense_os.commonsense.main.client.sensormanagement.component;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.ext.util.SensorOwnerFilter;
import nl.sense_os.commonsense.main.client.ext.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.ext.util.SensorTextFilter;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementView;
import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryGrid;
import nl.sense_os.commonsense.main.client.sensors.library.SensorGroupRenderer;
import nl.sense_os.commonsense.main.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.main.client.sensors.unshare.UnshareEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SensorManagementViewGxt extends Composite implements SensorManagementView {

	private static final Logger LOG = Logger.getLogger(LibraryGrid.class.getName());

	private ContentPanel panel;
	private BaseListLoader<ListLoadResult<ExtSensor>> loader;
	private GroupingStore<ExtSensor> store;
	private Grid<ExtSensor> grid;
	private ToolBar toolBar;
	private Button shareButton;
	private Button unshareButton;
	private Button removeButton;
	private Button alertButton;
	private Button vizButton;
	private ToolBar filterBar;
	private boolean forceRefresh = true;
	private Presenter presenter;

	public SensorManagementViewGxt() {

		panel = new ContentPanel(new FitLayout());
		panel.setHeading("Sensor library");
		panel.setAnimCollapse(false);

		// track whether the panel is expanded
		panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				refreshLoader(false);
			}
		});

		addAttachHandler(new Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				LOG.finest("AttachEvent");
				refreshLoader(false);
			}
		});

		forceRefresh = true;

		initGrid();
		initFilters();
		initToolBar();
		initHeaderTool();

		// do layout
		panel.setTopComponent(toolBar);
		ContentPanel content = new ContentPanel(new FitLayout());
		content.setBodyBorder(false);
		content.setHeaderVisible(false);
		content.setTopComponent(filterBar);
		content.add(grid);
		panel.add(content);

		setupDragDrop();

		initComponent(panel);
	}

	/**
	 * Initializes filter toolbar for the grid with sensors. The bar contains text filter and an
	 * owner filter.
	 */
	private void initFilters() {

		// text filter
		SensorTextFilter<ExtSensor> textFilter = new SensorTextFilter<ExtSensor>();
		textFilter.bind(store);

		// filter to show only my own sensors
		final SensorOwnerFilter<ExtSensor> ownerFilter = new SensorOwnerFilter<ExtSensor>();
		store.addFilter(ownerFilter);

		// checkbox to toggle filter
		final CheckBox filterOnlyMe = new CheckBox();
		filterOnlyMe.setBoxLabel("Only my own sensors");
		filterOnlyMe.setHideLabel(true);
		filterOnlyMe.addListener(Events.Change, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {

				ownerFilter.setEnabled(filterOnlyMe.getValue());
				store.applyFilters(null);
			}
		});

		// add filters to filter bar
		filterBar = new ToolBar();
		filterBar.add(new LabelToolItem("Filter: "));
		filterBar.add(textFilter);
		filterBar.add(new SeparatorToolItem());
		filterBar.add(filterOnlyMe);
	}

	private void initGrid() {

		// proxy
		DataProxy<ListLoadResult<ExtSensor>> proxy = new DataProxy<ListLoadResult<ExtSensor>>() {

			@Override
			public void load(DataReader<ListLoadResult<ExtSensor>> reader, Object loadConfig,
					AsyncCallback<ListLoadResult<ExtSensor>> callback) {
				// only load when the panel is not collapsed
				if (panel.isExpanded()) {
					if (loadConfig instanceof ListLoadConfig) {
						// LOG.fine( "Load library... Renew cache: " + forceRefresh);
						if (null != presenter) {
							presenter.loadData(callback, forceRefresh);
						}
						forceRefresh = false;
					} else {
						LOG.warning("Unexpected load config: " + loadConfig);
						callback.onFailure(null);
					}
				} else {
					LOG.warning("failed to load data: panel is not expanded...");
					callback.onFailure(null);
				}
			}
		};

		// list loader
		loader = new BaseListLoader<ListLoadResult<ExtSensor>>(proxy);

		// list store
		store = new GroupingStore<ExtSensor>(loader);
		store.setKeyProvider(new SenseKeyProvider<ExtSensor>());
		store.setMonitorChanges(true);

		// this.store.groupBy(SensorModel.DEVICE_TYPE, true);
		store.sort(ExtSensor.DISPLAY_NAME, SortDir.ASC);
		store.setDefaultSort(ExtSensor.DISPLAY_NAME, SortDir.ASC);

		// Column model
		ColumnModel cm = LibraryColumnsFactory.create();

		// grouping view for the grid
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(true);
		view.setForceFit(true);
		view.setGroupRenderer(new SensorGroupRenderer(cm));
		view.setStartCollapsed(true);

		grid = new Grid<ExtSensor>(store, cm);
		grid.setModelProcessor(new SensorProcessor<ExtSensor>());
		grid.setView(view);
		grid.setBorders(false);
		grid.setId("library-grid");
		grid.setStateful(true);
		grid.setLoadMask(true);
	}

	private void initHeaderTool() {
		ToolButton refresh = new ToolButton("x-tool-refresh");
		refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

			@Override
			public void componentSelected(IconButtonEvent ce) {
				refreshLoader(true);
			}
		});
		panel.getHeader().addTool(refresh);
	}

	private void initToolBar() {

		// listen to toolbar button clicks
		final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Button source = ce.getButton();
				if (source.equals(vizButton)) {
					onVizClick();
				} else if (source.equals(shareButton)) {
					onShareClick();
				} else if (source.equals(unshareButton)) {
					onUnshareClick();
				} else if (source.equals(removeButton)) {
					onRemoveClick();
				} else if (source.equals(alertButton)) {
					onAlertClick();
				} else {
					LOG.warning("Unexpected button pressed");
				}
			}
		};

		// initialize the buttons
		vizButton = new Button("Visualize", l);
		vizButton.disable();

		shareButton = new Button("Share", l);
		shareButton.disable();

		unshareButton = new Button("Unshare", l);
		unshareButton.disable();

		removeButton = new Button("Remove", l);
		removeButton.disable();

		alertButton = new Button("Alert", l);
		alertButton.disable();

		// listen to selection of tree items to enable/disable buttons
		GridSelectionModel<ExtSensor> selectionModel = new GridSelectionModel<ExtSensor>();
		selectionModel.setSelectionMode(SelectionMode.MULTI);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<ExtSensor>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<ExtSensor> se) {
				List<ExtSensor> selection = se.getSelection();
				if (selection != null && selection.size() > 0) {
					vizButton.enable();
					shareButton.enable();
					if (selection.size() == 1 && selection.get(0).getUsers() != null) {
						alertButton.enable();
					}
					if (selection.size() == 1 && selection.get(0).getUsers() != null
							&& selection.get(0).getUsers().size() > 0) {
						unshareButton.enable();
					} else {
						unshareButton.disable();
					}
					removeButton.enable();
				} else {
					vizButton.disable();
					shareButton.disable();
					unshareButton.disable();
					removeButton.disable();
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		// create tool bar
		toolBar = new ToolBar();
		toolBar.add(vizButton);
		toolBar.add(shareButton);
		toolBar.add(unshareButton);
		toolBar.add(removeButton);
		toolBar.add(alertButton);
	}

	private void onAlertClick() {
		// get sensor models from the selection
		final List<ExtSensor> sensors = grid.getSelectionModel().getSelection();

		if (sensors.size() > 0) {
			// AppEvent event = new AppEvent(AlertCreateEvents.ShowCreator);
			// event.setData("sensor", sensors.get(0));
			// Dispatcher.forwardEvent(event);

		} else {
			MessageBox.info(null, "No sensors selected. You can only create alerts for sensors!",
					null);
		}
	}

	public void onLibChanged() {
		refreshLoader(false);
	}

	@Override
	public void onListUpdate() {
		// re-filter the sensors store
		store.clearFilters();
		store.applyFilters(null);
	}

	private void onRemoveClick() {
		// get sensor models from the selection
		final List<ExtSensor> sensors = grid.getSelectionModel().getSelection();

		if (sensors.size() > 0) {
			AppEvent event = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
			event.setData("sensors", sensors);
			Dispatcher.forwardEvent(event);

		} else {
			MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
		}
	}

	private void onShareClick() {
		List<ExtSensor> sensors = grid.getSelectionModel().getSelection();
		AppEvent shareEvent = new AppEvent(SensorShareEvents.ShowShareDialog);
		shareEvent.setData("sensors", sensors);
		Dispatcher.forwardEvent(shareEvent);
	}

	private void onUnshareClick() {
		AppEvent shareEvent = new AppEvent(UnshareEvents.ShowUnshareDialog);
		shareEvent.setData("sensor", grid.getSelectionModel().getSelectedItem());
		Dispatcher.forwardEvent(shareEvent);
	}

	private void onVizClick() {
		List<ExtSensor> selection = grid.getSelectionModel().getSelection();
		Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
	}

	@Override
	public void refreshLoader(boolean force) {
		this.forceRefresh = this.forceRefresh || force;
		loader.load();
	}

	@Override
	public void setBusy(boolean busy) {
		if (busy) {
			panel.getHeader().setIconStyle("sense-btn-icon-loading");
		} else {
			panel.getHeader().setIconStyle("");
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	/**
	 * Sets up the grid for drag and drop of the sensors.
	 */
	private void setupDragDrop() {
		new GridDragSource(grid);
	}
}
