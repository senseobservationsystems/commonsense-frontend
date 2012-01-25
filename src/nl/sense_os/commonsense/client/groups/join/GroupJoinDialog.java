package nl.sense_os.commonsense.client.groups.join;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.common.utility.SenseKeyProvider;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class GroupJoinDialog extends Window {

    private static final Logger LOG = Logger.getLogger(GroupJoinDialog.class.getName());

    private Grid<GroupModel> grid;
    private FormPanel form;
    private ToolBar filterBar;
    private ListStore<GroupModel> store;
    private ListLoader<ListLoadResult<GroupModel>> loader;
    private Button btnSubmit;
    private Button btnCancel;

    public GroupJoinDialog(ListLoader<ListLoadResult<GroupModel>> loader) {
        LOG.setLevel(Level.ALL);

        this.loader = loader;

        setHeading("Join a public group");
        setSize(540, 480);
        setLayout(new FitLayout());

        initForm();

        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public Button getBtnSubmit() {
        return btnSubmit;
    }

    public Grid<GroupModel> getGrid() {
        return grid;
    }

    private void initButtons() {

        btnSubmit = new Button("Join");
        btnSubmit.setIconStyle("sense-btn-icon-go");
        btnCancel = new Button("Cancel");

        form.addButton(btnSubmit);
        form.addButton(btnCancel);

        // handle selections
        GridSelectionModel<GroupModel> selectionModel = new GridSelectionModel<GroupModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GroupModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GroupModel> se) {
                GroupModel selection = se.getSelectedItem();
                if (null != selection) {
                    btnSubmit.enable();
                } else {
                    btnSubmit.disable();
                }
            }
        });
        grid.setSelectionModel(selectionModel);
    }

    private void initFields() {

        initGrid();
        initFilters();

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setTopComponent(filterBar);
        panel.add(grid);

        AdapterField field = new AdapterField(panel);
        field.setHeight(380);
        field.setResizeWidget(true);
        field.setFieldLabel("Select the group");

        final FormData formData = new FormData("-10");
        form.add(field, formData);
    }

    /**
     * Initializes filter toolbar for the grid with sensors. The bar contains text filter and an
     * owner filter.
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

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setLabelAlign(LabelAlign.TOP);
        form.setBodyBorder(false);
        // this.form.setFieldWidth(275);

        initFields();
        initButtons();

        add(form);
    }

    private void initGrid() {

        // list store
        store = new ListStore<GroupModel>(loader);
        store.setKeyProvider(new SenseKeyProvider<GroupModel>());
        store.setMonitorChanges(true);

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
    }

    public void setBusy(boolean busy) {
        if (busy) {
            btnSubmit.setIconStyle("sense-btn-icon-loading");
            btnCancel.disable();
        } else {
            btnSubmit.setIconStyle("sense-btn-icon-go");
            btnCancel.enable();
        }
    }
}
