package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.SnifferValueModel;

public class MyriaTab extends LayoutContainer {

    private class MyriaNode extends BaseModel {

        private static final long serialVersionUID = 1L;

        public MyriaNode(int nodeId, String sensorName, int colIndex) {
            setNodeId(nodeId);
            setSensorName(sensorName);
            setColIndex(colIndex);
            incPointCount();
        }

        public int getColIndex() {
            return get("column_index", -1);
        }

        public int getNodeId() {
            return get("node_id", -1);
        }

        public String getSensorName() {
            return get("sensor_name");
        }
        
        public int incPointCount() {
            int count = get("point_count", 0);
            set("point_count", ++count);
            return count;
        }

        public void setColIndex(int colIndex) {
            set("column_index", colIndex);
        }

        public void setNodeId(int nodeId) {
            set("node_id", nodeId);
        }
        
        public void setPointCount(int count) {
            set("point_count", count);
        }

        public void setSensorName(String sensorName) {
            set("sensor_name", sensorName);
        }
    }

    private static final String TAG = "MyriaTab";
    private AnnotatedTimeLine chart;
    private MotionChart mChart;
    private TabPanel chartPanel;
    private SensorModel sensor;
    private HashMap<MyriaNode, AnnotatedTimeLine> shownCharts;
    private ListStore<MyriaNode> store;
    private long[] timeRange;
    private Grid<MyriaNode> nodeSelector;

    public MyriaTab(SensorModel sensor, long[] timeRange) {
        this.sensor = sensor;
        this.shownCharts = new HashMap<MyriaNode, AnnotatedTimeLine>();
        this.timeRange = timeRange; 

        final TabPanel centerPanel = createCenterPanel();
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));

        final ContentPanel nodeSelectPanel = createNodeSelector();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 200, 200, 300);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(true);
        
        this.setLayout(new BorderLayout());
        this.add(nodeSelectPanel, westLayout);
        this.add(centerPanel, centerLayout); 
    }

    private void addChart(MyriaNode m) {

        chart.showDataColumns(m.getColIndex() - 1);

        this.shownCharts.put(m, chart);
    }

    private TabPanel createCenterPanel() {

        this.chartPanel = new TabPanel();
        this.chartPanel.setSize("100%", "100%");
        this.chartPanel.setPlain(true);
        
        return this.chartPanel;
    }

    private ColumnModel createNodeCols(CheckBoxSelectionModel<MyriaNode> selectMdl) {
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // check box selection column
        configs.add(selectMdl.getColumn());

        // node id column
        ColumnConfig column = new ColumnConfig();
        column.setId("node_id");
        column.setHeader("Node ID");
        column.setWidth(50);
        configs.add(column);

        // node id column
        column = new ColumnConfig();
        column.setId("sensor_name");
        column.setHeader("Sensor");
        column.setWidth(60);
        configs.add(column);

        // datapoint count column
        column = new ColumnConfig();
        column.setId("point_count");
        column.setHeader("# points");
        column.setWidth(90);
        configs.add(column);

        return new ColumnModel(configs);
    }

    private ContentPanel createNodeSelector() {

        // request sensor values from service
        getSensorValues(this.timeRange);

        // selection model using check boxes
        final CheckBoxSelectionModel<MyriaNode> selectMdl = new CheckBoxSelectionModel<MyriaNode>();
        selectMdl.addSelectionChangedListener(new SelectionChangedListener<MyriaTab.MyriaNode>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<MyriaNode> se) {

                final List<MyriaNode> newSelection = se.getSelection();
                final Set<MyriaNode> oldSelection = shownCharts.keySet();

                // find newly selected items
                List<MyriaNode> toAdd = new ArrayList<MyriaNode>();
                for (MyriaNode m : newSelection) {

                    if (false == oldSelection.contains(m)) {
                        toAdd.add(m);
                    }
                }

                // find newly deselected items
                List<MyriaNode> toRemove = new ArrayList<MyriaNode>();
                for (MyriaNode m : oldSelection) {

                    if (false == newSelection.contains(m)) {
                        toRemove.add(m);
                    }
                }

                for (MyriaNode mn : toAdd) {
                    addChart(mn);
                }

                for (MyriaNode mn : toRemove) {
                    removeChart(mn);
                }
            }
        });

        ColumnModel columnMdl = createNodeCols(selectMdl);

        if (null == this.store){ 
            this.store = new ListStore<MyriaNode>();
        }

        this.nodeSelector = new Grid<MyriaNode>(this.store, columnMdl);
        this.nodeSelector.setSelectionModel(selectMdl);
        this.nodeSelector.addPlugin(selectMdl);

        final ContentPanel panel = new ContentPanel();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setSize("100%", "100%");
        panel.setHeading("MyriaNed nodes");
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);
        panel.add(nodeSelector);

        return panel;
    }
    
    private Timestamp getNextQuarterHour(Timestamp now) {
        final long period = 10 * 60 * 1000;
        final long remainder = now.getTime() % period;
        return new Timestamp(now.getTime() - remainder + period);
    }

    /**
     * Requests the sensor values from the service. <code>onSensorValuesReceived</code> is invoked
     * by the request's callback.
     */
    private void getSensorValues(long[] timeRange) {

        // show progress dialog
        final MessageBox wait = MessageBox.wait("CommonSense Web Application",
                "Requesting data, please wait...", "Requesting data...");

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensorValues: " + ex.getMessage());
                wait.close();
                onSensorValuesReceived(false, null);
            }

            public void onSuccess(List<SensorValueModel> values) {
                wait.close();
                onSensorValuesReceived(true, values);
            }
        };

        Timestamp start = new Timestamp(timeRange[0]);
        Timestamp end = new Timestamp(timeRange[1]);

        service.getSensorValues(this.sensor.getPhoneId(), this.sensor.getId(), start, end, callback);
        
//        ArrayList<SensorValueModel> values = new ArrayList<SensorValueModel>();
//        values.add(new SnifferValueModel(new Timestamp(0 * 24 * 60 * 60 * 1000), "2000", "1", "foo", ""));
//        values.add(new SnifferValueModel(new Timestamp(0 * 24 * 60 * 60 * 1000), "2500", "2", "bar", ""));
//        values.add(new SnifferValueModel(new Timestamp(1 * 24 * 60 * 60 * 1000), "1500", "1", "foo", ""));
//        values.add(new SnifferValueModel(new Timestamp(2 * 24 * 60 * 60 * 1000), "2000", "2", "bar", ""));
//        values.add(new SnifferValueModel(new Timestamp(3 * 24 * 60 * 60 * 1000), "2200", "2", "bar", ""));
//        values.add(new SnifferValueModel(new Timestamp(3 * 24 * 60 * 60 * 1000), "2200", "1", "foo", ""));
//        
//        onSensorValuesReceived(true, values);
    }

    /**
     * Puts the newly received sensor values in a DataTable and draws the chart.
     * 
     * @param success
     *            boolean indicating if the values were successfully received.
     * @param values
     *            the values.
     */
    private void onSensorValuesReceived(boolean success, List<SensorValueModel> values) {

        HashMap<String, MyriaNode> nodes = new HashMap<String, MyriaNode>();

        // fill table if values are present
        if ((true == success) && (values.size() > 0)) {
            Log.d(TAG, "Received " + values.size() + " sensor values");

            DataTable motionData = DataTable.create();
            motionData.addColumn(ColumnType.STRING, "Node", "node");
            motionData.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");
            motionData.addColumn(ColumnType.NUMBER, "Value", "value");
            
            DataTable timeChartData = DataTable.create();
            timeChartData.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");

            HashMap<Timestamp, Double> max = new HashMap<Timestamp, Double>();
            HashMap<Timestamp, Double> min = new HashMap<Timestamp, Double>();
            
            // keep track of number of columns in
            for (int i = 0; i < values.size(); i++) {
                SnifferValueModel v = (SnifferValueModel) values.get(i);
                final Timestamp time = getNextQuarterHour(v.getTimestamp());
                final int nodeId = Integer.parseInt(v.getNodeId());
                final String sensorName = v.getSensorName();
                final double value = this.sensor.getName().equals("temperature") ? Double
                        .parseDouble(v.getValue()) / 100 : Double.parseDouble(v.getValue()) / 10;

                // look up node in list of known nodes
                MyriaNode node = nodes.get(nodeId + sensorName);
                if (node == null) {
                    // add new column in table if node is new
                    int colIndex = timeChartData.getNumberOfColumns();
                    timeChartData.addColumn(ColumnType.NUMBER, "node " + nodeId + " (" + sensorName
                            + ")", "node_" + nodeId);
                    
                    node = new MyriaNode(nodeId, sensorName, colIndex);                    
                    node.setColIndex(colIndex);
                    nodes.put(nodeId + sensorName, node);

//                    Log.d(TAG, "New node: " + nodeId + " " + sensorName + ", col: "  + colIndex);
                }
                node.incPointCount();
                
                // check if this value is the new maximum
                Double currentMax = max.get(time);                
                if ((null == currentMax) || (currentMax.doubleValue() < value)) {
                    max.put(time, value);
                }
                
                // check if this value is the new minimum
                Double currentMin = min.get(time);                
                if ((null == currentMin) || (currentMin.doubleValue() > value)) {
                    min.put(time, value);
                }

                // add data to the table
                final int rowIndex = timeChartData.getNumberOfRows();
                timeChartData.addRow();
                timeChartData.setValue(rowIndex, 0, time);
                timeChartData.setValue(rowIndex, node.getColIndex(), value);

                motionData.addRow();
                motionData.setValue(rowIndex, 0, nodeId + " (" + sensorName + ")");
                motionData.setValue(rowIndex, 1, time);
                motionData.setValue(rowIndex, 2, value);
            }
            
            // put maximum in data table
            timeChartData.addColumn(ColumnType.NUMBER, "MAX");
            timeChartData.addColumn(ColumnType.NUMBER, "MIN");
            int colMax = timeChartData.getNumberOfColumns() - 2;
            int colMin = timeChartData.getNumberOfColumns() - 1;
            for (int i = 0; i<timeChartData.getNumberOfRows(); i++) {
                Date time = timeChartData.getValueDate(i, 0);
                Timestamp t = new Timestamp(time.getTime());
                double maxVal = max.get(t);
                double minVal = min.get(t);

                timeChartData.setValue(i, colMax, maxVal);
                timeChartData.setValue(i, colMin, minVal);
            }
            MyriaNode maxNode = new MyriaNode(-1, "MAX", colMax);
            maxNode.setPointCount(timeChartData.getNumberOfRows());
            nodes.put("MAX", maxNode);
            MyriaNode minNode = new MyriaNode(-1, "MIN", colMin);
            minNode.setPointCount(timeChartData.getNumberOfRows());
            nodes.put("MIN", minNode);

            showChart(timeChartData);

//            showMChart(motionData);

        } else {
            Log.w(TAG, "Zero values received!");
            showChart(null);
        }

        // put values in ListStore for grid
        if (null == this.store) {
            this.store = new ListStore<MyriaNode>();
        } else {
            this.store.removeAll();
        }
        this.store.add(new ArrayList<MyriaNode>(nodes.values()));
        this.nodeSelector.getSelectionModel().selectAll();
    }

    private void removeChart(MyriaNode m) {

        this.chart.hideDataColumns(m.getColIndex() - 1);

        this.shownCharts.remove(m);
    }

    private void showChart(DataTable data) {

        Log.d(TAG, "showChart");
        
        TabItem item = new TabItem("Time plot");
        item.setLayout(new CenterLayout());
        item.setClosable(true);
        item.setId("time");

        if ((null == data) || (data.getNumberOfRows() == 0)) {
            item.add(new Text("No data to display. Did you select the proper time range?"));
        } else {
            final int min = this.sensor.getName().equals("temperature") ? 15 : 0;
            final int max = this.sensor.getName().equals("temperature") ? 50 : 100;
            AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
            options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
            options.setMin(min);
            options.setMax(max);

            this.chart = new AnnotatedTimeLine(data, options, "800px", "600px");

            // initially hide all data from chart
            int[] cols = new int[data.getNumberOfColumns() - 1];
            for (int i = 0; i < data.getNumberOfColumns() - 1; i++) {
                cols[i] = i;
            }
//            this.chart.hideDataColumns(cols);

            item.add(this.chart);
        }

        this.chartPanel.add(item);
    }

    private void showMChart(DataTable data) {

        Log.d(TAG, "showMChart");
        
        TabItem item = new TabItem("Motion plot");
        item.setLayout(new CenterLayout());
        item.setClosable(true);
        item.setId("motion");

        if ((null == data) || (data.getNumberOfRows() == 0)) {
            item.add(new Text("No data to display. Did you select the proper time range?"));
        } else {
            final MotionChart.Options mOptions = MotionChart.Options.create();
            mOptions.setWidth(800);
            mOptions.setHeight(600);
            this.mChart = new MotionChart(data, mOptions);

            item.add(this.mChart);
        }

        this.chartPanel.add(item);
    }
}
