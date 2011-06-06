package nl.sense_os.commonsense.client.viz.panels.timeline;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.chap.links.client.Graph;
import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FillData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.events.RangeChangeHandler;

public class TimeLinePanel extends VizPanel {

    private static final Logger LOG = Logger.getLogger(TimeLinePanel.class.getName());

    private Graph graph;
    private final Graph.Options graphOpts = Graph.Options.create();

    private Timeline timeline;
    private final Timeline.Options tlineOpts = Timeline.Options.create();
    private boolean showTimeLine = true;

    /**
     * Used to enable Pim the hairy intern to skip drawing the time line.
     */
    private boolean isPimCheckComplete = false;

    public TimeLinePanel(List<SensorModel> sensors, long start, long end, String title) {
        super();

        LOG.setLevel(Level.WARNING);

        // set up layout
        setHeading("Time line: " + title);
        setBodyBorder(false);
        setLayout(new FillLayout());

        // Graph options
        graphOpts.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        graphOpts.setLineRadius(2);
        graphOpts.setWidth("100%");
        graphOpts.setHeight("100%");
        graphOpts.setLegendCheckboxes(true);
        graphOpts.setLegendWidth(125);

        // time line options
        tlineOpts.setWidth("100%");
        tlineOpts.setHeight("100%");
        tlineOpts.setAnimate(false);
        tlineOpts.setSelectable(false);
        tlineOpts.setEditable(false);
        tlineOpts.setStackEvents(false);
        tlineOpts.setGroupsOnRight(true);
        tlineOpts.setGroupsWidth(135);

        visualize(sensors, start, end);
    }

    /**
     * @return A DataTable with the correct columns for Timeline visualization.
     */
    private DataTable createDataTable(JsArray<Timeseries> data) {

        DataTable dataTable = DataTable.create();
        dataTable.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        dataTable.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        dataTable.addColumn(DataTable.ColumnType.STRING, "content");
        dataTable.addColumn(DataTable.ColumnType.STRING, "group");

        // put the time series values to the data table
        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = dataTable.getNumberOfRows(); j < values.length(); j++) {
                lastPoint = dataPoint;
                if (j == 0) {
                    dataPoint = values.get(j);
                } else {
                    dataPoint = nextPoint;
                }
                if (j < values.length() - 1) {
                    nextPoint = values.get(j + 1);
                } else {
                    nextPoint = null;
                }
                if (j > 0) {
                    if (false == (lastPoint != null && lastPoint.getRawValue().equals(
                            dataPoint.getRawValue()))) {
                        // value changed! new row...
                        dataTable.addRow();
                        index++;
                        dataTable.setValue(index, 0, dataPoint.getTimestamp());
                        dataTable.setValue(index, 2, dataPoint.getRawValue());
                        dataTable.setValue(index, 3, ts.getLabel());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    dataTable.addRow();
                    dataTable.setValue(index, 0, dataPoint.getTimestamp());
                    dataTable.setValue(index, 2, dataPoint.getRawValue());
                    dataTable.setValue(index, 3, ts.getLabel());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    dataTable.setValue(index, 1, new Date(endDate));
                } else {
                    dataTable.setValue(index, 1, new Date());
                }
            }
        }

        return dataTable;
    }

    private void createGraph(JsArray<Timeseries> numberData) {

        graph = new Graph(numberData, graphOpts);

        graph.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != timeline) {
                    timeline.setVisibleChartRange(event.getStart(), event.getEnd());
                    // timeline.redraw(); // not required
                }
            }
        });

        // this LayoutContainer ensures that the graph is sized and resized correctly
        LayoutContainer graphWrapper = new LayoutContainer() {

            @Override
            protected void onResize(int width, int height) {
                super.onResize(width, height);
                redrawGraph();
            }
        };
        graphWrapper.add(graph);

        this.add(graphWrapper, new FillData(0));
        this.layout();
    }

    private void createTimeline(DataTable table) {

        timeline = new Timeline(table, tlineOpts);

        timeline.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != graph) {
                    graph.setVisibleChartRange(event.getStart(), event.getEnd());
                    graph.redraw();
                }
            }
        });

        // this LayoutContainer ensures that the graph is sized and resized correctly
        LayoutContainer wrapper = new LayoutContainer() {
            @Override
            protected void onAfterLayout() {
                super.onAfterLayout();
                redrawTimeline();
            }

            @Override
            protected void onResize(int width, int height) {
                super.onResize(width, height);
                // redrawTimeline();
                layout(true);
            }
        };
        wrapper.add(timeline);

        this.insert(wrapper, 0, new FillData(new Margins(5, 10, 5, 70)));
        this.layout();
    }

    @Override
    protected void onNewData() {

        // special pim message
        if (!isPimCheckComplete && Registry.<UserModel> get(Constants.REG_USER).getId() == 1547) {
            MessageBox.confirm(null, "Hoi Pim! Wil je eventueel de tijdslijnvisualisatie zien?",
                    new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                                showTimeLine = true;
                            } else {
                                showTimeLine = false;
                            }
                            isPimCheckComplete = true;
                            onNewData();
                        }
                    });
            return;
        }

        LOG.fine("New data...");
        LOG.fine("Total " + data.length() + " timeseries");

        if (null == timeline && null == graph && data.length() == 0) {
            onNoData();
            return;
        }

        JsArray<Timeseries> numberData = JavaScriptObject.createArray().cast();
        JsArray<Timeseries> stringData = JavaScriptObject.createArray().cast();
        for (int i = 0; i < data.length(); i++) {
            Timeseries ts = data.get(i);
            if (ts.getType().equalsIgnoreCase("number")) {
                LOG.finest(ts.getLabel() + ": " + ts.getData().length()
                        + " data points (number data)");
                numberData.push(ts);
            } else {
                LOG.finest(ts.getLabel() + ": " + ts.getData().length() + " data points ("
                        + ts.getType() + " data)");
                stringData.push(ts);
            }
        }

        // show the string data in a time line
        if (showTimeLine && stringData.length() > 0) {
            showStringData(stringData);
        }

        // show the numerical data in a line graph
        if (numberData.length() > 0) {
            showNumberData(numberData);
        }

        // make sure both show the same time range
        if (graph != null && timeline != null) {
            Graph.DateRange graphRange = graph.getVisibleChartRange();
            Timeline.DateRange tlineRange = timeline.getVisibleChartRange();
            Date rangeStart = graphRange.getStart().before(tlineRange.getStart()) ? graphRange
                    .getStart() : tlineRange.getStart();
            Date rangeEnd = graphRange.getEnd().after(tlineRange.getEnd()) ? graphRange.getEnd()
                    : tlineRange.getEnd();
            graph.setVisibleChartRange(rangeStart, rangeEnd);
            graph.redraw();
            timeline.setVisibleChartRange(rangeStart, rangeEnd);
            timeline.redraw();
        }
    }

    private void onNoData() {
        String msg = "No data to visualize! "
                + "Please make sure that you selected a time range that contains sensor readings.";
        MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (null == graph && null == timeline) {
                    TimeLinePanel.this.hide();
                }
            }
        });
    }

    private void redrawGraph() {
        // only redraw if the graph is already drawn
        if (null != graph && graph.isAttached()) {
            graph.redraw();
        }
    }

    private void redrawTimeline() {
        // only redraw if the time line is already drawn
        if (null != timeline && timeline.isAttached()) {
            timeline.redraw();
        }
    }

    private void showNumberData(JsArray<Timeseries> data) {

        if (null == graph) {
            createGraph(data);
        } else {
            graph.draw(data, graphOpts);
        }
    }

    private void showStringData(JsArray<Timeseries> data) {

        // create a new data table
        DataTable dataTable = createDataTable(data);

        if (dataTable.getNumberOfRows() > 0) {
            if (null == timeline) {
                createTimeline(dataTable);
            } else {
                timeline.draw(dataTable);
            }
        } else {
            LOG.warning("No data for time line visualization!");
        }
    }
}