package nl.sense_os.commonsense.main.client.groupmanagement.component;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.groupmanagement.GroupListView;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.gxt.util.SenseIconProvider;
import nl.sense_os.commonsense.main.client.gxt.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.gxt.util.SensorComparator;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GxtGroupGrid extends Composite implements GroupListView {

	private static final Logger LOG = Logger.getLogger(GxtGroupGrid.class.getName());
	private Button createButton;
	private TreeGrid<GxtUser> grid;
	private Button addUserButton;
	private Button joinButton;
	private Button leaveButton;
	private ContentPanel panel;
	private TreeStore<GxtUser> store;
	private TreeLoader<GxtUser> loader;
	private ToolBar filterBar;
	private StoreFilterField<GxtUser> filter;
	private ToolBar toolBar;
	private Presenter presenter;

	public GxtGroupGrid() {
		panel = new ContentPanel(new FitLayout());
		panel.setHeading("Manage group memberships");
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
		initToolBar();

		initHeaderTool();
		panel.setTopComponent(toolBar);
		ContentPanel content = new ContentPanel(new FitLayout());
		content.setBodyBorder(false);
		content.setHeaderVisible(false);
		content.setTopComponent(filterBar);
		content.add(grid);
		panel.add(content);

		initComponent(panel);
	}

	@Override
    public List<GxtUser> getSelection() {
        return grid.getSelectionModel().getSelection();
    }

	private void initFilter() {
		filterBar = new ToolBar();
		filterBar.add(new LabelToolItem("Filter: "));
		filter = new StoreFilterField<GxtUser>() {

			@Override
			protected boolean doSelect(Store<GxtUser> store, GxtUser parent, GxtUser record,
					String property, String filter) {
				// only match leaf nodes
				if (record.getChildCount() > 0) {
					return false;
				}
				String name = record.getName() + " " + record.getSurname() + " "
						+ record.getUsername();
				name = name.toLowerCase();
				if (name.contains(filter.toLowerCase())) {
					return true;
				}
				return false;
			}

		};
		filter.bind(store);
		filterBar.add(filter);

		// TODO fix filtering
		filter.setEnabled(false);
	}

	private void initGrid() {

		// proxy
		DataProxy<List<GxtUser>> proxy = new DataProxy<List<GxtUser>>() {

			@Override
			public void load(DataReader<List<GxtUser>> reader, Object loadConfig,
					AsyncCallback<List<GxtUser>> callback) {

				if (panel.isExpanded() && null != presenter) {
					presenter.loadData(callback, loadConfig);
				} else {
					callback.onFailure(null);
				}
			}
		};

		// tree loader
		loader = new BaseTreeLoader<GxtUser>(proxy) {

			@Override
			public boolean hasChildren(GxtUser parent) {
				return parent instanceof GxtGroup;
			};
		};

		// tree store
		store = new TreeStore<GxtUser>(loader);
		store.setKeyProvider(new SenseKeyProvider<GxtUser>());
		store.setStoreSorter(new StoreSorter<GxtUser>(new SensorComparator<GxtUser>()));

		ColumnConfig id = new ColumnConfig(GxtUser.ID, "ID", 125);
		id.setRenderer(new TreeGridCellRenderer<TreeModel>());
		ColumnConfig name = new ColumnConfig(GxtUser.NAME, "Name", 200);
		ColumnConfig surname = new ColumnConfig(GxtUser.SURNAME, "Surname", 200);
		ColumnConfig description = new ColumnConfig(GxtGroup.DESCRIPTION, "Description", 200);
		ColumnConfig isPublic = new ColumnConfig(GxtGroup.PUBLIC, "Public", 75);
		isPublic.setHidden(true);
		ColumnConfig isHidden = new ColumnConfig(GxtGroup.HIDDEN, "Hidden", 75);
		isHidden.setHidden(true);
		ColumnConfig isAnon = new ColumnConfig(GxtGroup.ANONYMOUS, "Anonymous", 75);
		isAnon.setHidden(true);
		ColumnModel cm = new ColumnModel(Arrays.asList(id, name, surname, description, isPublic,
				isHidden, isAnon));

		grid = new TreeGrid<GxtUser>(store, cm);
		grid.setAutoLoad(true);
		grid.setLoadMask(true);
		grid.setId("groupGrid");
		grid.setStateful(true);
		grid.setAutoExpandColumn(GxtGroup.DESCRIPTION);
		grid.setIconProvider(new SenseIconProvider<GxtUser>());
	}

	private void initHeaderTool() {
		ToolButton refresh = new ToolButton("x-tool-refresh");
		refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

			@Override
			public void componentSelected(IconButtonEvent ce) {
				refreshLoader(true);
			}
		});

		// add to panel
		panel.getHeader().addTool(refresh);
	}

	private void initToolBar() {

		final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Button source = ce.getButton();
				if (source.equals(createButton)) {
                    if (null != presenter) {
                        presenter.onCreateClick();
                    }
				} else if (source.equals(leaveButton)) {
                    if (null != presenter) {
                        presenter.onLeaveClick();
                    }
				} else if (source.equals(joinButton)) {
                    if (null != presenter) {
                        presenter.onJoinClick();
                    }
				} else if (source.equals(addUserButton)) {
                    if (null != presenter) {
                        presenter.onAddUserClick();
                    }
				} else {
					LOG.warning("Unexpected button pressed: " + source);
				}
			}
		};

		createButton = new Button("Create", l);

		joinButton = new Button("Join", l);

		leaveButton = new Button("Leave", l);
		leaveButton.disable();

		addUserButton = new Button("Add User", l);
		addUserButton.disable();

		// handle selections
		TreeGridSelectionModel<GxtUser> selectionModel = new TreeGridSelectionModel<GxtUser>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<GxtUser>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<GxtUser> se) {
				GxtUser selection = se.getSelectedItem();
				if (null != selection) {
					leaveButton.enable();
					addUserButton.enable();
				} else {
					leaveButton.disable();
					addUserButton.disable();
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		// create tool bar
		toolBar = new ToolBar();
		toolBar.add(joinButton);
		toolBar.add(createButton);
		toolBar.add(addUserButton);
		toolBar.add(leaveButton);
	}

	@Override
	public void onLibChanged() {
		onListDirty();
	}

	private void onListDirty() {
		new Timer() {

			@Override
			public void run() {
				refreshLoader(true);
			}
		}.schedule(100);
	}

	@Override
	public void onListUpdate() {
		onListDirty();
	}

	private void refreshLoader(boolean force) {
		if (force || store.getChildCount() == 0) {
			loader.load();
		}
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
}
