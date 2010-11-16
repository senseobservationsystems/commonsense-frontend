package nl.sense_os.commonsense.client.widgets.building;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import java.util.ArrayList;
import java.util.Date;

import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.dto.building.BuildingModel;
import nl.sense_os.commonsense.dto.building.FloorModel;

public class BuildingEditor extends ContentPanel {

    private static final String LABEL_STYLE = "font-weight:bold;";
    @SuppressWarnings("unused")
    private static final String TAG = BuildingEditor.class.getName();
    private BuildingModel building;
    private final TextField<String> buildingName = new TextField<String>();
    private Button cancelBtn;
    private FormPanel form;
    private final FormData formData = new FormData();
    private Button saveBtn;

    public BuildingEditor() {
        setHeaderVisible(false);
        setScrollMode(Scroll.AUTOY);
        setTopComponent(createToolBar());

        // field 0: building name
        buildingName.setFieldLabel("Building label");
        buildingName.setLabelStyle(LABEL_STYLE);
        buildingName.setAllowBlank(false);

        // prepare the form
        form = new FormPanel();
        form.setAction("javascript:;"); // we use an event listener to perform the real action
        form.setHeaderVisible(false);
        form.setLabelWidth(100);
        setupSubmitAction();

        form.add(buildingName);

        add(form);
    }

    /**
     * Adds a clickable thumbnail image to the layout
     * 
     * @param url
     */
    private Image createImage(final String url) {
        Image image = new Image(url + "=s200");
        image.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final Image popupImage = new Image(url);

                final PopupPanel popup = new PopupPanel(true);
                popup.setWidget(popupImage);

                // Add some effects
                popup.setAnimationEnabled(true);
                popup.setGlassEnabled(true);
                popup.setAutoHideEnabled(true);

                // pop the image
                popup.center();

                // re-center when the dimensions are known
                popupImage.addLoadHandler(new LoadHandler() {

                    @Override
                    public void onLoad(LoadEvent event) {
                        popup.center();
                    }
                });
            }
        });
        return image;
    }

    /**
     * Shows the floor details, with name, number and image
     * 
     * @param floor
     *            the floor
     */
    private LayoutContainer createFloorData(final FloorModel floor) {
        LayoutContainer lc = new LayoutContainer(new FormLayout());

        String url = floor.getUrl();
        AdapterField imageField = new AdapterField(createImage(url));
        imageField.setFieldLabel("Floor image");
        imageField.setLabelStyle(LABEL_STYLE);
        lc.add(imageField, formData);

        String nameString = floor.getName();
        TextField<String> name = new TextField<String>();
        name.setValue(nameString);
        name.setFieldLabel("Floor label:");
        name.setLabelStyle(LABEL_STYLE);
        lc.add(name, formData);

        SpinnerField nr = new SpinnerField();
        nr.setPropertyEditorType(Integer.class);
        nr.setFormat(NumberFormat.getFormat("##"));
        nr.setAllowDecimals(false);
        nr.setAllowBlank(false);
        nr.setMinValue(-99);
        nr.setIncrement(1);
        nr.setMaxValue(99);
        nr.setValue(floor.getNumber());
        nr.setFieldLabel("Floor number:");
        nr.setLabelStyle(LABEL_STYLE);
        lc.add(nr, formData);

        LayoutContainer borderLc = new LayoutContainer();
        borderLc.setBorders(true);
        borderLc.add(lc, new FlowData(5));

        return borderLc;
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

    public BuildingModel getBuilding() {
        return this.building;
    }

    private void onCancel() {
        reset();
        fireEvent(Events.CancelEdit);
    }

    private void onSave() {
        saveBtn.setText("Saving...");
        cancelBtn.setEnabled(false);
        
        // update building object
        readFormValues();
        
        // perform update via RPC
        BuildingServiceAsync service = GWT.create(BuildingService.class);
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            
            @Override
            public void onSuccess(Void result) {
                onSaveComplete();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                String msg = "" + caught.getMessage();
                MessageBox.alert("Edit failed", msg, new Listener<MessageBoxEvent>() {
                    
                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        onCancel();
                    }
                });
            }
        };
        building.setModified(new Date());
        service.updateBuilding(building, callback);
    }
    
    private void readFormValues() {
        String name = buildingName.getValue();
        building.setName(name);
        
        ArrayList<FloorModel> floors = building.getFloors();
        for (int i = 0; i < floors.size(); i++) {
            FloorModel floor = floors.get(i);
            LayoutContainer outer = (LayoutContainer) form.getItemByItemId(floor.getUrl());
            LayoutContainer inner = (LayoutContainer) outer.getItem(0);
                        
            @SuppressWarnings("unchecked")
            TextField<String> floorName = (TextField<String>) inner.getItem(1);
            SpinnerField floorNr = (SpinnerField) inner.getItem(2);
            
            floor.setName(floorName.getValue());
            floor.setNumber(floorNr.getValue().intValue());
            floors.set(i, floor);
        }
        building.setFloors(floors);
    }

    private void onSaveComplete() {
        reset();
        fireEvent(Events.Complete);
    }

    private void reset() {
        saveBtn.setText("Save");
        cancelBtn.setEnabled(true);
        form.reset();
    }

    public void setBuilding(BuildingModel building) {
        buildingName.setValue(building.getName());

        // remove floor details of any old buildings
        if (null != this.building) {
            for (FloorModel floor : this.building.getFloors()) {
                form.remove(form.getItemByItemId(floor.getUrl()));
            }
        }

        // add the floor details of the new building
        ArrayList<FloorModel> floors = building.getFloors();
        for (FloorModel floor : floors) {
            LayoutContainer floorPanel = createFloorData(floor);
            floorPanel.setItemId(floor.getUrl());
            form.add(floorPanel, new FlowData(5));
        }

        this.building = building;

        layout();
    }

    /**
     * Starts listening for the Submit event to start the RPCs with the BuildingService
     */
    private void setupSubmitAction() {
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {

                onSave();
            }
        });
    }
}
