package nl.sense_os.commonsense.client.groups.join.forms;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.common.utility.SenseKeyProvider;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
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

    private Grid<GroupModel> grid;
    private ToolBar filterBar;
    private PagingToolBar pagingBar;
    private ListStore<GroupModel> store;
    private PagingLoader<PagingLoadResult<GroupModel>> loader;
    private TextField<String> hiddenField;
    private GroupModel selected;

    public AllVisibleGroupsForm(PagingLoader<PagingLoadResult<GroupModel>> loader) {
        super();

        LOG.setLevel(Level.ALL);

        this.loader = loader;

        initGrid();
        initFilters();

        // handle selections
        GridSelectionModel<GroupModel> selectionModel = new GridSelectionModel<GroupModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GroupModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GroupModel> se) {
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
        field.setHeight(380);
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
        StoreFilterField<GroupModel> textFilter = new StoreFilterField<GroupModel>() {

            @Override
            protected boolean doSelect(Store<GroupModel> store, GroupModel parent,
                    GroupModel record, String property, String filter) {
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
        store = new ListStore<GroupModel>(loader);
        store.setKeyProvider(new SenseKeyProvider<GroupModel>());
        store.setMonitorChanges(true);

        pagingBar = new PagingToolBar(10);
        pagingBar.bind(loader);

        // Column model
        ColumnConfig id = new ColumnConfig(UserModel.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig name = new ColumnConfig(GroupModel.NAME, "Name", 125);
        ColumnConfig description = new ColumnConfig(GroupModel.DESCRIPTION, "Description", 125);
        ColumnConfig isPublic = new ColumnConfig(GroupModel.PUBLIC, "Public", 75);
        ColumnConfig isHidden = new ColumnConfig(GroupModel.HIDDEN, "Hidden", 75);
        ColumnConfig isAnon = new ColumnConfig(GroupModel.ANONYMOUS, "Anonymous", 75);
        ColumnModel cm = new ColumnModel(Arrays.asList(id, name, description, isPublic, isHidden,
                isAnon));

        grid = new Grid<GroupModel>(store, cm);
        grid.setAutoExpandColumn(GroupModel.NAME);
        grid.setBorders(false);
        grid.setId("group-join-grid");
        grid.setStateful(true);
        grid.setLoadMask(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public GroupModel getGroup() {
        return selected;
    }
}
