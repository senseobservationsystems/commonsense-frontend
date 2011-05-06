package nl.sense_os.commonsense.client.environments.create;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;

public class EnvCreator extends View {

    private static final String TAG = "EnvCreator";
    private Window window;
    private ButtonBar buttons;
    private EnvForm form;
    private EnvMap map;
    private LayoutContainer mapPanel;
    private LayoutContainer mapControls;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;
    private ContentPanel outlineControls;
    private ContentPanel dropperControls;

    public EnvCreator(Controller c) {
        super(c);
    }

    private void goBack() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        if (active.equals(mapPanel)) {
            CardLayout controlLayout = (CardLayout) mapControls.getLayout();
            Component activeControls = controlLayout.getActiveItem();

            if (activeControls.equals(outlineControls)) {
                map.resetOutline();
                map.setOutlineEnabled(false);
                map.setDroppingEnabled(false);
                layout.setActiveItem(form);
                fwdButton.setText("Next");
                backButton.setEnabled(false);
            } else if (activeControls.equals(dropperControls)) {
                controlLayout.setActiveItem(outlineControls);
                map.resetMarkers();
                map.setOutlineEnabled(true);
                map.setDroppingEnabled(false);
                fwdButton.setText("Next");
                backButton.setEnabled(true);
            } else {
                Log.w(TAG, "Unexpected active component in map CardLayout: " + activeControls);
            }
        } else {
            Log.w(TAG, "Unexpected active component in window CardLayout: " + active);
        }
    }

    private void goForward() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        CardLayout controlLayout = (CardLayout) mapControls.getLayout();

        if (active.equals(form)) {
            layout.setActiveItem(mapPanel);
            controlLayout.setActiveItem(outlineControls);
            map.setOutlineEnabled(true);
            map.setDroppingEnabled(false);
            fwdButton.setText("Next");
            backButton.setEnabled(true);
        } else if (active.equals(mapPanel)) {

            Component activeControls = controlLayout.getActiveItem();

            if (activeControls.equals(outlineControls)) {
                controlLayout.setActiveItem(dropperControls);
                map.setOutlineEnabled(false);
                map.setDroppingEnabled(true);
                fwdButton.setText("Finish");
                backButton.setEnabled(true);
            } else if (activeControls.equals(dropperControls)) {
                hidePanel();
            } else {
                Log.w(TAG, "Unexpected active component in map CardLayout: " + activeControls);
            }
        } else {
            Log.w(TAG, "Unexpected active component in CardLayout: " + active);
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.ShowCreator)) {
            showPanel();

        } else if (type.equals(EnvCreateEvents.FormValid)) {
            onFormValid();

        } else if (type.equals(EnvCreateEvents.FormInvalid)) {
            onFormInvalid();

        } else {
            Log.w(TAG, "Unexpected event type: " + type);

        }
    }

    private void onFormInvalid() {
        fwdButton.setEnabled(false);
    }

    private void onFormValid() {
        fwdButton.setEnabled(true);
    }

    private void hidePanel() {
        this.window.hide();
    }

    private void initButtons() {

        this.fwdButton = new Button("Next", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                goForward();
            }
        });
        this.fwdButton.setMinWidth(75);

        this.backButton = new Button("Back", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                goBack();
            }
        });
        this.backButton.setMinWidth(75);

        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hidePanel();
            }
        });
        this.cancelButton.setMinWidth(75);

        this.buttons = new ButtonBar();
        this.buttons.setAlignment(HorizontalAlignment.RIGHT);
        this.buttons.add(this.backButton);
        this.buttons.add(this.fwdButton);
        this.buttons.add(this.cancelButton);
    }

    private void initForm() {
        this.form = new EnvForm();
        this.form.addListener(Events.Valid, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Log.d(TAG, "valid");
            }

        });
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new CardLayout());
        this.window.setMinWidth(720);
        this.window.setMinHeight(444);

        initButtons();
        this.window.setBottomComponent(this.buttons);
    }

    private void initMapPanel() {
        this.mapPanel = new LayoutContainer(new BorderLayout());

        // map widget
        this.map = new EnvMap();
        this.mapPanel.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));

        // controls
        this.mapControls = new LayoutContainer(new CardLayout());
        BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        this.mapPanel.add(this.mapControls, westLayout);

        // controls for outline stage
        this.outlineControls = new ContentPanel(new ColumnLayout());
        this.outlineControls.setHeaderVisible(false);

        Button resetOutline = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                map.resetOutline();
                map.setOutlineEnabled(true);
            }
        });
        resetOutline.setMinWidth(75);
        this.outlineControls.add(new Text("Draw the outline of the environment"));
        this.outlineControls.add(resetOutline);
        this.mapControls.add(this.outlineControls);

        // controls for dropper stage
        this.dropperControls = new ContentPanel(new ColumnLayout());
        this.dropperControls.setHeaderVisible(false);
        this.dropperControls.add(new Text("Drop sensors on the map"));
        this.mapControls.add(this.dropperControls);
    }

    private void resetPanel() {

        this.window.removeAll();

        initForm();
        initMapPanel();

        // do layout
        this.window.add(this.form);
        this.window.add(this.mapPanel);

        ((CardLayout) this.window.getLayout()).setActiveItem(form);
        this.backButton.setEnabled(false);
        this.fwdButton.setEnabled(false);
        this.fwdButton.setText("Next");
    }

    private void showPanel() {
        resetPanel();

        this.window.show();
        this.window.center();
    }
}
