package nl.sense_os.commonsense.client.viz.panels.table;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.HttpProxy;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * An object of this class renders a grid and a tool bar in a content panel.
 * 
 * @author fede
 * 
 */
public class PaginationGridPanel extends ContentPanel {

    private static final Logger LOG = Logger.getLogger(PaginationGridPanel.class.getName());
    private Grid<ModelData> grid;
    private PagingToolBar toolBar;
    private PagingLoader<PagingLoadResult<ModelData>> loader;
    private final int pageSize;
    private final long startDate;
    private final long endDate;

    /**
     * @param url
     *            action to be executed
     * @param mt
     *            data store structure
     * @param colConf
     *            column config
     */
    public PaginationGridPanel(String url, ModelType mt, List<ColumnConfig> colConf, int pageSize,
            long startDate, long endDate) {

        LOG.setLevel(Level.ALL);

        this.pageSize = pageSize;
        this.startDate = startDate;
        this.endDate = endDate;

        initGrid(url, mt, colConf);

        // Adds the tool bar to the top of the content panel.
        toolBar = new PagingToolBar(pageSize);
        toolBar.bind(loader);
        setTopComponent(toolBar);

        setHeaderVisible(false);
        setBodyBorder(false);

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

    private void initGrid(String url, ModelType mt, List<ColumnConfig> colConf) {

        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // Cross site HTTP proxy
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        HttpProxy<String> proxy = new HttpProxy<String>(builder);

        // Reader
        DataReader<PagingLoadResult<ModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(
                mt) {

            private int totalLength = -1;
            private boolean totalLengthKnown = false;

            @Override
            public PagingLoadResult<ModelData> read(Object loadConfig, Object data) {
                PagingLoadResult<ModelData> result = super.read(loadConfig, data);
                int offset = result.getOffset();
                int size = result.getData().size();

                if (size < pageSize) {
                    // we reached the last page
                    LOG.finest("Found total data size!");
                    totalLength = offset + size;
                    totalLengthKnown = true;

                } else if (!totalLengthKnown) {
                    // we do not know the length yet
                    LOG.finest("Guesstimate total data size...");
                    List<ModelData> sensorData = result.getData();
                    double pageNewest = Double.parseDouble(sensorData.get(0).<String> get("date"));
                    double pageOldest = Double.parseDouble(sensorData.get(sensorData.size() - 1)
                            .<String> get("date"));
                    double pageRange = pageNewest - pageOldest;
                    double queryStart = startDate / 1000d;
                    double queryRange = pageOldest - queryStart;
                    NumberFormat f = NumberFormat.getFormat("#.000");
                    LOG.finest("Page oldest: " + f.format(pageOldest) + ", page newest: "
                            + f.format(pageNewest) + ", page range: " + f.format(pageRange));
                    LOG.finest("Query start: " + f.format(startDate / 1000d) + ", query end: "
                            + f.format(queryStart) + ", query range: " + f.format(queryRange));

                    int remaining = (int) Math.round((queryRange / pageRange) * pageSize);
                    LOG.finest("Remaining points: " + remaining);

                    totalLength = offset + size + remaining;

                }
                result.setTotalLength(totalLength);
                return result;
            }
        };

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
