package nl.sense_os.commonsense.main.client.sensormanagement.component;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.util.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.gxt.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.gxt.util.SensorGroupRenderer;
import nl.sense_os.commonsense.main.client.gxt.util.SensorOwnerFilter;
import nl.sense_os.commonsense.main.client.gxt.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.gxt.util.SensorTextFilter;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorListView;
import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.main.client.sensors.publish.PublishEvents;
import nl.sense_os.commonsense.main.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.main.client.sensors.unshare.UnshareEvents;

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

public class GxtSensorGrid extends Composite implements SensorListView {

    private static final Logger LOG = Logger.getLogger(GxtSensorGrid.class.getName());

	private ContentPanel panel;
	private BaseListLoader<ListLoadResult<GxtSensor>> loader;
	private GroupingStore<GxtSensor> store;
	private Grid<GxtSensor> grid;
	private ToolBar toolBar;
	private Button shareButton;
	private Button unshareButton;
	private Button removeButton;
	private Button alertButton;
    private Button publishButton;
	private Button vizButton;
	private ToolBar filterBar;
    private boolean forceRefresh;
	private Presenter presenter;

	public GxtSensorGrid() {

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
		SensorTextFilter<GxtSensor> textFilter = new SensorTextFilter<GxtSensor>();
		textFilter.bind(store);

		// filter to show only my own sensors
		final SensorOwnerFilter<GxtSensor> ownerFilter = new SensorOwnerFilter<GxtSensor>();
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
		DataProxy<ListLoadResult<GxtSensor>> proxy = new DataProxy<ListLoadResult<GxtSensor>>() {

			@Override
			public void load(DataReader<ListLoadResult<GxtSensor>> reader, Object loadConfig,
					AsyncCallback<ListLoadResult<GxtSensor>> callback) {
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
		loader = new BaseListLoader<ListLoadResult<GxtSensor>>(proxy);

		// list store
		store = new GroupingStore<GxtSensor>(loader);
		store.setKeyProvider(new SenseKeyProvider<GxtSensor>());
		store.setMonitorChanges(true);

		// this.store.groupBy(SensorModel.DEVICE_TYPE, true);
		store.sort(GxtSensor.DISPLAY_NAME, SortDir.ASC);
		store.setDefaultSort(GxtSensor.DISPLAY_NAME, SortDir.ASC);

		// Column model
		ColumnModel cm = LibraryColumnsFactory.create();

		// grouping view for the grid
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(true);
		view.setForceFit(true);
		view.setGroupRenderer(new SensorGroupRenderer(cm));
		view.setStartCollapsed(true);

		grid = new Grid<GxtSensor>(store, cm);
		grid.setModelProcessor(new SensorProcessor<GxtSensor>());
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
                } else if (source.equals(publishButton)) {
                    onPublishClick();
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

        publishButton = new Button("Publish", l);
        publishButton.disable();

		removeButton = new Button("Remove", l);
		removeButton.disable();

		alertButton = new Button("Alert", l);
		alertButton.disable();

		// listen to selection of tree items to enable/disable buttons
		GridSelectionModel<GxtSensor> selectionModel = new GridSelectionModel<GxtSensor>();
		selectionModel.setSelectionMode(SelectionMode.MULTI);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<GxtSensor>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<GxtSensor> se) {
				List<GxtSensor> selection = se.getSelection();
				if (selection != null && selection.size() > 0) {
					vizButton.enable();
					shareButton.enable();
                    publishButton.enable();
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
                    publishButton.disable();
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		// create tool bar
		toolBar = new ToolBar();
		toolBar.add(vizButton);
		toolBar.add(shareButton);
		toolBar.add(unshareButton);
        toolBar.add(publishButton);
		toolBar.add(removeButton);
		toolBar.add(alertButton);
	}

    private void onPublishClick() {
        // get sensor models from the selection
        final List<GxtSensor> sensors = grid.getSelectionModel().getSelection();

        if (sensors.size() > 0) {
            AppEvent event = new AppEvent(PublishEvents.ShowPublisher);
            event.setData("sensors", sensors);
            Dispatcher.forwardEvent(event);

        } else {
            MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
        }
    }

	private void onAlertClick() {
		// get sensor models from the selection
		final List<GxtSensor> sensors = grid.getSelectionModel().getSelection();

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
		final List<GxtSensor> sensors = grid.getSelectionModel().getSelection();

		if (sensors.size() > 0) {
			AppEvent event = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
			event.setData("sensors", sensors);
			Dispatcher.forwardEvent(event);

		} else {
			MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
		}
	}

	private void onShareClick() {
		List<GxtSensor> sensors = grid.getSelectionModel().getSelection();
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
		List<GxtSensor> selection = grid.getSelectionModel().getSelection();
		presenter.onVisualizeClick(selection);
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
