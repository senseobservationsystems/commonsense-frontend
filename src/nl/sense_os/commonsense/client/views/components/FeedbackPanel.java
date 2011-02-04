package nl.sense_os.commonsense.client.views.components;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.chap.links.client.Timeline;
import com.chap.links.client.Timeline.AddHandler;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.ajaxloader.client.Properties.TypeException;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.RangeChangeHandler;
import com.google.gwt.visualization.client.events.SelectHandler;

public class FeedbackPanel extends VisualizationTab {

    private static final String TAG = "FeedbackPanel";
    private final TextBox txtStartDate = new TextBox();
    private final TextBox txtEndDate = new TextBox();
    private final Button btnSetRange = new Button("Set");
    private final CheckBox chkConfirmChange = new CheckBox("Confirm changes");
    private Timeline timeline;

    public FeedbackPanel() {
        super();

        DataTable data = createDataTable();

        Timeline.Options options = Timeline.Options.create();
        options.setWidth("100%");
        options.setHeight("100%");
        options.setLayout(Timeline.Options.LAYOUT.BOX);
        options.setEditable(true);

        // create the timeline, with data and options
        timeline = new Timeline(data, options);

        // add event handlers
        timeline.addSelectHandler(createSelectHandler(timeline));
        timeline.addRangeChangeHandler(createRangeChangeHandler(timeline));
        timeline.addChangeHandler(createChangeHandler(timeline));
        timeline.addAddHandler(createAddHandler(timeline));
    }

    /**
     * create an add handler (this event occurs when the user creates a new event).
     * 
     * @param timeline
     * @return
     */
    private Timeline.AddHandler createAddHandler(final Timeline timeline) {
        AddHandler h = new Timeline.AddHandler() {
            @Override
            public void onAdd(AddEvent event) {
                // retrieve the row number of the changed event
                JsArray<Selection> sel = timeline.getSelections();
                if (sel.length() > 0) {
                    int row = sel.get(0).getRow();

                    boolean confirmChanges = chkConfirmChange.getValue();

                    if (confirmChanges == false) {
                        String info = "Add event " + String.valueOf(row) + " applied";
                        Log.d(TAG, info);
                        return;
                    }

                    // request confirmation
                    String title = Window.prompt("Enter a title for the new event", "New event");

                    if (title != null) {
                        // apply the new title
                        timeline.getData().setValue(row, 2, title);

                        String info = "Add event " + String.valueOf(row) + " applied";
                        Log.d(TAG, info);
                    } else {
                        // cancel creating new event
                        timeline.cancelAdd();
                        String info = "Add event " + String.valueOf(row) + " cancelled";
                        Log.d(TAG, info);
                    }
                }
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                onAdd(new AddEvent());
            }

        };
        return h;
    }

    /**
     * create a change handler (this event occurs when the user changes the position of an event by
     * dragging it).
     * 
     * @param timeline
     * @return
     */
    private Timeline.ChangeHandler createChangeHandler(final Timeline timeline) {
        return new Timeline.ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                // retrieve the row number of the changed event
                JsArray<Selection> sel = timeline.getSelections();
                if (sel.length() > 0) {
                    int row = sel.get(0).getRow();

                    boolean confirmChanges = chkConfirmChange.getValue();

                    // request confirmation
                    boolean applyChange = confirmChanges ? Window
                            .confirm("Are you sure you want to change this event?") : true;

                    if (applyChange) {
                        String info = "Change event " + String.valueOf(row) + " changed";
                        Log.d(TAG, info);
                    } else {
                        // cancel the change
                        timeline.cancelChange();

                        String info = "Change event " + String.valueOf(row) + " cancelled";
                        Log.d(TAG, info);
                    }
                }
            }

            @Override
            protected void onEvent(Properties properties) throws TypeException {
                onChange(new ChangeEvent());
            }
        };
    }

    /**
     * Returns a table filled with data
     * 
     * @return data
     */
    private DataTable createDataTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

        int n = 0;

        data.addRow();
        data.setValue(n, 0, df.parse("2010-08-23"));
        data.setValue(n, 2, "Conversation");
        n++;

        data.addRow();
        data.setValue(n, 0, dtf.parse("2010-08-23 23:00:00"));
        data.setValue(n, 2, "Mail from boss");
        n++;

        data.addRow();
        data.setValue(n, 0, dtf.parse("2010-08-24 16:00:00"));
        data.setValue(n, 2, "Report");
        n++;

        data.addRow();
        data.setValue(n, 0, df.parse("2010-08-26"));
        data.setValue(n, 1, df.parse("2010-09-02"));
        data.setValue(n, 2, "Traject A");
        n++;

        data.addRow();
        data.setValue(n, 0, df.parse("2010-08-28"));
        data.setValue(n, 2, "Memo");
        n++;

        data.addRow();
        data.setValue(n, 0, df.parse("2010-08-29"));
        data.setValue(n, 2, "Phone call");
        n++;

        data.addRow();
        data.setValue(n, 0, df.parse("2010-08-31"));
        data.setValue(n, 1, df.parse("2010-09-03"));
        data.setValue(n, 2, "Traject B");
        n++;

        data.addRow();
        data.setValue(n, 0, dtf.parse("2010-09-04 12:00:00"));
        data.setValue(n, 2, "Report");
        n++;

        return data;
    }

    /**
     * create a RangeChange handler (this event occurs when the user changes the visible range by
     * moving or scrolling the Timeline).
     * 
     * @param timeline
     * @return
     */
    private RangeChangeHandler createRangeChangeHandler(final Timeline timeline) {
        return new RangeChangeHandler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                // getRange();
            }
        };
    }

    /**
     * add a select handler (the select event occurs when the user clicks on an event)
     * 
     * @param timeline
     * @return
     */
    private SelectHandler createSelectHandler(final Timeline timeline) {
        return new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                JsArray<Selection> sel = timeline.getSelections();

                if (sel.length() > 0) {
                    int row = sel.get(0).getRow();
                    String info = "Select event " + String.valueOf(row) + " selected";
                    Log.d(TAG, info);
                } else {
                    String info = "Select event &lt;nothing&gt; selected";
                    Log.d(TAG, info);
                }
            }
        };
    }

    /**
     * Get the range from the timeline and put it in the textboxes on screen
     */
    private void getRange() {
        Timeline.DateRange range = timeline.getVisibleChartRange();
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

        // set the new startdate and enddate
        txtStartDate.setText(dtf.format(range.getStart()));
        txtEndDate.setText(dtf.format(range.getEnd()));
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        // remove empty text message
        Component emptyText = this.getItemByItemId("empty_text");
        if (null != emptyText) {
            this.remove(emptyText);
        }

        // add(txtStartDate, new RowData(-1, -1, new Margins(5)));
        // add(txtEndDate, new RowData(-1, -1, new Margins(5)));
        // add(btnSetRange, new RowData(-1, -1, new Margins(5)));
        // add(chkConfirmChange, new RowData(-1, -1, new Margins(5)));
        add(timeline, new RowData(-1, 0.8, new Margins(5)));

        // getRange();
    }

    /**
     * Get the entered dates from the textboxes on screen, and apply them to the timeline
     */
    private void setRange() {
        DateTimeFormat datetime = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormat date = DateTimeFormat.getFormat("yyyy-MM-dd");

        Date startDate;
        Date endDate;

        // Try to parse the startdate
        try {
            startDate = datetime.parse(txtStartDate.getText());
        } catch (IllegalArgumentException err) {
            try {
                startDate = date.parse(txtStartDate.getText());
            } catch (IllegalArgumentException err2) {
                MessageBox
                        .alert(null, "I don't understand the startdate that you entered :(", null);
                return;
            }
        }

        // Try to parse the enddate
        try {
            endDate = datetime.parse(txtEndDate.getText());
        } catch (IllegalArgumentException err) {
            try {
                endDate = date.parse(txtEndDate.getText());
            } catch (IllegalArgumentException err2) {
                MessageBox.alert(null, "I cannot make sense of the enddate that you entered :(",
                        null);
                return;
            }
        }

        timeline.setVisibleChartRange(startDate, endDate);
        timeline.redraw();
    }

    @Override
    public void addData(TaggedDataModel taggedData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addData(List<TaggedDataModel> taggedDatas) {
        // TODO Auto-generated method stub

    }
}