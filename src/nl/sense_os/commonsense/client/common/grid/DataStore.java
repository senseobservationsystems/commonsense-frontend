package nl.sense_os.commonsense.client.common.grid;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;

import nl.sense_os.commonsense.client.utility.Log;

/**
 * An object of this class makes a remote request and stores the returned data into
 * a data store according to a given data model.
 * 
 * @author fede
 *
 */
public class DataStore {
	
	private static final String TAG = "DataStore";
    private ScriptTagProxy<String> proxy;
	private JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader;
	private ListStore<ModelData> store;
	private BasePagingLoader<PagingLoadResult<ModelData>> loader;
	
	/**
	 * Constructor.
	 * 
	 * @param url
	 * @param dataModel
	 */
	public DataStore(String url, ModelType dataModel) {
		// Proxy to make cross site requests.
		proxy = new ScriptTagProxy<String>(url);
		
		// Reader
		reader = new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(dataModel);

		// Loader
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);
		
		store = new ListStore<ModelData>(loader);
	}
	
	/**
	 * 
	 * @return
	 */
	public ListStore<ModelData> getStore() {
		return store;
	}
	
	/**
	 * 
	 * @param offset
	 */
	public void setOffset(int offset) {
        Log.d(TAG, "setOffset");
        
		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {  
			@Override
            public void handleEvent(LoadEvent be) {
			    Log.d(TAG, "BeforeLoad");
                be.<ModelData> getConfig().set("start", be.<ModelData> getConfig().get("offset"));  
                be.<ModelData> getConfig().set("per_page", 25);
			}  
		});		
	}
}
