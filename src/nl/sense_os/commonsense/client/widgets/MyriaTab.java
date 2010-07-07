package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;

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
        DataTable data; 
        
        public MyriaNode(String nodeId) {
            setNodeId(nodeId);
            
            this.data = DataTable.create();
            this.data.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");
            this.data.addColumn(ColumnType.NUMBER, "Node ID", "node_id");
            this.data.addColumn(ColumnType.STRING, "Sensor name", "sensor");
            this.data.addColumn(ColumnType.NUMBER, "Value", "value");
            this.data.addColumn(ColumnType.NUMBER, "Variance", "variance");
        }

        public String getNodeId() {
            return get("node_id");
        }

        public void setNodeId(String nodeId) {
            set("node_id", nodeId);
        }
    }

    private static final String TAG = "MyriaTab";
    private LayoutContainer chartPanel;
    private SensorModel sensor;
    HashMap<MyriaNode, AnnotatedTimeLine> shownCharts;

    private ListStore<MyriaNode> store;
    
    public MyriaTab(SensorModel sensor) {
        this.sensor = sensor;
        this.shownCharts = new HashMap<MyriaNode, AnnotatedTimeLine>();
    }

    private LayoutContainer createChartPanel() {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new FlowLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);
        panel.setScrollMode(Scroll.AUTOY);

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
        column.setWidth(150);
        configs.add(column);

        return new ColumnModel(configs);
    }

    private LayoutContainer createNodeSelector() {

        final LayoutContainer panel = new LayoutContainer();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        // request sensor values from service
        // getSensorValues();

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
        cp.setSize(200, 300);

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
    private void getSensorValues() {

        // show progress dialog
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

        Timestamp start = new Timestamp((new Date().getTime() - (365 * 24 * 60 * 60 * 1000)));
        Timestamp end = new Timestamp(new Date().getTime());

        service.getSensorValues(this.sensor.getPhoneId(), this.sensor.getId(), start, end, callback);
    }

    private ArrayList<SensorValueModel> mockValues() {

        ArrayList<SensorValueModel> result = new ArrayList<SensorValueModel>();

        /* node 1 */
        Timestamp ts = new Timestamp(new Date().getTime() - 3 * 15 * 60 * 1000);
        String nodeId = "" + 1;
        String sensorName = "smb380";
        String value = "" + 2;
        String variance = "" + 20;
        SensorValueModel v1 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v1);
        ts = new Timestamp(new Date().getTime() - 2 * 15 * 60 * 1000);
        value = "" + 1;
        variance = "" + 10;
        SensorValueModel v2 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v2);
        ts = new Timestamp(new Date().getTime() - 1 * 15 * 60 * 1000);
        value = "" + 3;
        variance = "" + 15;
        SensorValueModel v3 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v3);

        /* node 2 */
        ts = new Timestamp(new Date().getTime() - 3 * 15 * 60 * 1000);
        nodeId = "" + 2;
        value = "" + 20;
        variance = "" + 10;
        SensorValueModel v4 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v4);
        ts = new Timestamp(new Date().getTime() - 2 * 15 * 60 * 1000);
        value = "" + 25;
        variance = "" + 10;
        SensorValueModel v5 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v5);
        ts = new Timestamp(new Date().getTime() - 1 * 15 * 60 * 1000);
        value = "" + 26;
        variance = "" + 15;
        SensorValueModel v6 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v6);

        /* node 3 */
        ts = new Timestamp(new Date().getTime() - 3 * 15 * 60 * 1000);
        nodeId = "" + 3;
        value = "" + 10;
        variance = "" + 10;
        SensorValueModel v7 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v7);
        ts = new Timestamp(new Date().getTime() - 2 * 15 * 60 * 1000);
        value = "" + 5;
        variance = "" + 10;
        SensorValueModel v8 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v8);
        ts = new Timestamp(new Date().getTime() - 1 * 15 * 60 * 1000);
        value = "" + 16;
        variance = "" + 15;
        SensorValueModel v9 = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        result.add(v9);

        return result;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        Log.d(TAG, "onRender");

        this.setLayout(new BorderLayout());

        final LayoutContainer nodeSelectPanel = createNodeSelector();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        westLayout.setMargins(new Margins(5));
        this.add(nodeSelectPanel, westLayout);

        this.chartPanel = createChartPanel();
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));
        this.add(this.chartPanel, centerLayout);

        // mock
        onSensorValuesReceived(true, mockValues());
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
        Log.d(TAG, "onSensorValuesReceived");

        ArrayList<MyriaNode> list = new ArrayList<MyriaNode>();
        
        // fill table if values are present
        if ((true == success) && (values.size() > 0)) {

            for (int i = 0; i < values.size(); i++) {
                SnifferValueModel v = (SnifferValueModel) values.get(i);

                // get node
                MyriaNode node = null;
                for (MyriaNode mn : list) {
                    if (mn.getNodeId().equals(v.getNodeId())) {
                        node = mn;
                        break;
                    }
                }
                if (node == null) {
                    node = new MyriaNode(v.getNodeId());
                } else {
                    list.remove(node);
                }

                // add row to node's data table
                node.data.addRow();
                int index = node.data.getNumberOfRows() - 1;
                node.data.setValue(index, 0, v.getTimestamp());
                node.data.setValue(index, 1, Integer.parseInt(v.getNodeId()));
                node.data.setValue(index, 2, v.getSensorName());
                node.data.setValue(index, 3, Double.parseDouble(v.getValue()));
                node.data.setValue(index, 4, Double.parseDouble(v.getVariance()));
                
                list.add(node);
            }
        } else {
            Log.w(TAG, "Zero values received!");
        }

        // put values in ListStore for grid
        if (null == this.store) {
            this.store = new ListStore<MyriaNode>();
        } else {
            this.store.removeAll();
        }
        this.store.add(list);
    }
    
    private void addChart(MyriaNode m) {
        
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setDisplayAnnotations(true);
        options.setDisplayZoomButtons(true);
        options.setScaleType(AnnotatedTimeLine.ScaleType.ALLFIXED);
        
        AnnotatedTimeLine chart = new AnnotatedTimeLine(m.data, options, "600px", "200px");
        this.chartPanel.add(chart);

        this.shownCharts.put(m, chart);
        
        this.chartPanel.layout();
    }
    
    private void removeChart(MyriaNode m) {
        AnnotatedTimeLine chart = this.shownCharts.get(m);
        
        this.chartPanel.remove(chart);
        
        this.shownCharts.remove(m);
    }
}
