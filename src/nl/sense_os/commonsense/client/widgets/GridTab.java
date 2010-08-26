package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.visualization.client.DataTable;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;

public class GridTab extends LayoutContainer {
    
    private static final String TAG = "GridTab";
    private DataTable data;
    private Grid<SensorValueModel> grid;
    private TagModel sensor;
    private long[] timeRange;

    public GridTab(TagModel sensor, long[] timeRange) {
        this.sensor = sensor;
        this.timeRange = timeRange;

        this.grid = createLiveGrid();
        this.grid.setSize(200, 200);

        ContentPanel cp = new ContentPanel();
        cp.setSize(400, 400);
        cp.add(this.grid);
        cp.setFrame(true);

        // this.setSize(200,200);
        this.add(cp);
        this.setBorders(true);
    }

    private Grid<SensorValueModel> createLiveGrid() {
        
        ListStore<SensorValueModel> store = new ListStore<SensorValueModel>();
        
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("timestamp", 100));
        columns.add(new ColumnConfig("value", 100));

        ColumnModel cm = new ColumnModel(columns);

        Grid<SensorValueModel> grid = new Grid<SensorValueModel>(store, cm);
        grid.setLoadMask(true);
        grid.setHeight(200);

        return grid;
    }
}
