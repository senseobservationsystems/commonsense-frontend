package nl.sense_os.commonsense.client.states.feedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.chap.links.client.AddHandler;
import com.chap.links.client.ChangeHandler;
import com.chap.links.client.DeleteHandler;
import com.chap.links.client.EditHandler;
import com.chap.links.client.Graph;
import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.FillData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.ajaxloader.client.Properties.TypeException;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.RangeChangeHandler;

public class FeedbackPanel extends VizPanel {

    private static final Logger LOGGER = Logger.getLogger(FeedbackPanel.class.getName());

    private final SensorModel stateSensor;
    private final LayoutContainer feedbackContainer;
    private final LayoutContainer vizContainer;
    private List<String> labels;

    private Graph graph;
    private Timeline timeline;
    private Timeline states;
    private DataTable initialStates;

    private Button submitButton;
    private Button cancelButton;

    public FeedbackPanel(SensorModel statesSensor, List<SensorModel> sensors, long start, long end,
            String title, List<String> labels) {
        super();
        LOGGER.setLevel(Level.ALL);

        this.stateSensor = statesSensor;
        this.labels = labels;

        // set up layout
        setHeading("Feedback: " + statesSensor.get("text"));
        setBodyBorder(false);
        setLayout(new RowLayout(Orientation.VERTICAL));

        sensors.add(statesSensor);
        LOGGER.finest("Start time: " + start + ", end time: " + end);
        visualize(sensors, start, end);

        this.feedbackContainer = new LayoutContainer(new FitLayout());
        this.add(this.feedbackContainer, new RowData(1, 150));
        this.vizContainer = new LayoutContainer(new FillLayout(Orientation.VERTICAL));
        this.add(this.vizContainer, new RowData(1, 1));
        createButtons();
    }
    @Override
    public void addData(JsArray<Timeseries> data) {
        LOGGER.fine("Add data...");

        JsArray<Timeseries> numberData = JsArray.createArray().cast();
        JsArray<Timeseries> stringData = JsArray.createArray().cast();
        JsArray<Timeseries> stateData = JsArray.createArray().cast();
        for (int i = 0; i < data.length(); i++) {
            Timeseries ts = data.get(i);
            if (ts.getId().equals(this.stateSensor.getId())) {
                LOGGER.fine(ts.getLabel() + " (stateSensor data)");
                stateData.push(ts);
            } else if (ts.getType().equalsIgnoreCase("number")) {
                LOGGER.fine(ts.getLabel() + " (number data)");
                numberData.push(ts);
            } else {
                LOGGER.fine(ts.getLabel() + " (" + ts.getType() + " data)");
                stringData.push(ts);
            }
        }

        // show the stateSensor data in a stateSensor time line
        showStateData(stateData);

        // show the string data in a time line
        if (stringData.length() > 0) {
            showStringData(stringData);
        }

        // show the numerical data in a line graph
        if (numberData.length() > 0) {
            showNumberData(numberData);
        }
    }

    private void addFeedbackHandlers() {

        this.states.addAddHandler(new AddHandler() {

            @Override
            public void onAdd(AddEvent event) {
                onFbAdd();
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                // LOGGER.fine( "AddHandler onEvent... " + properties);
                onAdd(null);
            }
        });

        this.states.addEditHandler(new EditHandler() {

            @Override
            public void onEdit(EditEvent event) {
                onFbEdit();
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                // LOGGER.fine( "EditHandler onEvent... " + properties);
                onEdit(null);
            }
        });

        this.states.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                onFbChange();
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                // LOGGER.fine( "ChangeHandler onEvent... " + properties);
                onChange(null);
            }
        });

        this.states.addDeleteHandler(new DeleteHandler() {

            @Override
            public void onDelete(DeleteEvent event) {
                onFbDelete();
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                // LOGGER.fine( "DeleteHandler onEvent... " + properties);
                onDelete(null);
            }
        });
    }

    private void createButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(submitButton)) {
                    submitForm();
                } else if (source.equals(cancelButton)) {
                    FeedbackPanel.this.hide();
                } else {
                    LOGGER.warning("Unexpected button pressed");
                }
            }
        };

        this.submitButton = new Button("Submit states", SenseIconProvider.ICON_BUTTON_GO, l);
        this.submitButton.setMinWidth(75);
        this.cancelButton = new Button("Cancel", l);
        this.cancelButton.setMinWidth(75);

        ButtonBar bar = new ButtonBar();
        bar.setAlignment(HorizontalAlignment.CENTER);
        bar.add(submitButton);
        bar.add(cancelButton);
        this.setBottomComponent(bar);
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
     * @return An empty DataTable with the correct columns for states visualization.
     */
    private DataTable createFbTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        return data;
    }

    private void createFeedback(DataTable table) {

        // time line options
        Timeline.Options options = Timeline.Options.create();
        options.setWidth("100%");
        options.setHeight("100%");
        options.setEditable(true);
        options.setStackEvents(true);
        options.setGroupsOnRight(true);
        options.setGroupsWidth(135);

        this.states = new Timeline(table, options);

        this.states.addRangeChangeHandler(new RangeChangeHandler() {

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
        wrapper.add(this.states);

        this.feedbackContainer.add(wrapper, new FitData(new Margins(5, 145, 5, 45)));

        addFeedbackHandlers();
    }

    private void createGraph(JsArray<Timeseries> data) {

        // Graph options
        Graph.Options options = Graph.Options.create();
        options.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        options.setLineRadius(2);
        options.setWidth("100%");
        options.setHeight("100%");
        options.setLegendCheckboxes(true);
        options.setLegendWidth(125);

        // create graph instance
        this.graph = new Graph(data, options);

        this.graph.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != timeline) {
                    timeline.setVisibleChartRange(event.getStart(), event.getEnd());
                    // timeline.redraw(); // not required
                }

                if (null != states) {
                    states.setVisibleChartRange(event.getStart(), event.getEnd());
                    // states.redraw(); // not required
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

        this.vizContainer.add(graphWrapper, new FillData(0));
        this.layout();
    }

    private void createTimeline() {

        // time line options
        Timeline.Options options = Timeline.Options.create();
        options.setWidth("100%");
        options.setHeight("100%");
        options.setAnimate(false);
        options.setSelectable(false);
        options.setEditable(false);
        options.setStackEvents(false);
        options.setGroupsOnRight(true);
        options.setGroupsWidth(135);

        DataTable table = createDataTable();
        this.timeline = new Timeline(table, options);

        this.timeline.addRangeChangeHandler(new RangeChangeHandler() {

            @Override
            public void onRangeChange(RangeChangeEvent event) {
                if (null != graph) {
                    graph.setVisibleChartRange(event.getStart(), event.getEnd());
                    graph.redraw();
                }

                if (null != states) {
                    states.setVisibleChartRange(event.getStart(), event.getEnd());
                    // states.redraw(); // not required
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

        this.vizContainer.insert(wrapper, 0, new FillData(new Margins(5, 10, 5, 45)));
        this.layout();
    }

    private void onFbAdd() {
        // LOGGER.fine( "onAdd...");
        showLabelChoice();
    }

    private void onFbChange() {
        // LOGGER.fine( "onChange...");
    }

    private void onFbDelete() {
        // LOGGER.fine( "onDelete...");
    }

    private void onFbEdit() {
        // LOGGER.fine( "onEdit...");
        showLabelChoice();
    }

    private void redrawFeedback() {
        // only redraw if the time line is already drawn
        if (null != this.states && this.states.isAttached()) {
            this.states.redraw();
        }
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

    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.submitButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
        }
    }

    private void setLabelChoice(String label) {

        // check if label is already listed
        if (!labels.contains(label)) {
            labels.add(label);
        }

        // retrieve the row number of the changed event
        JsArray<Selection> sel = states.getSelections();
        if (sel.length() > 0) {
            final int row = sel.get(0).getRow();

            if (label != null) {
                // apply the new title
                this.states.getData().setValue(row, 2, label);
                this.states.redraw();
            }
        }
    }

    private void showLabelChoice() {

        // get current label
        JsArray<Selection> sel = states.getSelections();
        String currentLabel = null;
        if (sel.length() > 0) {
            final int row = sel.get(0).getRow();
            currentLabel = this.states.getData().getValueString(row, 2);
        } else if (labels.size() > 0) {
            currentLabel = labels.get(0);
        }

        final CenteredWindow choiceWindow = new CenteredWindow();
        choiceWindow.setSize(300, 100);
        choiceWindow.setLayout(new FitLayout());
        choiceWindow.setHeading("State label selection");

        FormPanel choiceForm = new FormPanel();
        choiceForm.setHeaderVisible(false);
        choiceForm.setBodyBorder(false);

        final SimpleComboBox<String> labelCombo = new SimpleComboBox<String>();
        labelCombo.setFieldLabel("Select state");
        labelCombo.setAllowBlank(false);
        labelCombo.setTypeAhead(true);
        labelCombo.setTriggerAction(TriggerAction.ALL);
        labelCombo.add(labels);
        SimpleComboValue<String> v = labelCombo.findModel(currentLabel);
        if (v != null) {
            labelCombo.setValue(v);
        }

        choiceForm.add(labelCombo, new FormData("-10"));

        Button submitChoice = new Button("Ok", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                choiceWindow.hide();
                setLabelChoice(labelCombo.getSimpleValue());
            }
        });
        submitChoice.setMinWidth(75);

        FormButtonBinding binding = new FormButtonBinding(choiceForm);
        binding.addButton(submitChoice);

        Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                choiceWindow.hide();
            }
        });
        cancel.setMinWidth(75);

        ButtonBar bar = new ButtonBar();
        bar.setAlignment(HorizontalAlignment.CENTER);
        bar.add(submitChoice);
        bar.add(cancel);
        choiceForm.setBottomComponent(bar);

        choiceWindow.add(choiceForm);
        choiceWindow.show();
    }

    private void showNumberData(JsArray<Timeseries> data) {
        if (null == this.graph) {
            createGraph(data);
        } else {
            redrawGraph();
        }
    }

    private void showStateData(JsArray<Timeseries> data) {

        // save initial states
        this.initialStates = createFbTable();

        // clear the data table
        DataTable table = createFbTable();
        if (null != states) {
            table = this.states.getData();
            table.removeRows(0, table.getNumberOfRows());
        }

        // put the time series values to the data table
        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = table.getNumberOfRows(); j < values.length(); j++) {
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
                        index++;
                        table.addRow();
                        table.setValue(index, 0, dataPoint.getTimestamp());
                        table.setValue(index, 2, dataPoint.getRawValue());
                        this.initialStates.addRow();
                        this.initialStates.setValue(index, 0, dataPoint.getTimestamp());
                        this.initialStates.setValue(index, 2, dataPoint.getRawValue());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    table.addRow();
                    table.setValue(index, 0, dataPoint.getTimestamp());
                    table.setValue(index, 2, dataPoint.getRawValue());
                    this.initialStates.addRow();
                    this.initialStates.setValue(index, 0, dataPoint.getTimestamp());
                    this.initialStates.setValue(index, 2, dataPoint.getRawValue());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    table.setValue(index, 1, new Date(endDate));
                    this.initialStates.setValue(index, 1, new Date(endDate));
                } else {
                    table.setValue(index, 1, new Date());
                    this.initialStates.setValue(index, 1, new Date());
                }
            }
        }

        if (null == this.states) {
            createFeedback(table);
        } else {
            redrawFeedback();
        }
    }

    private void showStringData(JsArray<Timeseries> data) {

        // clear the data table
        DataTable table = createDataTable();
        if (null != this.timeline) {
            table = this.timeline.getData();
            table.removeRows(0, table.getNumberOfRows());
        }

        // put the time series values to the data table
        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = table.getNumberOfRows(); j < values.length(); j++) {
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
                        table.addRow();
                        index++;
                        table.setValue(index, 0, dataPoint.getTimestamp());
                        table.setValue(index, 2, dataPoint.getRawValue());
                        table.setValue(index, 3, ts.getLabel());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    table.addRow();
                    table.setValue(index, 0, dataPoint.getTimestamp());
                    table.setValue(index, 2, dataPoint.getRawValue());
                    table.setValue(index, 3, ts.getLabel());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    table.setValue(index, 1, new Date(endDate));
                } else {
                    table.setValue(index, 1, new Date());
                }
            }
        }

        if (table.getNumberOfRows() > 0) {
            if (null == this.timeline) {
                createTimeline();
            } else {
                redrawTimeline();
            }
        } else {
            LOGGER.warning("No data for time line visualization!");
        }
    }

    private List<FeedbackData> findChanges() {
        List<FeedbackData> changes = new ArrayList<FeedbackData>();

        DataTable endStates = this.states.getData();

        Date initialStart, finalStart, initialEnd, finalEnd;
        String initialLabel, finalLabel;
        boolean isInitialState, isFinalState;

        // check for deleted states
        for (int i = 0; i < initialStates.getNumberOfRows(); i++) {
            initialStart = this.initialStates.getValueDate(i, 0);
            initialEnd = this.initialStates.getValueDate(i, 1);
            isFinalState = false;
            for (int j = 0; j < endStates.getNumberOfRows(); j++) {
                finalStart = endStates.getValueDate(j, 0);
                finalEnd = endStates.getValueDate(j, 1);
                if (initialStart.compareTo(finalStart) == 0 && initialEnd.compareTo(finalEnd) == 0) {
                    isFinalState = true;
                    break;
                }
            }

            if (false == isFinalState) {
                changes.add(new FeedbackData(initialStart.getTime(), initialEnd.getTime(),
                        FeedbackData.TYPE_REMOVE, null));
            }
        }

        // check for changed and added states
        for (int i = 0; i < endStates.getNumberOfRows(); i++) {
            finalStart = endStates.getValueDate(i, 0);
            finalEnd = endStates.getValueDate(i, 1);
            finalLabel = endStates.getValueString(i, 2);
            isInitialState = false;
            for (int j = 0; j < this.initialStates.getNumberOfRows(); j++) {
                initialStart = this.initialStates.getValueDate(j, 0);
                initialEnd = this.initialStates.getValueDate(j, 1);
                if (initialStart.compareTo(finalStart) == 0 && initialEnd.compareTo(finalEnd) == 0) {
                    isInitialState = true;
                    initialLabel = this.initialStates.getValueString(j, 2);
                    if (!initialLabel.equals(finalLabel)) {
                        changes.add(new FeedbackData(initialStart.getTime(), initialEnd.getTime(),
                                FeedbackData.TYPE_REMOVE, null));
                        changes.add(new FeedbackData(finalStart.getTime(), finalEnd.getTime(),
                                FeedbackData.TYPE_ADD, finalLabel));
                    }
                    break;
                }
            }

            if (false == isInitialState) {
                changes.add(new FeedbackData(finalStart.getTime(), finalEnd.getTime(),
                        FeedbackData.TYPE_ADD, finalLabel));
            }
        }

        return changes;
    }

    private void submitForm() {
        setBusy(true);

        List<FeedbackData> changes = findChanges();
        AppEvent submitEvent = new AppEvent(FeedbackEvents.FeedbackSubmit);
        submitEvent.setData("state", stateSensor);
        submitEvent.setData("changes", changes);
        submitEvent.setData("panel", this);
        Dispatcher.forwardEvent(submitEvent);
    }

    public void onFeedbackComplete() {
        setBusy(false);
        MessageBox.info(null, "Feedback succesfully processed.", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                // do nothing
            }
        });
    }

    public void onFeedbackFailed() {
        setBusy(false);
        MessageBox.alert(null, "Failed to process feedback!", null);
    }
}
