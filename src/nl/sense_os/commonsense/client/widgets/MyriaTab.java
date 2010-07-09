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
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.sql.Timestamp;
import java.util.ArrayList;
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

        public void setColIndex(int colIndex) {
            set("column_index", colIndex);
        }

        public void setNodeId(int nodeId) {
            set("node_id", nodeId);
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

    public MyriaTab(SensorModel sensor, long[] timeRange) {
        this.sensor = sensor;
        this.shownCharts = new HashMap<MyriaNode, AnnotatedTimeLine>();
        this.timeRange = timeRange;

        this.setLayout(new BorderLayout());

        LayoutContainer nodeSelectPanel = createNodeSelector();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        westLayout.setMargins(new Margins(5));
        this.add(nodeSelectPanel, westLayout);

        LayoutContainer centerPanel = createCenterPanel();
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));
        this.add(centerPanel, centerLayout);
    }

    private void addChart(MyriaNode m) {

        chart.showDataColumns(m.getColIndex() - 1);

        this.shownCharts.put(m, chart);
    }

    private LayoutContainer createCenterPanel() {

        TabItem foo = new TabItem("foo");

        this.chartPanel = new TabPanel();
        this.chartPanel.setPlain(true);
        this.chartPanel.add(foo);

        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new FitLayout());
        panel.setSize("100%", "100%");
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);
        panel.add(this.chartPanel, new FitData(10));

        return panel;
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
        column.setWidth(150);
        configs.add(column);

        return new ColumnModel(configs);
    }

    private LayoutContainer createNodeSelector() {

        final LayoutContainer panel = new LayoutContainer();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

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

        this.store = new ListStore<MyriaNode>();

        ContentPanel cp = new ContentPanel();
        cp.setHeading("MyriaNed nodes");
        cp.setLayout(new FitLayout());

        Grid<MyriaNode> grid = new Grid<MyriaNode>(this.store, columnMdl);
        grid.setSelectionModel(selectMdl);
        grid.addPlugin(selectMdl);

        cp.add(grid);

        panel.add(cp);

        return panel;
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
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
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

        ArrayList<MyriaNode> list = new ArrayList<MyriaNode>();

        // fill table if values are present
        if ((true == success) && (values.size() > 0)) {
            Log.d(TAG, "Received " + values.size() + " sensor values");

            DataTable motionData = DataTable.create();
            motionData.addColumn(ColumnType.STRING, "Node", "node");
            motionData.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");
            motionData.addColumn(ColumnType.NUMBER, "Value", "value");

            DataTable timeChartData = DataTable.create();
            timeChartData.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");
            timeChartData.addColumn(ColumnType.NUMBER, "Value", "value");

            // keep track of number of columns in
            HashMap<String, Integer> colIndexes = new HashMap<String, Integer>(30);
            // for (int i = 0; i < values.size(); i++) {
            // SnifferValueModel v = (SnifferValueModel) values.get(i);
            // final int nodeId = Integer.parseInt(v.getNodeId());
            // final String sensorName = v.getSensorName();
            // final double value = this.sensor.getName().equals("temperature") ? Double
            // .parseDouble(v.getValue()) / 100 : Double.parseDouble(v.getValue()) / 10;
            //
            // // look up node in list of known nodes
            // Integer colIndex = colIndexes.get(nodeId + sensorName);
            //
            // // add new column in table if node is new
            // if (colIndex == null) {
            // timeChartData.addColumn(ColumnType.NUMBER, "node " + nodeId + " (" + sensorName
            // + ")",
            // "node_" + nodeId);
            // colIndex = timeChartData.getNumberOfColumns() - 1;
            // colIndexes.put(nodeId + sensorName, colIndex);
            // }
            //
            // // add data to the table
            // final int rowIndex = timeChartData.getNumberOfRows();
            // timeChartData.addRow();
            // timeChartData.setValue(rowIndex, 1, v.getTimestamp());
            // timeChartData.setValue(rowIndex, colIndex, value);
            //
            // motionData.addRow();
            // motionData.setValue(rowIndex, 0, nodeId + " (" + sensorName + ")");
            // motionData.setValue(rowIndex, 1, v.getTimestamp());
            // motionData.setValue(rowIndex, 2, value);
            //
            // // get node
            // MyriaNode node = null;
            // for (MyriaNode mn : list) {
            // if ((mn.getNodeId() == nodeId) && (mn.getSensorName().equals(sensorName))) {
            // node = mn;
            // break;
            // }
            // }
            // if (node == null) {
            // node = new MyriaNode(nodeId, sensorName, colIndex);
            // list.add(node);
            // }
            // }

            showChart(timeChartData);

            showMChart(motionData);

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
        this.store.add(list);
    }

    private void removeChart(MyriaNode m) {

        this.chart.hideDataColumns(m.getColIndex() - 1);

        this.shownCharts.remove(m);
    }

    private void showChart(DataTable data) {
        
        Log.d(TAG, "addChart");
        
        TabItem item = new TabItem("Time plot");
        item.setLayout(new CenterLayout());
        item.setClosable(true);
        item.setId("time");

        if (null == data) {
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
            this.chart.hideDataColumns(cols);

            item.add(this.chart);
        }

        this.chartPanel.add(item);
    }

    private void showMChart(DataTable data) {
        TabItem item = new TabItem("Motion plot");
        item.setLayout(new CenterLayout());
        item.setClosable(true);
        item.setId("motion");
        
        if (null == data) {
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
