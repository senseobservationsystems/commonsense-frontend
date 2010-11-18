package nl.sense_os.commonsense.client.common.grid;

import nl.sense_os.commonsense.client.common.grid.GridPanel;

import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

/**
 * An object of this class renders a grid and a tool bar into a content panel.
 * 
 * @author fede
 * 
 */
public class PaginationGridPanel extends GridPanel {

	// GridPanel properties.
	private PagingToolBar toolBar;

	/**
	 * 
	 * @param url data source 
	 * @param mt model data
	 * @param cm column model config
	 * @param pageSize number of registers to be displayed in each page of the grid
	 */
	public PaginationGridPanel(
			String url, 
			ModelType mt, 
			ColumnModel cm,
			int pageSize) {

		// Create the data store and the grid panel.
		super(url, mt, cm);

		// Adds the tool bar to the bottom of the content panel.
		createToolBar(pageSize);
	}

	/**
	 * Adds a tool bar to the bottom of the content panel.
	 * 
	 * @param pageSize number of registers to be displayed in each page of the 
	 * grid
	 */
	private void createToolBar(int pageSize) {
		toolBar = new PagingToolBar(pageSize);
		toolBar.bind((PagingLoader<?>) dataStore.getStore().getLoader());
		setBottomComponent(toolBar);		
	}	
}
