package nl.sense_os.commonsense.client.states.feedback;

import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.panels.VizPanel;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.chap.links.client.AddHandler;
import com.chap.links.client.Graph;
import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FillData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.ajaxloader.client.Properties.TypeException;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.RangeChangeHandler;

import java.util.Date;
import java.util.List;

public class FeedbackPanel extends VizPanel {

    private static final String TAG = "FeedbackPanel";
    private Graph graph;
    private Timeline timeline;
    private final Graph.Options graphOpts;
    private final Timeline.Options tlineOpts;
    private final DataTable dataTable;

    private Timeline feedback;
    private final Timeline.Options fbOpts;
    private final DataTable fbTable;

    private Button submitButton;
    private Button cancelButton;

    public FeedbackPanel(SensorModel state, List<SensorModel> sensors, String title) {
        super();

        // set up layout
        setHeading("Feedback: " + state.get("text"));
        setBodyBorder(false);
        setLayout(new FillLayout());

        this.graphOpts = Graph.Options.create();
        this.tlineOpts = Timeline.Options.create();
        this.fbOpts = Timeline.Options.create();
        this.dataTable = createDataTable();
        this.fbTable = createFbTable();

        long start = System.currentTimeMillis() - (1000 * 60 * 60 * 24);
        long end = System.currentTimeMillis();
        visualize(sensors, start, end);

        createFeedback();
        createButtons();
    }

    @Override
    public void addData(JsArray<Timeseries> data) {
        // Log.d(TAG, "addData...");

        if (data.length() == 0) {
            onNoData();
            return;
        }

        JsArray<Timeseries> numberData = JsArray.createArray().cast();
        JsArray<Timeseries> stringData = JsArray.createArray().cast();
        for (int i = 0; i < data.length(); i++) {
            Timeseries ts = data.get(i);
            if (ts.getType().equalsIgnoreCase("number")) {
                // Log.d(TAG, ts.getLabel() + " (number data)");
                numberData.push(ts);
            } else {
                // Log.d(TAG, ts.getLabel() + " (" + ts.getType() + " data)");
                stringData.push(ts);
            }
        }

        // show the string data in a time line
        if (stringData.length() > 0) {
            showStringData(stringData);
        }

        // show the numerical data in a line graph
        if (numberData.length() > 0) {
            showNumberData(numberData);
        }
    }

    /**
     * @return An empty DataTable with the correct columns for Timeline visualization.
     */
    private DataTable createDataTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");
        data.addColumn(DataTable.ColumnType.STRING, "group");

        return data;
    }

    /**
     * @return An empty DataTable with the correct columns for feedback visualization.
     */
    private DataTable createFbTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        return data;
    }

    private void createFeedback() {

        // time line options
        this.fbOpts.setWidth("100%");
        this.fbOpts.setHeight("100%");
        this.fbOpts.setEditable(true);

        this.feedback = new Timeline(this.fbTable, this.fbOpts);

        this.feedback.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != timeline) {
                    timeline.setVisibleChartRange(event.getStart(), event.getEnd());
                    // timeline.redraw(); // not required
                }

                if (null != graph) {
                    graph.setVisibleChartRange(event.getStart(), event.getEnd());
                    graph.redraw();
                }
            }
        });

        this.feedback.addAddHandler(new AddHandler() {

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                onAdd(null);
            }

            @Override
            public void onAdd(AddEvent event) {
                Log.d(TAG, "onAdd");

                // retrieve the row number of the changed event
                JsArray<Selection> sel = feedback.getSelections();
                if (sel.length() > 0) {
                    final int row = sel.get(0).getRow();

                    String title = "foo";

                    if (title != null) {
                        // apply the new title
                        feedback.getData().setValue(row, 2, title);
                    }
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
        wrapper.add(this.feedback);

        this.add(wrapper, new FillData(new Margins(5, 145, 5, 145)));
    }

    private void createGraph(JsArray<Timeseries> data) {

        // Graph options
        this.graphOpts.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        this.graphOpts.setLineRadius(2);
        this.graphOpts.setWidth("100%");
        this.graphOpts.setHeight("100%");
        this.graphOpts.setLegendCheckboxes(true);
        this.graphOpts.setLegendWidth(125);

        // create graph instance
        this.graph = new Graph(data, this.graphOpts);

        this.graph.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != timeline) {
                    timeline.setVisibleChartRange(event.getStart(), event.getEnd());
                    // timeline.redraw(); // not required
                }

                if (null != feedback) {
                    feedback.setVisibleChartRange(event.getStart(), event.getEnd());
                    // feedback.redraw(); // not required
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
        graphWrapper.add(this.graph);

        this.add(graphWrapper, new FillData(0));
        this.layout();
    }

    private void createTimeline() {

        // time line options
        this.tlineOpts.setWidth("100%");
        this.tlineOpts.setHeight("100%");
        this.tlineOpts.setAnimate(false);
        this.tlineOpts.setSelectable(false);
        this.tlineOpts.setEditable(false);
        this.tlineOpts.setStackEvents(false);

        this.timeline = new Timeline(this.dataTable, this.tlineOpts);

        this.timeline.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != graph) {
                    graph.setVisibleChartRange(event.getStart(), event.getEnd());
                    graph.redraw();
                }

                if (null != feedback) {
                    feedback.setVisibleChartRange(event.getStart(), event.getEnd());
                    // feedback.redraw(); // not required
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
        wrapper.add(this.timeline);

        this.insert(wrapper, 1, new FillData(new Margins(5, 145, 5, 5)));
        this.layout();
    }

    private void createButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(submitButton)) {
                    submitForm();
                } else if (source.equals(cancelButton)) {
                    hide();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        this.submitButton = new Button("Submit feedback",
                IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.cancelButton = new Button("Cancel", l);

        ButtonBar bar = new ButtonBar();
        bar.setAlignment(HorizontalAlignment.CENTER);
        bar.add(submitButton);
        bar.add(cancelButton);
        this.setBottomComponent(bar);
    }

    protected void submitForm() {
        Log.d(TAG, "submit!");
    }

    private void onNoData() {
        String msg = "No data to visualize! "
                + "Please make sure that you selected a time range that contains sensor readings.";
        MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (null == graph && null == timeline) {
                    FeedbackPanel.this.hide();
                }
            }
        });
    }

    private void redrawGraph() {
        // only redraw if the graph is already drawn
        if (null != this.graph && this.graph.isAttached()) {
            this.graph.redraw();
        }
    }

    private void redrawTimeline() {
        // only redraw if the time line is already drawn
        if (null != this.timeline && this.timeline.isAttached()) {
            this.timeline.redraw();
        }
    }

    private void showNumberData(JsArray<Timeseries> data) {
        if (null == this.graph) {
            createGraph(data);
        } else {
            redrawGraph();
        }
    }

    private void showStringData(JsArray<Timeseries> data) {

        // clear the data table
        this.dataTable.removeRows(0, this.dataTable.getNumberOfRows());

        // put the time series values to the data table
        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = this.dataTable.getNumberOfRows(); j < values.length(); j++) {
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
                        this.dataTable.addRow();
                        index++;
                        this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                        this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                        this.dataTable.setValue(index, 3, ts.getLabel());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    this.dataTable.addRow();
                    this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                    this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                    this.dataTable.setValue(index, 3, ts.getLabel());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    this.dataTable.setValue(index, 1, new Date(endDate));
                } else {
                    this.dataTable.setValue(index, 1, new Date());
                }
            }
        }

        if (dataTable.getNumberOfRows() > 0) {
            if (null == this.timeline) {
                createTimeline();
            } else {
                redrawTimeline();
            }
        } else {
            Log.w(TAG, "No data for time line visualization!");
        }
    }
}
