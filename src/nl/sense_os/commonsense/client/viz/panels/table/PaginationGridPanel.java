package nl.sense_os.commonsense.client.viz.panels.table;

import java.util.List;

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

/**
 * An object of this class renders a grid and a tool bar in a content panel.
 * 
 * @author fede
 * 
 */
public class PaginationGridPanel extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "PaginatonGridPanel";

    private Grid<ModelData> grid;
    private PagingToolBar toolBar;
    private PagingLoader<PagingLoadResult<ModelData>> loader;

    /**
     * @param url
     *            action to be executed
     * @param mt
     *            data store structure
     * @param colConf
     *            column config
     */
    public PaginationGridPanel(String url, ModelType mt, List<ColumnConfig> colConf, int pageSize) {

        initGrid(url, mt, colConf);

        // Adds the tool bar to the top of the content panel.
        this.toolBar = new PagingToolBar(pageSize);
        this.toolBar.bind(this.loader);
        setTopComponent(this.toolBar);

        setHeaderVisible(false);
        setBodyBorder(false);

        // Adds the grid to the content panel.
        add(this.grid);

        // Loads the data store by getting the data from the URL
        this.loader.load();
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

    private void initGrid(String url, ModelType mt, List<ColumnConfig> colConf) {
        // Cross site HTTP proxy.
        DataProxy<String> proxy = new ScriptTagProxy<String>(url);

        // Reader
        DataReader<PagingLoadResult<ModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(
                mt);

        // Loader
        this.loader = createLoader(proxy, reader);

        // Data store
        ListStore<ModelData> store = new ListStore<ModelData>(this.loader);

        // Column model
        ColumnModel cm = new ColumnModel(colConf);

        // Grid
        this.grid = new Grid<ModelData>(store, cm);
        this.grid.setAutoExpandColumn("value");
        this.grid.setAutoExpandMax(1000);
        this.grid.setLoadMask(true);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        // grid is not resized automatically
        final int toolBarHeight = toolBar.getHeight();
        grid.setWidth(width);
        grid.setHeight(height - toolBarHeight);
    }
}
