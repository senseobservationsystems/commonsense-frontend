package nl.sense_os.commonsense.client.widgets.building;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.Date;

import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.dto.building.BuildingModel;
import nl.sense_os.commonsense.dto.building.FloorModel;

/**
 * @see <a href=
 *      "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 *      >blobstore image service</a>
 */
public class BuildingCreator extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = BuildingCreator.class.getName();
    private final TextField<String> buildingName = new TextField<String>();
    private final ArrayList<FieldSet> floorFields = new ArrayList<FieldSet>();
    private final BuildingServiceAsync service = GWT.create(BuildingService.class);
    private Button saveBtn;
    private Button cancelBtn;
    private BuildingModel building;
    private final FormPanel form;
    private final FormData formData = new FormData();
    private final String userId;

    public BuildingCreator(String userId) {
        this.userId = userId;

        // field 0: building name
        buildingName.setFieldLabel("Building label");
        buildingName.setAllowBlank(false);

        // field 1: number of floors (dynamically changes the number of floor entry fields)
        final SpinnerField nrOfFloors = new SpinnerField();
        nrOfFloors.setFieldLabel("Number of floors");
        nrOfFloors.setPropertyEditorType(Integer.class);
        nrOfFloors.setFormat(NumberFormat.getFormat("##"));
        nrOfFloors.setAllowDecimals(false);
        nrOfFloors.setAllowBlank(false);
        nrOfFloors.setMinValue(1);
        nrOfFloors.setIncrement(1);
        nrOfFloors.setMaxValue(10);
        nrOfFloors.setValue(1);
        nrOfFloors.addListener(Events.Valid, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                int newCount = nrOfFloors.getValue().intValue();
                updateFloorFields(newCount);
            }
        });

        // set up the form panel
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setLabelWidth(100);
        setupSubmitAction();

        form.add(buildingName, formData);
        form.add(nrOfFloors, formData);

        // add form to the main panel
        setHeaderVisible(false);
        setScrollMode(Scroll.AUTOY);
        setHeaderVisible(false);
        setTopComponent(createToolBar());
        add(form);

        // add at least 1 floor
        addFloorField(0);
    }

    private void addFloorField(int nr) {

        // floor label field
        TextField<String> floorName = new TextField<String>();
        floorName.setFieldLabel("Floor label");

        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setName("image");
        file.setFieldLabel("Image");

        FieldSet floor = new FieldSet();
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(90);
        formLayout.setDefaultWidth(200);
        floor.setLayout(formLayout);

        floor.add(floorName, formData);
        floor.add(file, formData);

        form.add(floor, formData);
        floorFields.add(floor);
    }

    private ToolBar createToolBar() {
        ToolBar bar = new ToolBar();

        saveBtn = new Button("Save");
        saveBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {

                if (!form.isValid()) {
                    return;
                }

                form.submit();
            }
        });
        cancelBtn = new Button("Cancel");
        cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {

                onCancel();
            }
        });

        bar.add(saveBtn);
        bar.add(cancelBtn);

        return bar;
    }

    public BuildingModel getCreatedBuilding() {
        return this.building;
    }

    private void reset() {
        // reset the form
        form.reset();

        // clear the floorFields list
        updateFloorFields(0);
        updateFloorFields(1);

        saveBtn.setText("Save");
        cancelBtn.setEnabled(true);
    }

    private void onSubmitComplete() {
        // final MessageBox box = MessageBox.progress("Please wait", "Waiting for datastore...",
        // "Initializing...");
        // final ProgressBar bar = box.getProgressBar();
        // final Timer t = new Timer() {
        // float i;
        // private static final int STEPSIZE = 1;
        //
        // @Override
        // public void run() {
        // bar.updateProgress(i / 100, (int) i + "% Complete");
        // i += STEPSIZE;
        // if (i > 100 + STEPSIZE) {
        // cancel();
        // box.close();
        reset();
        fireEvent(Events.Complete);
        // }
        // }
        // };
        // t.scheduleRepeating(500);
    }

    private void onSave() {
        saveBtn.setText("Saving...");
        cancelBtn.setEnabled(false);

        building = new BuildingModel();
        building.setName(buildingName.getValue());
        building.setUserId(userId);

        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert("Failed to store building! ", caught.getMessage(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                onSubmitComplete();
                            }
                        }).show();
            }

            @Override
            public void onSuccess(String key) {
                // set the building key
                building.setKey(key);
                building.setCreated(new Date());

                // upload the first floor
                startNewBlobstoreSession(0, key);
            }
        };

        service.storeBuilding(building, callback);
    }

    private void onCancel() {
        reset();
        fireEvent(Events.CancelEdit);
    }

    private void onUploadFailed(String msg) {
        MessageBox.alert("Upload failed", msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                onSubmitComplete();
            }
        });
    }

    private void removeFloorField(int nr) {
        form.remove(floorFields.get(nr));
        floorFields.remove(nr);
    }

    /**
     * Starts listening for the Submit event to start the RPCs with the BuildingService
     */
    private void setupSubmitAction() {
        form.setAction("javascript:;"); // we use an event listener to perform the real action
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {

                onSave();
            }
        });
    }

    private void startNewBlobstoreSession(final int nr, final String key) {
        FieldSet floorField = floorFields.get(nr);
        @SuppressWarnings("unchecked")
        final String name = ((TextField<String>) floorField.getItem(0)).getValue();

        String params = "?name=" + name + "&nr=" + nr + "&user=" + userId + "&building=" + key;

        service.getBlobstoreUploadUrl(params, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert("Failed getting floor upload URL", caught.getMessage(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                onSubmitComplete();
                            }
                        });
            }

            @Override
            public void onSuccess(String url) {
                uploadFloor(url, nr, key);
            }
        });
    }

    private void updateFloorFields(int newCount) {

        int oldCount = floorFields.size();
        if (oldCount > newCount) {
            // remove the unwanted extra floor(s)
            for (int i = oldCount - 1; i >= newCount; i--) {
                removeFloorField(i);
            }
        } else if (oldCount < newCount) {
            // add the desired extra floors
            for (int i = oldCount; i < newCount; i++) {
                addFloorField(i);
            }
        }

        layout();
    }

    private void uploadFloor(final String url, final int nr, final String key) {

        FieldSet floorField = floorFields.get(nr);
        @SuppressWarnings("unchecked")
        final String name = ((TextField<String>) floorField.getItem(0)).getValue();
        FileUploadField file = (FileUploadField) floorField.getItem(1);

        final FormPanel form = new FormPanel();
        form.setEncoding(Encoding.MULTIPART);
        form.setMethod(Method.POST);
        form.setAction(url);
        form.add(file);
        final LabelField placeholder = new LabelField("Uploading...");
        placeholder.setFieldLabel("Image:");
        floorField.add(placeholder);

        form.addListener(Events.Submit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {

                // add floor to the building
                try {
                    String url = be.getResultHtml().split("\n")[0];
                    
                    if (url.length() < 10) {
                        onUploadFailed("404");
                        return;
                    }
                    
                    FloorModel floor = new FloorModel(url, nr, name, userId, new Date(), new Date());
                    ArrayList<FloorModel> floors = building.getFloors();
                    floors.add(floor);
                    building.setFloors(floors);

                    // remove the invisible form that was submitted
                    remove(form);
                    placeholder.setText("Complete");

                    // start upload of the next floor
                    if (nr + 1 < floorFields.size()) {
                        startNewBlobstoreSession(nr + 1, key);
                    } else {
                        // no more floors to upload
                        onSubmitComplete();
                    }
                } catch (Exception e) {
                    onUploadFailed("" + be.getResultHtml());
                }
            }
        });

        // add invisible form to the layout before submitting
        form.setVisible(false);
        add(form);

        layout();

        form.submit();
    }
}
