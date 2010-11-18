package nl.sense_os.testing.client.common.grid;

import java.util.Map;

import com.extjs.gxt.ui.client.js.JsonConverter;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

/**
 * 
 * @author fede
 *
 */
public class ColumnModelBuilder {
	/**
	 * 	Format:
	 * 		
	 * 		{column: [
	 * 			{id: 'id', head: 'id', dataidx: 'id', w: 100}
	 * 			,{id: 'name', head: 'name', dataidx: 'name', w: 100}
	 * 		]}
	 */
	private String conf;
	private ColumnModel cm;
	
	/**
	 * 
	 * @param columnModel
	 */
	public ColumnModelBuilder(String conf) {
		this.conf = conf;
		
		setColumnModel();
	}
	
	/**
	 * 
	 */
	private void setColumnModel() {
		Map<String, Object> config = JsonConverter.decode(conf);

		System.out.println("config: " + config.toString());
		//System.out.println("dataidx: " + config.get("dataidx"));
	}
	
}
