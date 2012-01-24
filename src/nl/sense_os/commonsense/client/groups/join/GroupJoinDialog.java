package nl.sense_os.commonsense.client.groups.join;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.common.utility.SenseKeyProvider;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
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
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupJoinDialog extends View {

    private static final Logger LOG = Logger.getLogger(GroupJoinController.class.getName());
    private Window window;
    private Grid<GroupModel> grid;
    private FormPanel form;
    private ToolBar filterBar;
    private ListStore<GroupModel> store;
    private ListLoader<ListLoadResult<GroupModel>> loader;
    private Button submitButton;
    private Button cancelButton;

    public GroupJoinDialog(Controller c) {
        super(c);
        LOG.setLevel(Level.ALL);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupJoinEvents.Show)) {
            LOG.finest("Show");
            show();

        } else if (type.equals(GroupJoinEvents.JoinSuccess)) {
            LOG.finest("JoinSuccess");
            onSuccess();

        } else if (type.equals(GroupJoinEvents.JoinFailure)) {
            LOG.finest("JoinFailure");
            onFailure();

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void onFailure() {
        setBusy(false);

    }

    private void onSuccess() {
        setBusy(false);
    }

    private void hideWindow() {
        window.hide();
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(submitButton)) {
                    if (form.isValid()) {
                        submitForm();
                    }
                } else if (pressed.equals(cancelButton)) {
                    hideWindow();
                } else {
                    LOG.warning("Unexpected button pressed: " + pressed);
                }
            }
        };
        submitButton = new Button("Join", SenseIconProvider.ICON_BUTTON_GO, l);
        cancelButton = new Button("Cancel", l);

        form.addButton(submitButton);
        form.addButton(cancelButton);

        // handle selections
        GridSelectionModel<GroupModel> selectionModel = new GridSelectionModel<GroupModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GroupModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GroupModel> se) {
                GroupModel selection = se.getSelectedItem();
                if (null != selection) {
                    submitButton.enable();
                } else {
                    submitButton.disable();
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

        window.add(form);
    }

    private void initGrid() {

        // proxy
        DataProxy<ListLoadResult<GroupModel>> proxy = new DataProxy<ListLoadResult<GroupModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<GroupModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<GroupModel>> callback) {
                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    LOG.finest("Load library...");
                    AppEvent loadRequest = new AppEvent(GroupJoinEvents.PublicGroupsRequested);
                    loadRequest.setData("callback", callback);
                    fireEvent(loadRequest);
                } else {
                    LOG.warning("Unexpected load config: " + loadConfig);
                    callback.onFailure(null);
                }
            }
        };

        // list loader
        loader = new BaseListLoader<ListLoadResult<GroupModel>>(proxy);

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

    private void show() {
        window = new CenteredWindow();
        window.setHeading("Join a public group");
        window.setSize(540, 480);
        window.setLayout(new FitLayout());

        initForm();

        window.show();

        loader.load();
    }

    private void submitForm() {

        GroupModel group = grid.getSelectionModel().getSelectedItem();
        AppEvent event = new AppEvent(GroupJoinEvents.JoinRequest);
        event.setData("group", group);
        event.setSource(this);
        Dispatcher.forwardEvent(event);

        setBusy(true);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            submitButton.setIcon(SenseIconProvider.ICON_LOADING);
            cancelButton.disable();
        } else {
            submitButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
            cancelButton.enable();
        }
    }
}
