package nl.sense_os.commonsense.client.components.building;

import com.extjs.gxt.ui.client.Registry;
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
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

/**
 * @see <a href=
 *      "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 *      >blobstore image service</a>
 */
public class BuildingCreator extends ContentPanel {

    private static final String TAG = BuildingCreator.class.getName();
    private BuildingModel building;
    private final TextField<String> buildingName = new TextField<String>();
    private Button cancelBtn;
    private final ArrayList<FormPanel> floorFields = new ArrayList<FormPanel>();
    private final FormPanel form;
    private final FormData formData = new FormData();
    private ProgressBar progressBar;
    private MessageBox progressBox;
    private Button saveBtn;
    private final BuildingServiceAsync buildingService;
    private int uploadCount = 0;
    private final String userId;

    public BuildingCreator() {
        final UserModel user = Registry.<UserModel> get(Constants.REG_USER);
        if (null == user) {
            Log.e(TAG, "No user object in Registry");
            this.userId = "-1";
        } else {
            this.userId = "" + user.getId();
        }
        buildingService = Registry.<BuildingServiceAsync> get(Constants.REG_BUILDING_SVC);

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
        form.setLabelWidth(150);
        setupSubmitAction();

        form.add(buildingName, formData);
        form.add(nrOfFloors, formData);

        // add form to the main panel
        setScrollMode(Scroll.AUTOY);
        setHeaderVisible(false);
        setTopComponent(createToolBar());
        add(form);

        // add at least 1 floor
        addFloorField();
    }

    private void addFloorField() {

        // get floor number based on previous floor
        int nr = 0;
        if (floorFields.size() > 0) {
            FormPanel prevFloor = floorFields.get(floorFields.size() - 1);
            SpinnerField prevNr = (SpinnerField) prevFloor.getItemByItemId("nr");
            nr = prevNr.getValue().intValue() + 1;
        }

        // floor label field
        TextField<String> labelField = new TextField<String>();
        labelField.setFieldLabel("Floor label");
        labelField.setName("label");
        labelField.setId("label");

        // floor number field
        final SpinnerField nrField = new SpinnerField();
        nrField.setFieldLabel("Floor number");
        nrField.setPropertyEditorType(Integer.class);
        nrField.setFormat(NumberFormat.getFormat("##"));
        nrField.setAllowDecimals(false);
        nrField.setAllowBlank(false);
        nrField.setMinValue(-99);
        nrField.setIncrement(1);
        nrField.setMaxValue(99);
        nrField.setValue(nr);
        nrField.setName("nr");
        nrField.setId("nr");

        // dimensions
        TextField<Double> height = new TextField<Double>();
        height.setName("height");
        TextField<Double> width = new TextField<Double>();
        width.setName("width");
        TextField<Double> depth = new TextField<Double>();
        depth.setName("depth");
        MultiField<TextField<Double>> dims = new MultiField<TextField<Double>>();
        dims.setFieldLabel("Dimensions (HxWxD)");
        dims.setName("dimensions");
        dims.setId("dimensions");
        dims.setResizeFields(true);
        dims.setSpacing(2);
        dims.add(height);
        dims.add(width);
        dims.add(depth);

        // file upload field
        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setName("image");
        file.setFieldLabel("Image");
        file.setId("image");

        // complete the form
        FormPanel floorForm = new FormPanel();
        floorForm.setEncoding(Encoding.MULTIPART);
        floorForm.setMethod(Method.POST);
        floorForm.setHeaderVisible(false);
        floorForm.setLabelWidth(140);
        floorForm.add(labelField);
        floorForm.add(nrField);
        floorForm.add(file);
        floorForm.add(dims);

        // add floor form to main form panel
        add(floorForm, new FlowData(5));
        floorFields.add(floorForm);
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

    private void getNewBlobstoreSession() {

        // update progress
        double progress = (2d * uploadCount) / (2d * floorFields.size() + 1);
        String text = "Saving floor " + (uploadCount + 1) + "/" + floorFields.size() + "...";
        progressBar.updateProgress(progress, text);

        final String params = "?user=" + userId;
        final AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert("Failed getting floor upload URL", caught.getMessage(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                onComplete();
                            }
                        });
            }

            @Override
            public void onSuccess(String url) {

                // update progress
                double progress = (2d * uploadCount + 1) / (2d * floorFields.size() + 1);
                String text = "Saving floor " + (uploadCount + 1) + "/" + floorFields.size()
                        + "...";
                progressBar.updateProgress(progress, text);

                storeFloor(url);
            }
        };
        buildingService.getBlobstoreUploadUrl(params, callback);
    }

    private void onCancel() {
        reset();
        fireEvent(Events.CancelEdit);
    }

    private void onSave() {
        saveBtn.setText("Saving...");
        cancelBtn.setEnabled(false);

        building = new BuildingModel();
        building.setName(buildingName.getValue());
        building.setUserId(userId);

        // show progress bar
        progressBox = MessageBox.progress("Please wait", "Saving building data...", "Saving...");
        progressBar = progressBox.getProgressBar();

        // start uploading floors
        getNewBlobstoreSession();
    }

    private void onComplete() {
        reset();
        fireEvent(Events.Complete);
    }

    private void onSaveFailed(String msg) {
        MessageBox.alert("Saving failed", msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                onCancel();
            }
        });
    }

    private void removeFloorField() {
        if (floorFields.size() > 0) {
            int index = floorFields.size() - 1;
            remove(floorFields.get(index));
            floorFields.remove(index);
        }
    }

    private void reset() {

        // clear the floorFields list
        updateFloorFields(0);
        updateFloorFields(1);

        // reset the form
        uploadCount = 0;
        form.reset();

        // remove the progress box if it is shown
        if (null != progressBox) {
            progressBox.close();
        }

        saveBtn.setText("Save");
        cancelBtn.setEnabled(true);
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

    private void storeBuilding() {

        // update progress
        double progress = (2d * uploadCount) / (2d * floorFields.size() + 1);
        String text = "Saving building...";
        progressBar.updateProgress(progress, text);

        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                String msg = "Failed to store Building: " + caught.getMessage();
                onSaveFailed(msg);
            }

            @Override
            public void onSuccess(String key) {
                // set the building key
                building.setKey(key);
                Date now = new Date();
                building.setCreated(now);
                building.setModified(now);

                // done
                onComplete();
            }
        };

        buildingService.storeBuilding(building, callback);
    }

    private void storeFloor(final String url) {

        final FormPanel floorField = floorFields.get(uploadCount);
        floorField.setAction(url);

        floorField.addListener(Events.Submit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {

                // add floor to the building
                try {
                    String resultHtml = be.getResultHtml();

                    int urlStart = resultHtml.indexOf("url=") + "url".length() + 1;
                    int urlEnd = resultHtml.indexOf("&");
                    String url = resultHtml.substring(urlStart, urlEnd);

                    int keyStart = resultHtml.indexOf("key=") + "key".length() + 1;
                    int keyEnd = resultHtml.length() - 1;
                    String key = resultHtml.substring(keyStart, keyEnd);

                    if (url.length() < 10) {
                        Log.e(TAG, "Floor upload failed");

                        getNewBlobstoreSession();
                    } else {

                        // add floor data to building
                        @SuppressWarnings("unchecked")
                        TextField<String> label = (TextField<String>) floorField
                                .getItemByItemId("label");
                        SpinnerField nr = (SpinnerField) floorField.getItemByItemId("nr");
                        @SuppressWarnings("unchecked")
                        MultiField<TextField<Double>> dims = (MultiField<TextField<Double>>) floorField
                                .getItemByItemId("dimensions");
                        List<Field<?>> dimsList = dims.getAll();
                        double h = Double.parseDouble((String) dimsList.get(0).getValue());
                        double w = Double.parseDouble((String) dimsList.get(1).getValue());
                        double d = Double.parseDouble((String) dimsList.get(2).getValue());
                        
                        FloorModel floor = new FloorModel(url, nr.getValue().intValue(), label
                                .getValue(), h, w, d, userId, new Date(), new Date());
                        floor.setKey(key);
                        ArrayList<FloorModel> floors = building.getFloors();
                        floors.add(floor);
                        building.setFloors(floors);

                        uploadCount++;

                        // start upload of the next floor
                        if (floorFields.size() > uploadCount) {
                            getNewBlobstoreSession();
                        } else {
                            // no more floors to upload, store the building
                            storeBuilding();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Store floor failed: " + e.getMessage());
                    onSaveFailed(e.getMessage());
                }
            }
        });
        
        floorField.submit();
    }

    private void updateFloorFields(int newCount) {

        int oldCount = floorFields.size();
        if (oldCount > newCount) {
            // remove the unwanted extra floor(s)
            for (int i = oldCount - 1; i >= newCount; i--) {
                removeFloorField();
            }
        } else if (oldCount < newCount) {
            // add the desired extra floors
            for (int i = oldCount; i < newCount; i++) {
                addFloorField();
            }
        }

        layout();
    }
}
