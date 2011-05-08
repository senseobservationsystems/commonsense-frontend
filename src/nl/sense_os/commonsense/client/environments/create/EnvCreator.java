package nl.sense_os.commonsense.client.environments.create;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class EnvCreator extends View {

    private static final String TAG = "EnvCreator";
    private Window window;
    private EnvCreatorForm form;
    private EnvCreatorMapPanel mapPanel;

    public EnvCreator(Controller c) {
        super(c);
    }

    private void goBack() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        if (active.equals(this.mapPanel)) {
            if (this.mapPanel.isOutlineActive()) {
                layout.setActiveItem(this.form);

            } else if (this.mapPanel.isDropperActive()) {
                this.mapPanel.showOutline();

            } else {
                Log.w(TAG, "Unexpected active component in MapPanel");
            }

        } else {
            Log.w(TAG, "Unexpected active component in window CardLayout: " + active);
        }
    }

    private void goForward() {

        CardLayout layout = (CardLayout) window.getLayout();
        Component active = layout.getActiveItem();

        if (active.equals(this.form)) {
            this.mapPanel.reset();
            layout.setActiveItem(this.mapPanel);

        } else if (active.equals(mapPanel)) {
            if (this.mapPanel.isOutlineActive()) {
                this.mapPanel.showDropper();

            } else if (this.mapPanel.isDropperActive()) {
                submit();

            } else {
                Log.w(TAG, "Unexpected active component in MapPanel");
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

        } else if (type.equals(EnvCreateEvents.Forward)) {
            goForward();

        } else if (type.equals(EnvCreateEvents.Back)) {
            goBack();

        } else if (type.equals(EnvCreateEvents.Cancel)) {
            hidePanel();

        } else if (type.equals(EnvCreateEvents.OutlineComplete)) {
            this.mapPanel.setFwdEnabled(true);

        } else if (type.equals(EnvCreateEvents.CreateSuccess)) {
            onCreateSuccess();

        } else if (type.equals(EnvCreateEvents.CreateFailure)) {
            onCreateFailure();

        } else {
            Log.w(TAG, "Unexpected event type: " + type);

        }
    }

    private void hidePanel() {
        this.window.hide();
    }

    private void initForm() {
        if (null == this.form) {
            this.form = new EnvCreatorForm();
        } else {
            this.form.reset();
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new CardLayout());
        this.window.setMinWidth(720);
        this.window.setMinHeight(444);
    }

    private void initMapPanel() {

        if (null == this.mapPanel) {
            this.mapPanel = new EnvCreatorMapPanel();
        } else {
            this.mapPanel.reset();
        }
    }

    private void onCreateFailure() {
        MessageBox.confirm(null, "Failed to store the enviroment in CommonSense! Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submit();
                        } else {
                            hidePanel();
                        }
                    }
                });
    }

    private void onCreateSuccess() {
        MessageBox.info(null, "The environment was successfully stored in CommonSense.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        hidePanel();
                    }
                });

    }

    private void resetPanel() {

        this.window.removeAll();

        initForm();
        initMapPanel();

        // do layout
        this.window.add(this.form);
        this.window.add(this.mapPanel);

        ((CardLayout) this.window.getLayout()).setActiveItem(this.form);
    }

    private void showPanel() {
        resetPanel();

        this.window.show();
        this.window.center();
    }

    private void submit() {
        fireEvent(EnvCreateEvents.CreateRequest);
    }
}
