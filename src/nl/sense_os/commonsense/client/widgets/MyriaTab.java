package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;

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
        
        public MyriaNode(int nodeId, int colIndex) {
            setNodeId(nodeId);
            setColIndex(colIndex);
        }

        public int getColIndex() {
            return get("column_index", -1);
        }
        
        public int getNodeId() {
            return get("node_id", -1);
        }

        public void setColIndex(int colIndex) {
            set("column_index", colIndex);
        }
        
        public void setNodeId(int nodeId) {
            set("node_id", nodeId);
        }
    }

    private static final String TAG = "MyriaTab";
    private AnnotatedTimeLine chart;
    private LayoutContainer chartPanel;
    private SensorModel sensor;
    private HashMap<MyriaNode, AnnotatedTimeLine> shownCharts;
    private ListStore<MyriaNode> store;
    private long[] timeRange;

    public MyriaTab(SensorModel sensor, long[] timeRange) {
        this.sensor = sensor;
        this.shownCharts = new HashMap<MyriaNode, AnnotatedTimeLine>();
        this.timeRange = timeRange;
    }

    private void addChart(MyriaNode m) {       

        chart.showDataColumns(m.getColIndex() -1);
        
        this.shownCharts.put(m, chart);
    }

    private LayoutContainer createCenterPanel() {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        this.chartPanel = new VerticalPanel();
        this.chartPanel.setScrollMode(Scroll.AUTOY);
        this.chartPanel.setLayoutOnChange(true);

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

        Timestamp start = new Timestamp(timeRange[0]);
        Timestamp end = new Timestamp(timeRange[1]);

        service.getSensorValues(this.sensor.getPhoneId(), this.sensor.getId(), start, end, callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

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
            Log.d(TAG, "Received " + values.size() + " values");

            DataTable data = DataTable.create();
            data.addColumn(ColumnType.DATETIME, "Date/Time", "timestamp");
            
            // keep track of number of columns in
            HashMap<Integer, Integer> colIndexes = new HashMap<Integer, Integer>(30);
            for (int i = 0; i < values.size(); i++) {
                SnifferValueModel v = (SnifferValueModel) values.get(i);
                final int nodeId = Integer.parseInt(v.getNodeId());
                final double value = Double.parseDouble(v.getValue()) / 100;

                // look up node in list of known nodes
                Integer colIndex = colIndexes.get(nodeId);

                // add new column in table if node is new
                if (colIndex == null) {
                    data.addColumn(ColumnType.NUMBER, "MyriaNed node #" + nodeId, "node_"
                            + nodeId);
                    colIndex = data.getNumberOfColumns() - 1;
                    colIndexes.put(nodeId, colIndex);
                }

                // add data to the table
                data.addRow();
                data.setValue(data.getNumberOfRows() - 1, colIndex, value);
                data.setValue(data.getNumberOfRows() - 1, 0, v.getTimestamp());

                // get node
                MyriaNode node = null;
                for (MyriaNode mn : list) {
                    if (mn.getNodeId() == nodeId) {
                        node = mn;
                        break;
                    }
                }
                if (node == null) {
                    node = new MyriaNode(nodeId, colIndex);
                    list.add(node);
                }
            }  
            
            showChart(data);
            
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

    private void removeChart(MyriaNode m) {

        this.chart.hideDataColumns(m.getColIndex() -1);
        
        this.shownCharts.remove(m);
    }
    
    private void showChart(DataTable data) {

        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
        options.setMin(15);
        options.setMax(50);

        this.chart = new AnnotatedTimeLine(data, options, "800px", "600px");
        this.chartPanel.add(this.chart, new TableData(HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE));
        
        // initially hide all data from chart
        int[] cols = new int[data.getNumberOfColumns()-1];
        for (int i = 0; i < data.getNumberOfColumns() - 1; i++) {
            cols[i] = i;
        }
        this.chart.hideDataColumns(cols);
    }
}
