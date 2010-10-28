package nl.sense_os.testing.client;

import com.extjs.gxt.ui.client.Registry;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.HttpProxy;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;

import java.util.List;

final public class RPCServiceCaller {

	static public ListStore<ModelData> JsonStoreCreatePaginate(String symbol,ModelType mt, String url, String sortField){

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		HttpProxy<Object> proxy = new HttpProxy<Object>(builder);
				
		// ------ json decoder -----------//
		// basic load without paginate //
		/*  
		    JsonReader<ListLoadResult<ModelData>> reader = new JsonReader<ListLoadResult<ModelData>>(mt);
		    BaseListLoader<ListLoadResult<ModelData>> loader = new BaseListLoader<ListLoadResult<ModelData>>(proxy, reader);
		    */
		// ------ end json decoder -----------//
		
		JsonLoadResultReader<PagingLoadResult<ModelData>> reader = 
				new JsonLoadResultReader<PagingLoadResult<ModelData>>(mt) {
					@Override
					protected ListLoadResult<ModelData> newLoadResult(Object loadConfig, List<ModelData> models) {
						
							PagingLoadConfig pagingConfig = (PagingLoadConfig) loadConfig;
														
							PagingLoadResult<ModelData> result = 
								new BasePagingLoadResult<ModelData>(
									models,
									pagingConfig.getOffset(), 
									pagingConfig.getLimit());
							
							return result;
						}
				};

		BasePagingLoader<PagingLoadResult<ModelData>> loader = 
				new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);
		
		loader.addLoadListener(new LoadListener() {
			public void loaderBeforeLoad(LoadEvent le) {
				Info.display("JData","Staring Loading....");
				//Window.alert("before load");
				System.out.println("before load");
			}
			
			public void loaderLoad(LoadEvent le) {
				Info.display("JData","Loading complete!");
				System.out.println("load: " + le.getData());
			}

			public void loaderLoadException(LoadEvent le) {
				Info.display("JData","Loading Error!");
				System.out.println("ERROR: " + le.toString());
				System.out.println("ERROR: " + le.getData());
			}
		});

		//loader.setSortField(sortField);
		//loader.setRemoteSort(true);
		ListStore<ModelData> store = new ListStore<ModelData>(loader);
		Registry.register(symbol, store);
		loader.load(); // load Store
		return store;		
	}
	
	static public ListStore<ModelData> request(String url, ModelType mt){

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		HttpProxy<Object> proxy = new HttpProxy<Object>(builder);

		JsonLoadResultReader<PagingLoadResult<ModelData>> reader = 
			new JsonLoadResultReader<PagingLoadResult<ModelData>>(mt);

		BasePagingLoader<PagingLoadResult<ModelData>> loader = 
			new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);
		
		loader.addLoadListener(new LoadListener() {
			public void loaderBeforeLoad(LoadEvent le) {
				Info.display("JData","Staring Loading....");
				//Window.alert("before load");
				System.out.println("before load");
			}
			
			public void loaderLoad(LoadEvent le) {
				Info.display("JData","Loading complete!");
				System.out.println("load: " + le.getData());
			}

			public void loaderLoadException(LoadEvent le) {
				Info.display("JData","Loading Error!");
				System.out.println("ERROR: " + le.exception.toString());
			}
		});

		//loader.setSortField(sortField);
		//loader.setRemoteSort(true);
		ListStore<ModelData> store = new ListStore<ModelData>(loader);
		//Registry.register(symbol, store);
		loader.load(); // load Store
		return store;		
	}
	
	public static void asyncCall(final String url, // URL to retrieve JSON result
		      ModelType type, // ModelType to define JSON format
		      final EventType successEvent, // Event needs to be fired after a successful communication
		      final EventType failureEvent, // Event needs to be fired after a failed communication
		      int timeout) { // Timeout in seconds

		      ScriptTagProxy proxy = new ScriptTagProxy(url);
		      JsonPagingLoadResultReader reader = new JsonPagingLoadResultReader(type);
		      final PagingLoader loader = new BasePagingLoader(proxy, reader);
		      final ListStore store = new ListStore(loader);

		      final Timer t = new Timer() {
		          public void run() {
		            loader.removeAllListeners();
		            Dispatcher.forwardEvent(failureEvent, "ERROR: timeout");
		          }
		      };
		      loader.addListener(Loader.Load, new Listener() {
		    	  /*
		          public void handleEvent(LoadEvent be) {

		          }*/
				@Override
				public void handleEvent(BaseEvent be) {
					// TODO Auto-generated method stub
		              t.cancel();
		              int count = store.getCount();
		              if (count == 0) {
		                  Dispatcher.forwardEvent(failureEvent, "Zero result(s)");
		              } else
		                  Dispatcher.forwardEvent(successEvent, store);
				}
		      });

		      loader.addListener(Loader.LoadException, new Listener() {
		          public void handleEvent(BaseEvent be) {
		              Dispatcher.forwardEvent(failureEvent, "Exception: " + ((LoadEvent)be).exception.toString());
		          }
		      });
		      
		      loader.load();
		      t.schedule(timeout*1000);
		 }
	
	public static ListStore<ModelData> load(String url, ModelType mt) {		
		ScriptTagProxy<String> proxy = new ScriptTagProxy<String>(url);

		ModelType model = new ModelType();
		model.setRoot("data");
		model.setTotalName("total");
		model.addField("id");
		model.addField("name");
		
		JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader = 
			new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(model);

		//BasePagingLoadConfig config = new BasePagingLoadConfig();
		
		BasePagingLoader<PagingLoadResult<ModelData>> loader = 
			new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);
		
		ListStore<ModelData> store = new ListStore<ModelData>(loader);		
		loader.load();
		
		return store;
	}
}
