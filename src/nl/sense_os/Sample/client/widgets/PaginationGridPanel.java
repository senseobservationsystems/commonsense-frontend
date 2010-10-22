package nl.sense_os.Sample.client.widgets;

import java.util.HashMap;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

/**
 * An object of this class renders a grid and a tool bar into a content panel.
 * 
 * @author fede
 * 
 */
public class PaginationGridPanel extends ContentPanel {

	// Grid properties.
	private Grid<ModelData> grid;
	private ListStore<ModelData> store;
	private PagingToolBar toolBar;

	// Panel properties.
	public static final int WIDTH = 1;
	public static final int AUTO_WIDTH = 2;
	public static final int HEIGHT = 3;
	public static final int AUTO_HEIGHT = 4;
	public static final int STRIP_ROWS = 5;
	public static final int BORDERS = 6;

	/**
	 * 
	 * @param url data source 
	 * @param mt model data
	 * @param cm column model config
	 * @param pageSize size of each page displayed in the grid
	 */
	public PaginationGridPanel(
			String url, 
			ModelType mt, 
			ColumnModel cm, 
			int pageSize) {

		// Data store
		DataStore ds = new DataStore(url, mt);
		store = ds.getStore();

		// Grid
		grid = new Grid<ModelData>(store, cm);

		// Adds the grid to the content panel.
		add(grid);

		// Adds the tool bar to the bottom of the content panel.
		toolBar = new PagingToolBar(pageSize);
		toolBar.bind((PagingLoader<?>) store.getLoader());
		setBottomComponent(toolBar);
	}

	/**
	 * Loads the data store by getting the data from the url.
	 */
	public void load() {
		store.getLoader().load();
	}

	/**
	 * 
	 * @param conf
	 */
	public void loadConf(HashMap<Integer, Object> conf) {
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

	public void setWidth(int width) {
		grid.setWidth(width);
		super.setWidth(width);
	}

	public void setHeight(int height) {
		grid.setHeight(height);
		super.setHeight(height);
	}

	public void setAutoHeight(boolean autoHeight) {
		grid.setAutoHeight(autoHeight);
		super.setAutoHeight(autoHeight);
	}

	public void setTitle(String title) {
		super.setHeading(title);
	}

	public Grid<ModelData> getGrid() {
		return grid;
	}

	public void addListener(EventType eventType, Listener<?> listener) {
		grid.addListener(eventType, listener);
	}
}
