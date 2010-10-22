package nl.sense_os.Sample.client.widgets;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.store.ListStore;

/**
 * An object of this class makes a remote request and stores the returned data into
 * a data store according to a given data model.
 * 
 * @author fede
 *
 */
public class DataStore {
	
	private ScriptTagProxy<String> proxy;
	private JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader;
	private ListStore<ModelData> store;
	
	public DataStore(String url, ModelType dataModel) {
		// Proxy to make cross site requests.
		proxy = new ScriptTagProxy<String>(url);
		
		// Reader
		reader = new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(dataModel);

		// Loader
		BasePagingLoader<PagingLoadResult<ModelData>> loader = 
			new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);

		store = new ListStore<ModelData>(loader);
	}
	
	public ListStore<ModelData> getStore() {
		return store;
	}
}
