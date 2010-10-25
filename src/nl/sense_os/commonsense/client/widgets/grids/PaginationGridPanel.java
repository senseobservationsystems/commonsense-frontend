package nl.sense_os.commonsense.client.widgets.grids;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
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

	Grid<ModelData> grid;

	// Grid properties.
	public static final int WIDTH = 1;
	public static final int AUTO_WIDTH = 2;
	public static final int HEIGHT = 3;
	public static final int AUTO_HEIGHT = 4;
	public static final int STRIP_ROWS = 5;
	public static final int BORDERS = 6;
	
	/**
	 * 
	 * @param url action to be executed
	 * @param mt data store structure
	 * @param colConf column config
	 */
	public PaginationGridPanel(String url, ModelType mt, List<ColumnConfig> colConf, int pageSize) {		
		// Cross site http proxy.
		ScriptTagProxy<String> proxy = new ScriptTagProxy<String>(url);
		
		// Reader
		JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader = 
			new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(mt);

		// Loader
		BasePagingLoader<PagingLoadResult<ModelData>> loader = 
			new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);

		// Data store
		ListStore<ModelData> store = new ListStore<ModelData>(loader);				
			
		// Column model
		ColumnModel cm = new ColumnModel(colConf);
		
		// Grid
		grid = new Grid<ModelData>(store, cm);
        grid.setAutoExpandColumn("v");

		// Adds the grid to the content panel.
		add(grid);
		
		// Adds the tool bar to the bottom of the content panel.
		PagingToolBar toolBar = new PagingToolBar(pageSize);
		//toolBar.bind((BasePagingLoader<PagingLoadResult<ModelData>>)store.getLoader());
		toolBar.bind(loader);		
		setBottomComponent(toolBar);
		
		// Loads the data store by getting the data from the url.
		loader.load();
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
		
		//grid.setStyleAttribute("borderTop", "none");
		//grid.setAutoExpandColumn("id");		
	}

	@Override
    public void setWidth(int width) {
        super.setWidth(width);
		grid.setWidth(width);
	}
	
	@Override
    public void setHeight(int height) {
        super.setHeight(height);
		grid.setHeight(height);
	}

	@Override
    public void setAutoHeight(boolean autoHeight) {
        super.setAutoHeight(autoHeight);
		grid.setAutoHeight(autoHeight);		
	}
	
	@Override
    public void setTitle(String title) {
		super.setHeading(title);
	}
	
	public Grid<ModelData> getGrid() {
		return grid;
	}
}
