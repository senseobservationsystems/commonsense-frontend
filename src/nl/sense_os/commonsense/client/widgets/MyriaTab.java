package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public class MyriaTab extends LayoutContainer {

    /*
    @SuppressWarnings("unused")
    private class MyriaNode extends BaseModel {

        private static final long serialVersionUID = 1L;

        public MyriaNode(int nodeId, String sensorName, int colIndex, String group) {
            setNodeId(nodeId);
            setSensorName(sensorName);
            setColIndex(colIndex);
            setGroup(group);
        }

        public int getColIndex() {
            return get("column_index", -1);
        }

        public String getGroup() {
            return get("group");
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

        public void setGroup(String groupName) {
            set("group", groupName);
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
    private GroupingStore<MyriaNode> store;
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

    private void updateCharts(List<MyriaNode> toShow, List<MyriaNode> toHide) {

        int[] showCols = new int[toShow.size()];
        for (int i = 0; i < toShow.size(); i++) {
            MyriaNode mn = toShow.get(i);
            showCols[i] = mn.getColIndex() - 1;
            shownCharts.put(mn, chart);
        }
        chart.showDataColumns(showCols);

        int[] hideCols = new int[toHide.size()];
        for (int i = 0; i < toHide.size(); i++) {
            MyriaNode mn = toHide.get(i);
            hideCols[i] = mn.getColIndex() - 1;
            shownCharts.remove(mn);
        }
        chart.hideDataColumns(hideCols);
    }

    private TabPanel createCenterPanel() {

        this.chartPanel = new TabPanel();
        this.chartPanel.setSize("100%", "100%");
        this.chartPanel.setPlain(true);

        return this.chartPanel;
    }

    private ColumnModel createNodeCols(CheckBoxSelectionModel<MyriaNode> selectMdl) {

        ColumnConfig group = new ColumnConfig("group", "Floor", 10);
        ColumnConfig node = new ColumnConfig("node_id", "Node ID", 60);
        ColumnConfig sensor = new ColumnConfig("sensor_name", "Sensor", 60);
        ColumnConfig count = new ColumnConfig("point_count", "# points", 90);

        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(selectMdl.getColumn());
        configs.add(group);
        configs.add(node);
        configs.add(sensor);
        configs.add(count);

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

                // add new charts
                updateCharts(toAdd, toRemove);
            }
        });

        final ColumnModel columnMdl = createNodeCols(selectMdl);

        if (null == this.store) {
            this.store = new GroupingStore<MyriaNode>();
        }

        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                String l = data.models.size() == 1 ? "Node" : "Nodes";
                return data.group + " (" + data.models.size() + " " + l + ")";
            }
        });
        this.store.groupBy("group");

        this.nodeSelector = new Grid<MyriaNode>(this.store, columnMdl);
        this.nodeSelector.setView(view);
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

    private String getGroup(int nodeId) {
        String result = "";
        switch (nodeId) {
        case 145:
            result = "1st floor";
            break;
        case 107:
        case 295:
        case 297:
        case 115:
            result = "2nd floor";
            break;
        case 193:
        case 372:
        case 131:
        case 242:
        case 144:
            result = "3nd floor";
            break;
        case 95:
        case 273:
        case 132:
        case 96:
        case 302:
            result = "4th floor";
            break;
        case 240:
        case 141:
        case 397:
        case 264:
        case 220:
            result = "5th floor";
            break;
        case 254:
        case 260:
            result = "6th floor (Roof)";
            break;
        default:
            result = "New group";
            break;
        }
        return result;
    }

    private Timestamp getNextQuarterHour(Timestamp now) {
        final long period = 10 * 60 * 1000;
        final long remainder = now.getTime() % period;
        return new Timestamp(now.getTime() - remainder + period);
    }

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

        // ArrayList<SensorValueModel> values = new ArrayList<SensorValueModel>();
        // values.add(new SnifferValueModel(new Timestamp(0 * 24 * 60 * 60 * 1000), "2000", "1",
        // "foo", ""));
        // values.add(new SnifferValueModel(new Timestamp(0 * 24 * 60 * 60 * 1000), "2500", "2",
        // "bar", ""));
        // values.add(new SnifferValueModel(new Timestamp(1 * 24 * 60 * 60 * 1000), "1500", "1",
        // "foo", ""));
        // values.add(new SnifferValueModel(new Timestamp(2 * 24 * 60 * 60 * 1000), "2000", "2",
        // "bar", ""));
        // values.add(new SnifferValueModel(new Timestamp(3 * 24 * 60 * 60 * 1000), "2200", "2",
        // "bar", ""));
        // values.add(new SnifferValueModel(new Timestamp(3 * 24 * 60 * 60 * 1000), "2200", "1",
        // "foo", ""));
        //
        // onSensorValuesReceived(true, values);
    }


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

            HashMap<Timestamp, Float> max = new HashMap<Timestamp, Float>();
            HashMap<Timestamp, Float> min = new HashMap<Timestamp, Float>();

            // keep track of number of columns in
            double totalMax = 0;
            double totalMin = 100;
            HashMap<Timestamp, Integer> rowNrs = new HashMap<Timestamp, Integer>();
            for (int i = 0; i < values.size(); i++) {
                MyriaTempValueModel v = (MyriaTempValueModel) values.get(i);
                final Timestamp time = getNextQuarterHour(v.getTimestamp());
                final int nodeId = v.getNodeId();
                final int sensorType = v.getType();
                final String sensorName = v.getSensorName();
                final float value = sensorType == SensorValueModel.MYRIA_TEMPERATURE ? v.getValue() / 100
                        : v.getValue() / 10;

                // look up node in list of known nodes
                MyriaNode node = nodes.get(nodeId + sensorName);
                if (node == null) {
                    // add new column in table if node is new
                    int colIndex = timeChartData.getNumberOfColumns();
                    timeChartData.addColumn(ColumnType.NUMBER, "node " + nodeId + " (" + sensorName
                            + ")", "node_" + nodeId);

                    node = new MyriaNode(nodeId, sensorName, colIndex, getGroup(nodeId));
                    node.setColIndex(colIndex);
                    nodes.put(nodeId + sensorName, node);

                    // Log.d(TAG, "New node: " + nodeId + " " + sensorName + ", col: " + colIndex);
                }
                node.incPointCount();

                // check if this value is the new maximum
                Float currentMax = max.get(time);
                totalMax = totalMax < value ? value : totalMax;
                if ((null == currentMax) || (currentMax.floatValue() < value)) {
                    max.put(time, value);
                }

                // check if this value is the new minimum
                Float currentMin = min.get(time);
                totalMin = totalMin > value ? value : totalMin;
                if ((null == currentMin) || (currentMin.floatValue() > value)) {
                    min.put(time, value);
                }

                // add data to the table
                Integer rowIndex = rowNrs.get(time);
                if (null == rowIndex) {
                    rowIndex = timeChartData.addRow();
                    rowNrs.put(time, rowIndex);
                }
                timeChartData.setValue(rowIndex, 0, time);
                timeChartData.setValue(rowIndex, node.getColIndex(), value);

                int motionRow = motionData.addRow();
                motionData.setValue(motionRow, 0, nodeId + " (" + sensorName + ")");
                motionData.setValue(motionRow, 1, time);
                motionData.setValue(motionRow, 2, value);
            }

            // put maximum in data table
            timeChartData.addColumn(ColumnType.NUMBER, "MAX");
            timeChartData.addColumn(ColumnType.NUMBER, "MIN");
            int colMax = timeChartData.getNumberOfColumns() - 2;
            int colMin = timeChartData.getNumberOfColumns() - 1;
            for (int i = 0; i < timeChartData.getNumberOfRows(); i++) {
                Date time = timeChartData.getValueDate(i, 0);
                Timestamp t = new Timestamp(time.getTime());
                double maxVal = max.get(t);
                double minVal = min.get(t);

                timeChartData.setValue(i, colMax, maxVal);
                timeChartData.setValue(i, colMin, minVal);
            }
            MyriaNode maxNode = new MyriaNode(-1, "MAX", colMax, "Functions");
            maxNode.setPointCount(timeChartData.getNumberOfRows());
            nodes.put("MAX", maxNode);
            MyriaNode minNode = new MyriaNode(-1, "MIN", colMin, "Functions");
            minNode.setPointCount(timeChartData.getNumberOfRows());
            nodes.put("MIN", minNode);

            showChart(timeChartData, totalMin, totalMax);

            // showMChart(motionData);

        } else {
            Log.w(TAG, "Zero values received!");
            showChart(null, 0, 0);
        }

        // put values in ListStore for grid
        if (null == this.store) {
            this.store = new GroupingStore<MyriaNode>();
        } else {
            this.store.removeAll();
        }
        this.store.add(new ArrayList<MyriaNode>(nodes.values()));
        this.store.groupBy("group");
        this.nodeSelector.getSelectionModel().selectAll();
    }

    private void showChart(DataTable data, double min, double max) {

        Log.d(TAG, "showChart");

        TabItem item = new TabItem("Time plot");
        item.setLayout(new CenterLayout());
        item.setClosable(true);
        item.setId("time");

        if ((null == data) || (data.getNumberOfRows() == 0)) {
            item.add(new Text("No data to display. Did you select the proper time range?"));
        } else {
            AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
            options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
            if (this.sensor.getName().equals("temperature")) {
                max = max > 45 ? 45 : max;
            } else if (this.sensor.getName().equals("humidity")) {
                max = max > 110 ? 110 : max;
            }
            double padding = (max - min) / 10;
            options.setMin((int) (min - padding));
            options.setMax((int) (max + padding));

            this.chart = new AnnotatedTimeLine(data, options, "800px", "600px");

            // initially hide all data from chart
            int[] cols = new int[data.getNumberOfColumns() - 1];
            for (int i = 0; i < data.getNumberOfColumns() - 1; i++) {
                cols[i] = i;
            }
            // this.chart.hideDataColumns(cols);

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
    */
}
