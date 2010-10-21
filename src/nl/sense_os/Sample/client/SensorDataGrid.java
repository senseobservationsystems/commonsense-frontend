/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 *
 * http://extjs.com/license
 */
package nl.sense_os.Sample.client;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import nl.sense_os.Sample.client.widgets.PaginationGridPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

public class SensorDataGrid extends LayoutContainer {

	public SensorDataGrid(){
		// Data store structure.
		ModelType model = new ModelType();
		model.setTotalName("total");
		model.setRoot("data");
		model.addField("t");
		model.addField("v");
		
		// Column model.
		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig();
		column.setId("t");
		column.setHeader("time");
		column.setDataIndex("t");
		column.setWidth(200);			
		colConf.add(column);		
		
		column = new ColumnConfig();
		column.setId("v");
		column.setHeader("value");
		column.setDataIndex("v");		
		column.setWidth(200);
		colConf.add(column);
		
		// Grid.
		PaginationGridPanel gridPanel = new PaginationGridPanel(
				"http://dev.almende.com/commonsense/get_sensor_data_paged.php" +
				"?email=steven@sense-os.nl&password=81dc9bdb52d04dc20036dbd8313ed055&d_id=78&s_id=1",
				model, 
				colConf,
				10);
		
		// Grid config.
		/*
		HashMap<Integer, Object> gridConf = new HashMap<Integer, Object>();
		gridConf.put(grid.WIDTH, 402);
		gridConf.put(grid.AUTO_HEIGHT, true);
		grid.loadConf(gridConf);
		*/
		gridPanel.setWidth(402);
		gridPanel.setAutoHeight(true);
		gridPanel.setTitle("sensor data");
		gridPanel.setCollapsible(true);
		gridPanel.setBodyBorder(true);

		new Draggable(gridPanel);
		
		add(gridPanel);
	}
}
