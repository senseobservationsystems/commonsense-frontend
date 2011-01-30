package nl.sense_os.commonsense.client.views.components.grids;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

import java.util.HashMap;
import java.util.List;

/**
 * An object of this class renders a grid and a tool bar in a content panel.
 * 
 * @author fede
 * 
 */
public class PaginationGridPanel extends ContentPanel {

    public static final int AUTO_HEIGHT = 4;
    public static final int AUTO_WIDTH = 2;
    public static final int BORDERS = 6;
    public static final int HEIGHT = 3;
    public static final int STRIP_ROWS = 5;
    @SuppressWarnings("unused")
    private static final String TAG = "PaginatonGridPanel";
    // Grid properties.
    public static final int WIDTH = 1;

    private Grid<ModelData> grid;
    private PagingToolBar toolBar;

    /**
     * 
     * @param url
     *            action to be executed
     * @param mt
     *            data store structure
     * @param colConf
     *            column config
     */
    public PaginationGridPanel(String url, ModelType mt, List<ColumnConfig> colConf, int pageSize) {
        // Cross site HTTP proxy.
        DataProxy<String> proxy = new ScriptTagProxy<String>(url);

        // Reader
        DataReader<PagingLoadResult<ModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(
                mt);

        // Loader
        PagingLoader<PagingLoadResult<ModelData>> loader = createLoader(proxy, reader);

        // Data store
        ListStore<ModelData> store = new ListStore<ModelData>(loader);

        // Column model
        ColumnModel cm = new ColumnModel(colConf);

        // Adds the tool bar to the top of the content panel.
        this.toolBar = new PagingToolBar(pageSize);
        this.toolBar.bind(loader);
        setTopComponent(toolBar);

        // Grid
        this.grid = new Grid<ModelData>(store, cm);
        this.grid.setAutoExpandColumn("value");
        this.grid.setAutoExpandMax(1000);
        this.grid.setLoadMask(true);

        // Adds the grid to the content panel.
        add(grid);

        // Loads the data store by getting the data from the URL
        loader.load();
    }

    private PagingLoader<PagingLoadResult<ModelData>> createLoader(DataProxy<String> proxy,
            DataReader<PagingLoadResult<ModelData>> reader) {
        PagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(
                proxy, reader);
        loader.setRemoteSort(true);
        loader.setSortDir(SortDir.DESC);
        loader.setSortField("date");
        loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {

            /* sets the parameters names correctly for CommonSense API */
            @Override
            public void handleEvent(LoadEvent be) {

                BasePagingLoadConfig m = be.<BasePagingLoadConfig> getConfig();
                int offset = m.get("offset");
                int limit = m.get("limit");

                // page size
                m.set("per_page", limit);
                // m.remove("limit");

                // page number
                m.set("page", offset / limit);
                // m.remove("offset");

                // sort dir
                m.set("sort", be.<ModelData> getConfig().get("sortDir"));
                // m.remove("sortDir");
                // m.remove("sortField");
            }
        });

        return loader;
    }

    public Grid<ModelData> getGrid() {
        return grid;
    }

    void loadConf(HashMap<Integer, Object> conf) {
        if (conf.containsKey(HEIGHT)) {
            grid.setHeight((Integer) conf.get(HEIGHT));
        }

        if (conf.containsKey(WIDTH)) {
            // Sets the grid width.
            grid.setHeight((Integer) conf.get(WIDTH));
            // Sets the content panel width.
            setWidth((Integer) conf.get(WIDTH));
        }

        if (conf.containsKey(STRIP_ROWS)) {
            grid.setStripeRows((Boolean) conf.get(STRIP_ROWS));
        }

        if (conf.containsKey(BORDERS)) {
            grid.setBorders((Boolean) conf.get(BORDERS));
        }

        if (conf.containsKey(AUTO_HEIGHT)) {
            grid.setAutoHeight((Boolean) conf.get(AUTO_HEIGHT));
        }

        // grid.setStyleAttribute("borderTop", "none");
        // grid.setAutoExpandColumn("id");
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        // grid is not resized automatically
        final int toolBarHeight = this.toolBar.getHeight();
        final int extraSpace = 27; // some extra space because the height is not correct
        grid.setWidth(width);
        grid.setHeight(height - toolBarHeight - extraSpace);
    }

    @Override
    public void setTitle(String title) {
        super.setHeading(title);
    }
}
