package nl.sense_os.commonsense.client.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.chap.links.client.Timeline;
import com.chap.links.client.Timeline.AddHandler;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.ajaxloader.client.Properties.TypeException;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;

public class FeedbackForm extends View {

    private static final String TAG = "FeedbackForm";
    private static final FormData FORMDATA = new FormData("-10");
    private ContentPanel panel;
    private FormPanel form;
    private final TextField<String> labelField = new TextField<String>();
    private Timeline timeline;
    private Button submitButton;
    private Button cancelButton;
    private TreeModel service;

    public FeedbackForm(Controller c) {
        super(c);
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
                    final int row = sel.get(0).getRow();

                    String title = labelField.getValue();

                    if (title != null) {
                        // apply the new title
                        timeline.getData().setValue(row, 2, title);
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
     * Returns a table filled with data
     * 
     * @return data
     */
    private DataTable createDataTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        return data;
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

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowFeedback)) {
            // Log.d(TAG, "Show");
            onShow(event);
        } else if (type.equals(StateEvents.FeedbackComplete)) {
            Log.d(TAG, "FeedbackComplete");
            timeline.draw(createDataTable());
            setBusy(false);
        } else if (type.equals(StateEvents.FeedbackFailed)) {
            Log.w(TAG, "FeedbackFailed");
            onFeedbackFailed(event);
        } else {
            Log.e(TAG, "Unexpected event type received!");
        }
    }

    private void onFeedbackFailed(AppEvent event) {
        // TODO Auto-generated method stub
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(submitButton)) {
                    submitForm();
                } else if (source.equals(cancelButton)) {
                    Dispatcher.forwardEvent(StateEvents.FeedbackCancelled);
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        this.submitButton = new Button("Submit feedback",
                IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.cancelButton = new Button("Cancel", l);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);

        FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.submitButton);
    }

    private void initFields() {
        labelField.setFieldLabel("State label");
        labelField.setAllowBlank(false);

        form.add(labelField, FORMDATA);
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setBodyBorder(false);
        this.form.setScrollMode(Scroll.AUTOY);

        initFields();
        initTimeline();
        initButtons();

        this.panel.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeaderVisible(false);
        this.panel.setBodyBorder(false);
        this.panel.setScrollMode(Scroll.NONE);

        // listener to redraw the time line when the panel is displayed, fixes width problems
        this.panel.addListener(Events.Attach, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent ce) {
                timeline.redraw();
            }
        });

        initForm();
    }

    private void initTimeline() {
        DataTable data = createDataTable();

        Timeline.Options options = Timeline.Options.create();
        options.setWidth("100%");
        options.setHeight(300);
        options.setLayout(Timeline.Options.LAYOUT.BOX);
        options.setEditable(true);

        // create the timeline, with data and options
        timeline = new Timeline(data, options);

        // add event handlers
        this.timeline.addSelectHandler(createSelectHandler(timeline));
        this.timeline.addAddHandler(createAddHandler(timeline));

        this.form.add(this.timeline, FORMDATA);
    }

    private void onShow(AppEvent event) {
        this.service = event.<TreeModel> getData("service");

        this.timeline.draw(createDataTable());
        this.form.reset();
        setBusy(false);

        AppEvent response = new AppEvent(StateEvents.FeedbackReady);
        response.setData(this.panel);
        Dispatcher.forwardEvent(response);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancelButton.disable();
        } else {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancelButton.enable();
        }
    }

    protected void submitForm() {

        setBusy(true);

        DataTable data = this.timeline.getData();

        List<ModelData> feedback = new ArrayList<ModelData>();
        for (int i = 0; i < data.getNumberOfRows(); i++) {
            Date start = data.getValueDate(i, 0);
            Date end = data.getValueDate(i, 1);
            double startDate = start.getTime() / 1000d;
            double endDate = end.getTime() / 1000d;
            // String label = data.getValueString(i, 2);

            ModelData eventData = new BaseModelData();
            eventData.set("start", startDate);
            eventData.set("end", endDate);
            // eventData.set("label", label);
            feedback.add(eventData);
        }

        AppEvent event = new AppEvent(StateEvents.FeedbackSubmit);
        event.setData("feedback", feedback);
        event.setData("service", service);
        event.setData("label", labelField.getValue());
        Dispatcher.forwardEvent(event);
    }
}