package nl.sense_os.commonsense.client.components.building;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
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
import java.util.List;

import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

public class BuildingEditor extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = BuildingEditor.class.getName();
    private BuildingModel building;
    private final TextField<String> buildingName = new TextField<String>();
    private Button cancelBtn;
    private FormPanel form;
    private final FormData formData = new FormData();
    private Button saveBtn;
    private final BuildingServiceAsync buildingService;

    public BuildingEditor() {

        buildingService = Registry.<BuildingServiceAsync> get(Constants.REG_BUILDING_SVC);

        setHeaderVisible(false);
        setScrollMode(Scroll.AUTOY);
        setTopComponent(createToolBar());

        // field 0: building name
        buildingName.setFieldLabel("Building label");
        buildingName.setAllowBlank(false);

        // prepare the form
        form = new FormPanel();
        form.setAction("javascript:;"); // we use an event listener to perform the real action
        form.setHeaderVisible(false);
        form.setLabelWidth(150);
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

                        int clientWidth = com.google.gwt.user.client.Window.getClientWidth();
                        int clientHeight = com.google.gwt.user.client.Window.getClientHeight();
                        int imgWidth = popupImage.getWidth();
                        int imgHeight = popupImage.getHeight();
                        
                        float clientScale = clientWidth / clientHeight;
                        float imgScale = imgWidth / imgHeight;
                        
                        if (imgScale > clientScale) {
                            if (imgWidth > clientWidth - 20) {
                                popupImage.setWidth((clientWidth - 20) + "px");
                            }
                        } else {
                            if (imgHeight > clientHeight - 20) {
                                popupImage.setHeight((clientHeight - 20) + "px");
                            }
                        }
                        
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

    /**
     * Shows the floor details, with name, number and image
     * 
     * @param floor
     *            the floor
     */
    private LayoutContainer createFloorData(final FloorModel floor) {
        
        FormPanel floorForm = new FormPanel();
        floorForm.setHeaderVisible(false);
        floorForm.setLabelWidth(140);
        
        String url = floor.getUrl();
        AdapterField imageField = new AdapterField(createImage(url));
        imageField.setFieldLabel("Floor image");
        floorForm.add(imageField, formData);

        String nameString = floor.getName();
        TextField<String> name = new TextField<String>();
        name.setValue(nameString);
        name.setFieldLabel("Floor label");
        floorForm.add(name, formData);

        SpinnerField nr = new SpinnerField();
        nr.setPropertyEditorType(Integer.class);
        nr.setFormat(NumberFormat.getFormat("##"));
        nr.setAllowDecimals(false);
        nr.setAllowBlank(false);
        nr.setMinValue(-99);
        nr.setIncrement(1);
        nr.setMaxValue(99);
        nr.setValue(floor.getNumber());
        nr.setFieldLabel("Floor number");
        floorForm.add(nr, formData);
        
        // dimensions
        TextField<Double> height = new TextField<Double>();
        height.setValue(floor.getHeight());
        TextField<Double> width = new TextField<Double>();
        width.setValue(floor.getWidth());
        TextField<Double> depth = new TextField<Double>();
        depth.setValue(floor.getDepth());
        MultiField<TextField<Double>> dims = new MultiField<TextField<Double>>();
        dims.setFieldLabel("Dimensions (HxWxD)");
        dims.setResizeFields(true);
        dims.setSpacing(2);
        dims.add(height);
        dims.add(width);
        dims.add(depth);
        floorForm.add(dims, formData);

        ContentPanel floorPanel = new ContentPanel();
        floorPanel.setHeaderVisible(false);
        floorPanel.setTopComponent(createFloorToolBar(floor));
        floorPanel.add(floorForm);        

        return floorPanel;
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

    private ToolBar createFloorToolBar(final FloorModel floor) {

        final Button addBtn = new Button("Add sensors");
        addBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                
                MyriaPositioner pos = new MyriaPositioner();
                pos.setFloor(floor);
                
                final Window window = new Window();  
                int width = com.google.gwt.user.client.Window.getClientWidth();
                int height = com.google.gwt.user.client.Window.getClientHeight();                
                window.setSize(width-100, height - 100);
                window.setLayout(new FitLayout());
                window.setModal(true);
                window.add(pos);

                pos.addListener(Events.CancelEdit, new Listener<BaseEvent>() {

                    @Override
                    public void handleEvent(BaseEvent be) {
                        window.hide();
                    }
                });
                pos.addListener(Events.Complete, new Listener<BaseEvent>() {

                    @Override
                    public void handleEvent(BaseEvent be) {
                        window.hide();
                    }
                });
                
                window.show();                
            }
        });
        
        final ToolBar bar = new ToolBar();
        bar.add(addBtn);
        
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
        buildingService.updateBuilding(building, callback);
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
            @SuppressWarnings("unchecked")
            MultiField<TextField<Double>> dims = (MultiField<TextField<Double>>) inner.getItem(3);
            List<Field<?>> dimsList = dims.getAll();
            double h = Double.parseDouble((String) dimsList.get(0).getValue());
            double w = Double.parseDouble((String) dimsList.get(1).getValue());
            double d = Double.parseDouble((String) dimsList.get(2).getValue());
            floor.setDimensions(h, w, d);
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
            form.add(floorPanel, new FlowData(0));
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
