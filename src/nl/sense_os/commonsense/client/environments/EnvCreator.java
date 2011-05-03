package nl.sense_os.commonsense.client.environments;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.environments.components.EnvDropper;
import nl.sense_os.commonsense.client.environments.components.EnvForm;
import nl.sense_os.commonsense.client.environments.components.EnvOutline;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class EnvCreator extends View {

    private static final String TAG = "EnvCreator";
    private Window window;
    private ButtonBar buttons;
    private EnvForm form;
    private EnvOutline outline;
    private EnvDropper dropper;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;

    public EnvCreator(Controller c) {
        super(c);
    }

    private void goBack() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        if (active.equals(dropper)) {
            layout.setActiveItem(outline);
            fwdButton.setText("Next");
            backButton.setEnabled(true);
        } else if (active.equals(outline)) {
            layout.setActiveItem(form);
            fwdButton.setText("Next");
            backButton.setEnabled(false);
        } else {
            Log.w(TAG, "Unexpected active component in CardLayout: " + active);
        }
    }

    private void goForward() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        if (active.equals(form)) {
            layout.setActiveItem(outline);
            fwdButton.setText("Next");
            backButton.setEnabled(true);
        } else if (active.equals(outline)) {
            layout.setActiveItem(dropper);
            fwdButton.setText("Finish");
            backButton.setEnabled(true);
        } else if (active.equals(dropper)) {
            hidePanel();
        } else {
            Log.w(TAG, "Unexpected active component in CardLayout: " + active);
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvEvents.ShowCreator)) {
            showPanel();
        }
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

    private void initDropper() {
        this.dropper = new EnvDropper();
    }

    private void initForm() {
        this.form = new EnvForm();
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new CardLayout());
        this.window.setMinWidth(720);
        this.window.setMinHeight(444);

        initForm();
        initOutline();
        initDropper();
        initButtons();

        // do layout
        this.window.setBottomComponent(this.buttons);
        this.window.add(this.form);
        this.window.add(this.outline);
        this.window.add(this.dropper);
    }

    private void initOutline() {
        this.outline = new EnvOutline();
    }

    private void resetPanel() {
        ((CardLayout) this.window.getLayout()).setActiveItem(form);
        this.backButton.setEnabled(false);
    }

    private void showPanel() {
        resetPanel();

        this.window.show();
        this.window.center();
    }
}
