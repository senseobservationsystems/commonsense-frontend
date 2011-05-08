package nl.sense_os.commonsense.client.environments.create;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;

public class EnvCreatorMapPanel extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "EnvCreatorMapPanel";
    private ButtonBar buttons;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;
    private EnvMap map;
    private LayoutContainer mapControls;
    private ContentPanel outlineControls;
    private ContentPanel dropperControls;

    public EnvCreatorMapPanel() {
        this.setLayout(new BorderLayout());
        this.setHeaderVisible(false);

        initMap();
        initControls();

        this.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));
        BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        this.add(this.mapControls, westLayout);

        // add bottom buttons
        initButtons();
        this.setBottomComponent(this.buttons);
    }

    private void initMap() {

        this.map = new EnvMap();
    }

    private void initControls() {

        this.mapControls = new LayoutContainer(new CardLayout());

        // controls for outline stage
        this.outlineControls = new ContentPanel(new ColumnLayout());
        this.outlineControls.setHeaderVisible(false);

        Button resetOutline = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
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

    /**
     * Initializes the Back/Forward/Cancel buttons for the creator wizard. The buttons trigger
     * events that are handled by {@link EnvCreator}.
     */
    private void initButtons() {

        // forward button
        this.fwdButton = new Button("Next", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Forward);
            }
        });
        this.fwdButton.setMinWidth(75);
        this.fwdButton.setEnabled(false);

        // back button
        this.backButton = new Button("Back", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Back);
            }
        });
        this.backButton.setMinWidth(75);

        // cancel button
        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Cancel);
            }
        });
        this.cancelButton.setMinWidth(75);

        this.buttons = new ButtonBar();
        this.buttons.setAlignment(HorizontalAlignment.RIGHT);
        this.buttons.add(this.backButton);
        this.buttons.add(this.fwdButton);
        this.buttons.add(this.cancelButton);
    }

    public void showDropper() {
        ((CardLayout) this.mapControls.getLayout()).setActiveItem(this.dropperControls);
        this.map.setOutlineEnabled(false);
        this.map.setDroppingEnabled(true);
        this.fwdButton.setText("Finish");
    }

    public void showOutline() {
        ((CardLayout) this.mapControls.getLayout()).setActiveItem(this.outlineControls);
        this.map.resetSensors();
        this.map.setOutlineEnabled(true);
        this.map.setDroppingEnabled(false);
        this.fwdButton.setText("Next");
    }

    public boolean isDropperActive() {
        return ((CardLayout) this.mapControls.getLayout()).getActiveItem().equals(
                this.dropperControls);
    }

    public boolean isOutlineActive() {
        return ((CardLayout) this.mapControls.getLayout()).getActiveItem().equals(
                this.outlineControls);
    }

    public void reset() {
        this.map.resetSensors();
        this.map.resetOutline();
        this.fwdButton.setEnabled(false);
        showOutline();
    }

    public void setFwdEnabled(boolean enabled) {
        this.fwdButton.setEnabled(enabled);
    }
}
