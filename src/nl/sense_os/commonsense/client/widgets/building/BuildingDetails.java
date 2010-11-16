package nl.sense_os.commonsense.client.widgets.building;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import java.util.ArrayList;

import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.building.BuildingModel;
import nl.sense_os.commonsense.dto.building.FloorModel;

public class BuildingDetails extends ContentPanel {

    private static final String LABEL_STYLE = "font-weight:bold;";
    private static final String TAG = BuildingDetails.class.getName();
    private BuildingModel building;
    private LabelField created = new LabelField();
    private LabelField modified = new LabelField();
    private final LayoutContainer form = new LayoutContainer();
    private final FormData formData = new FormData();
    private LabelField name = new LabelField();

    public BuildingDetails() {

        setHeaderVisible(false);
        setScrollMode(Scroll.AUTOY);
        setTopComponent(createToolBar());

        add(createBuildingData(), new FlowData(5));
    }

    private LayoutContainer createBuildingData() {
        // set up the form panel
        FormLayout layout = new FormLayout();
        form.setLayout(layout);
        form.setScrollMode(Scroll.AUTOY);

        name.setFieldLabel("Building label:");
        name.setLabelStyle(LABEL_STYLE);
        form.add(name, formData);

        created.setFieldLabel("Created:");
        created.setLabelStyle(LABEL_STYLE);
        form.add(created, formData);

        modified.setFieldLabel("Modified:");
        modified.setLabelStyle(LABEL_STYLE);
        form.add(modified, formData);

        return form;
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
        LabelField name = new LabelField(nameString);
        name.setFieldLabel("Floor label:");
        name.setLabelStyle(LABEL_STYLE);
        lc.add(name, formData);

        String nrString = "" + floor.getNumber();
        LabelField nr = new LabelField(nrString);
        nr.setFieldLabel("Floor number:");
        nr.setLabelStyle(LABEL_STYLE);
        lc.add(nr, formData);

        LayoutContainer borderLc = new LayoutContainer();
        borderLc.setBorders(true);
        borderLc.add(lc, new FlowData(5));

        return borderLc;
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
                popupImage.addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        popup.hide();
                    }
                });
            }
        });
        return image;
    }

    final Button delete = new Button();
    final Button edit = new Button();
    
    private ToolBar createToolBar() {
        delete.setText("Delete building");
        delete.addListener(Events.Select, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                if (null == building) {
                    Log.e(TAG, "No building to delete?");
                    return;
                }
                delete.setText("Deleting...");
                delete.setEnabled(false);
                
                BuildingServiceAsync service = GWT.create(BuildingService.class);
                service.deleteBuilding(building.getKey(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        delete.setText("Delete building");
                        delete.setEnabled(true);
                        
                        String msg = caught.getMessage();
                        MessageBox.alert("Failure deleting building", msg, null);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        fireEvent(Events.BeforeRemove);
                    }
                });
            }
        });
        edit.setText("Edit building");
        edit.addListener(Events.Select, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                fireEvent(Events.BeforeEdit);
            }
        });

        ToolBar toolBar = new ToolBar();
        toolBar.add(delete);
        toolBar.add(edit);

        return toolBar;
    }

    public BuildingModel getBuilding() {
        return this.building;
    }
    
    private void reset() {
        delete.setText("Delete building");
        delete.setEnabled(true);
    }
    
    public void setBuilding(BuildingModel building) {
        
        reset();
        
        // set the building label
        String nameString = building.getName();
        name.setText(nameString);

        // set the building date
        DateTimeFormat formatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        String createdString = formatter.format(building.getCreated());
        created.setText(createdString);
        String modifString = formatter.format(building.getModified());
        modified.setText(modifString);

        // remove floor details of any old buildings
        if (null != this.building) {            
            for (FloorModel floor : this.building.getFloors()) {
                form.remove(form.getItemByItemId(floor.getUrl()));
            }
        }

        // add the floor details of the new building
        ArrayList<FloorModel> floors = building.getFloors();
        for (FloorModel floor : floors) {
            LayoutContainer floorPanel =createFloorData(floor);
            floorPanel.setItemId(floor.getUrl());
            form.add(floorPanel, new FlowData(5));
        }

        this.building = building;
        
        layout();
    }
}
