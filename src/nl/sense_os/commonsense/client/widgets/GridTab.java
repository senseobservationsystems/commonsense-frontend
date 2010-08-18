package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public class GridTab extends LayoutContainer {

    /*
    private static final String TAG = "TableTab";
    private DataTable data;
    private Grid<SensorValueModel> grid;
    private SensorModel sensor;
    private long[] timeRange;

    public GridTab(SensorModel sensor, long[] timeRange) {
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
        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        // data proxy
        RpcProxy<List<SensorValueModel>> proxy = new RpcProxy<List<SensorValueModel>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<SensorValueModel>> callback) {
                Log.d(TAG, "RpcProxy load...");
//                if (loadConfig == null) {
                    Timestamp start = new Timestamp(timeRange[0]);
                    Timestamp end = new Timestamp(timeRange[1]);
                    service.getSensorValues(sensor.getPhoneId(), sensor.getId(), start, end,
                            callback);
//                } else {
//                    Log.e("RpcProxy", "loadConfig unexpected type: " + loadConfig);
//                }
            }
        };

        PagingLoader<PagingLoadResult<SensorValueModel>> loader = new BasePagingLoader<PagingLoadResult<SensorValueModel>>(
                proxy);
        loader.setRemoteSort(true);
        
        ListStore<SensorValueModel> store = new ListStore<SensorValueModel>(loader);

        final PagingToolBar toolBar = new PagingToolBar(50);  
        toolBar.bind(loader);  
        
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("timestamp", 100));
        columns.add(new ColumnConfig("value", 100));

        ColumnModel cm = new ColumnModel(columns);

        Grid<SensorValueModel> grid = new Grid<SensorValueModel>(store, cm);
        grid.setLoadMask(true);
        grid.setHeight(200);

        return grid;
    }

    private void displayData() {

    }

    private void getSensorValues() {

        final MessageBox progress = MessageBox.progress("Please wait", "Requesting data...", "");
        progress.getProgressBar().auto();
        progress.show();

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensorValues: " + ex.getMessage());
                progress.close();
                onSensorValuesReceived(false, null);
            }

            public void onSuccess(List<SensorValueModel> values) {
                progress.close();
                onSensorValuesReceived(true, values);
            }
        };

        Timestamp start = new Timestamp(this.timeRange[0]);
        Timestamp end = new Timestamp(this.timeRange[1]);

        service.getSensorValues(this.sensor.getPhoneId(), this.sensor.getId(), start, end, callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        // request data from service
         getSensorValues();

        // set up this TabItem
    }

    private void onSensorValuesReceived(boolean success, List<SensorValueModel> values) {

        // fill table if values are present
        if ((true == success) && (values.size() > 0)) {

            Log.d(TAG, "Received " + values.size() + " sensor values...");

            this.data = DataTable.create();
            this.data.addColumn(ColumnType.DATETIME, "Date/Time");
            this.data.addColumn(ColumnType.STRING, "Value");

            this.data.addRows(values.size());

            switch (this.sensor.getId()) {
                // TODO
            }
        } else {
            Log.d(TAG, "Zero values received!");
        }

        displayData();
    }
    */
}
