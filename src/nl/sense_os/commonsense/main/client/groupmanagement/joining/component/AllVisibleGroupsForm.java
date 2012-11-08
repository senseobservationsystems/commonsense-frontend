package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.component.WizardFormPanel;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.gxt.util.SenseKeyProvider;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class AllVisibleGroupsForm extends WizardFormPanel {

	private static final Logger LOG = Logger.getLogger(AllVisibleGroupsForm.class.getName());

	private Grid<GxtGroup> grid;
	private ToolBar filterBar;
	private PagingToolBar pagingBar;
	private ListStore<GxtGroup> store;
	private PagingLoader<PagingLoadResult<GxtGroup>> loader;
	private TextField<String> hiddenField;
	private GxtGroup selected;

    public AllVisibleGroupsForm(List<GxtGroup> groups) {

        // GXT group loader
        PagingModelMemoryProxy groupProxy = new PagingModelMemoryProxy(groups);
        loader = new BasePagingLoader<PagingLoadResult<GxtGroup>>(groupProxy);

		initGrid();
		initFilters();

		// handle selections
		GridSelectionModel<GxtGroup> selectionModel = new GridSelectionModel<GxtGroup>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<GxtGroup>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<GxtGroup> se) {
				// change valid state of the hidden field when a group is selected
				selected = se.getSelectedItem();
				if (null != selected) {
					LOG.finest("Group selected: " + selected.getName());
					hiddenField.setAllowBlank(true);
				} else {
					hiddenField.setAllowBlank(false);
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		ContentPanel panel = new ContentPanel(new FitLayout());
		panel.setHeaderVisible(false);
		panel.setStyleAttribute("backgroundColor", "white");
		panel.setTopComponent(filterBar);
		panel.setBottomComponent(pagingBar);
		panel.add(grid);

		AdapterField field = new AdapterField(panel);
		field.setHeight(370);
		field.setResizeWidget(true);
		field.setFieldLabel("Select the group you want to join");

		hiddenField = new TextField<String>();
		hiddenField.setAllowBlank(false);
		hiddenField.setVisible(false);

		add(hiddenField);
		add(field, new FormData("-10"));
	}

	/**
	 * Initializes filter toolbar for the grid with groups.
	 */
	private void initFilters() {

		// text filter
		StoreFilterField<GxtGroup> textFilter = new StoreFilterField<GxtGroup>() {

			@Override
			protected boolean doSelect(Store<GxtGroup> store, GxtGroup parent,
					GxtGroup record, String property, String filter) {
				String matchMe = record.getName().toLowerCase() + " "
						+ record.getDescription().toLowerCase();
				return matchMe.contains(filter.toLowerCase());
			}
		};
		textFilter.bind(store);

		// add filters to filter bar
		filterBar = new ToolBar();
		filterBar.add(new LabelToolItem("Filter: "));
		filterBar.add(textFilter);
	}

	private void initGrid() {

		// list store
		store = new ListStore<GxtGroup>(loader);
		store.setKeyProvider(new SenseKeyProvider<GxtGroup>());
		store.setMonitorChanges(true);

		pagingBar = new PagingToolBar(15);
		pagingBar.bind(loader);

		// Column model
		ColumnConfig id = new ColumnConfig(GxtUser.ID, "ID", 50);
		id.setHidden(true);
		ColumnConfig name = new ColumnConfig(GxtGroup.NAME, "Name", 125);
		ColumnConfig description = new ColumnConfig(GxtGroup.DESCRIPTION, "Description", 125);
		ColumnConfig isPublic = new ColumnConfig(GxtGroup.PUBLIC, "Public", 75);
		ColumnConfig isAnon = new ColumnConfig(GxtGroup.ANONYMOUS, "Anonymous", 75);
		ColumnModel cm = new ColumnModel(Arrays.asList(id, name, description, isPublic, isAnon));

		grid = new Grid<GxtGroup>(store, cm);
		grid.setAutoExpandColumn(GxtGroup.NAME);
		grid.setBorders(false);
		grid.setId("group-join-grid");
		grid.setStateful(true);
		grid.setLoadMask(true);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        grid.addListener(Events.Attach, new Listener<GridEvent<GxtGroup>>() {
            public void handleEvent(GridEvent<GxtGroup> be) {
                loader.load(0, 15);
            }
        });
	}

	public GxtGroup getGroup() {
		return selected;
	}
}
