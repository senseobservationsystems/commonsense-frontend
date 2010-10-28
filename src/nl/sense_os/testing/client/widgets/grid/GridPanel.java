package nl.sense_os.Sample.client.widgets.grid;

import java.util.HashMap;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * An object of this class renders a grid into a content panel.
 * 
 * @author fede
 *
 */
public class GridPanel extends ContentPanel {

	// Grid properties.
	protected Grid<ModelData> grid;
	protected DataStore dataStore;

	// Panel properties.
	public static final int WIDTH = 1;
	public static final int AUTO_WIDTH = 2;
	public static final int HEIGHT = 3;
	public static final int AUTO_HEIGHT = 4;
	public static final int STRIP_ROWS = 5;
	public static final int BORDERS = 6;
	
	/**
	 * Constructor.
	 * 
	 * @param url
	 * @param mt
	 * @param cm
	 */
	public GridPanel(String url, ModelType mt, ColumnModel cm) {
		// Data store
		dataStore = new DataStore(url, mt);
		ListStore<ModelData> store = dataStore.getStore();

		// Grid
		grid = new Grid<ModelData>(store, cm);

		// Adds the grid to the content panel.
		add(grid);
	}
	
	/**
	 * Adds a listener for an event type.
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void addListener(EventType eventType, Listener<?> listener) {
		grid.addListener(eventType, listener);
	}

	/**
	 * Loads the data store with the data from the url.
	 */
	public void load() {
		dataStore.getStore().getLoader().load();
	}	
	
	/**
	 * 
	 * @return
	 */
	public DataStore getDataStore() {
		return dataStore;
	}	
	
	/**
	 * Sets panel and grid properties.
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
	
}
