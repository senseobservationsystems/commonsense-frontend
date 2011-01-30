package nl.sense_os.commonsense.client.views;

import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class StateEditor extends View {

    private static final String TAG = "StateEditor";
    private TreeModel service;
    private ComboBox<ModelData> methodField;
    private FieldSet paramFields;
    private ListStore<ModelData> store;
    private Window window;
    private FormPanel form;

    public StateEditor(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowEditor)) {
            Log.d(TAG, "Show");
            onShow(event);
        } else if (type.equals(StateEvents.InvokeMethodComplete)) {
            Log.d(TAG, "InvokeMethodComplete");
            onInvokeComplete(event);
        } else if (type.equals(StateEvents.InvokeMethodFailed)) {
            Log.w(TAG, "InvokeMethodFailed");
            onInvokeFailed(event);
        } else if (type.equals(StateEvents.MethodsUpdated)) {
            Log.d(TAG, "MethodsUpdated");
            onMethodsUpdated(event);
        } else if (type.equals(StateEvents.MethodsNotUpdated)) {
            Log.w(TAG, "MethodsNotUpdated");
            onMethodsNotUpdated(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void onMethodsNotUpdated(AppEvent event) {
        this.store.removeAll();
    }

    private void onMethodsUpdated(AppEvent event) {
        List<ModelData> methods = event.<List<ModelData>> getData();
        this.store.removeAll();
        this.store.add(methods);
    }

    private void onInvokeFailed(AppEvent event) {
        Log.e(TAG, "onInvokeFailed not implemented");

    }

    private void onInvokeComplete(AppEvent event) {
        Log.e(TAG, "onInvokeComplete not implemented");
    }

    private void onShow(AppEvent event) {
        service = event.<TreeModel> getData();
        event.setType(StateEvents.MethodsRequested);
        Dispatcher.forwardEvent(event);

        this.store.removeAll();

        this.window.show();
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setSize(350, 200);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Set/get service parameters");

        initForm();
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setLabelAlign(LabelAlign.TOP);

        this.store = new ListStore<ModelData>();

        methodField = new ComboBox<ModelData>();
        methodField.setDisplayField("name");
        methodField.setStore(this.store);
        methodField.setFieldLabel("Select service method");

        methodField.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ModelData> se) {
                Log.d(TAG, "selectionchanged");
                ModelData method = se.getSelectedItem();
                List<String> params = method.<List<String>> get("parameters");

                FieldSet newParamFields = new FieldSet();
                FormLayout layout = new FormLayout(LabelAlign.TOP);
                layout.setLabelWidth(75);
                newParamFields.setLayout(layout);
                newParamFields.setHeading("Parameters");
                for (String param : params) {
                    TextField<String> field = new TextField<String>();
                    field.setFieldLabel(param);
                    newParamFields.add(field, new FormData());
                }

                if (null != paramFields) {
                    form.remove(paramFields);
                }
                form.add(newParamFields);
                paramFields = newParamFields;
                form.layout();
            }
        });

        form.add(methodField);

        this.window.add(form);
    }
}
