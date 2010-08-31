package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.dto.JsonValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;

public class GridTab extends VisualizationTab {

    private static final String TAG = "GridTab";
    private List<BaseModel> baseModelData;

    public GridTab() {
        super();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeaderVisible(false);
        this.add(this.panel, new RowData(1, 1));
    }

    public GridTab(TaggedDataModel data) {
        this();

        addData(data);
    }

    @Override
    public void addData(List<TaggedDataModel> taggedDatas) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addData(TaggedDataModel taggedData) {

        // prepare BaseModel versions of the incoming data
        List<BaseModel> baseModels = new ArrayList<BaseModel>();
        for (SensorValueModel value : taggedData.getData()) {
            if (value instanceof JsonValueModel) {
                Map<String, Object> fields = ((JsonValueModel) value).getFields();
                for (Map.Entry<String, Object> field : fields.entrySet()) {
                    BaseModel m = new BaseModel();
                    m.set("timestamp", value.getTimestamp());
                    m.set("value", field.getValue());
                    m.set("tag", taggedData.getTag().get("text") + ": " + field.getKey());
                    baseModels.add(m);
                }
            } else {
                BaseModel m = new BaseModel();
                m.set("timestamp", value.getTimestamp());
                m.set("value", value.get("value"));
                m.set("tag", taggedData.getTag().get("text"));
                baseModels.add(m);
            }
        }

        // add new data to current collection
        if (this.baseModelData != null) {
            this.baseModelData.addAll(baseModels);
        } else {
            this.baseModelData = baseModels;
        }

        // add data to the paging proxy
        PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(this.baseModelData);
        
        BasePagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(
                proxy);
        loader.setSortField("timestamp");

        ListStore<BaseModel> store = new ListStore<BaseModel>(loader);

        final int pageSize = 42;
        final PagingToolBar toolBar = new PagingToolBar(pageSize);
        toolBar.bind(loader);

        loader.load(0, pageSize);

        ColumnModel cm = createColumns();

        Grid<BaseModel> grid = new Grid<BaseModel>(store, cm);
        grid.setLoadMask(true);

        panel.removeAll();
        panel.add(grid);
//        panel.add(toolBar);
        panel.layout();
    }

    ContentPanel panel;

    private ColumnModel createColumns() {
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        configs.add(new ColumnConfig("tag", "Tag", 200));
        configs.add(new ColumnConfig("value", "Value", 200));
        configs.add(new ColumnConfig("timestamp", "Time", 200));

        return new ColumnModel(configs);
    }

    /*
     * private ContentPanel createGrid() {
     * 
     * List<BaseModel> preparedData = prepareData(); PagingModelMemoryProxy proxy = new
     * PagingModelMemoryProxy(preparedData);
     * 
     * PagingLoader<PagingLoadResult<ModelData>> loader = new
     * BasePagingLoader<PagingLoadResult<ModelData>>( proxy); loader.setRemoteSort(true);
     * 
     * ListStore<BaseModel> store = new ListStore<BaseModel>(loader); // store.add(prepareData());
     * 
     * final int pageSize = 42; final PagingToolBar toolBar = new PagingToolBar(pageSize);
     * toolBar.bind(loader);
     * 
     * loader.load(0, pageSize);
     * 
     * ColumnModel cm = createColumns();
     * 
     * Grid<BaseModel> grid = new Grid<BaseModel>(store, cm); grid.setLoadMask(true);
     * 
     * ContentPanel panel = new ContentPanel(new FitLayout()); panel.add(grid);
     * panel.setBottomComponent(toolBar);
     * 
     * return panel; }
     */

    /*
     * private List<BaseModel> prepareData() { List<BaseModel> result = new ArrayList<BaseModel>();
     * 
     * SensorValueModel[] values = this.data.getData(); for (SensorValueModel value : values) {
     * 
     * if (value instanceof JsonValueModel) { Map<String,String> fields = ((JsonValueModel)
     * value).getFields(); for (Map.Entry<String, String> field : fields.entrySet()) { BaseModel m =
     * new BaseModel(); m.set("timestamp", value.getTimestamp()); m.set("value", field.getValue());
     * m.set("tag", this.data.getTag().get("text") + ": " + field.getKey()); result.add(m); } } else
     * { BaseModel m = new BaseModel(); m.set("timestamp", value.getTimestamp()); m.set("value",
     * value.get("value")); m.set("tag", this.data.getTag().get("text")); result.add(m); } }
     * 
     * return result; }
     */
}
