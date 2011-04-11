package nl.sense_os.commonsense.client.sensors;

import java.util.List;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class UnshareDialog extends View {

    private static final String TAG = "UnshareDialog";
    private Window window;
    private Text text;
    private Button removeButton;
    private Button cancelButton;
    private List<SensorModel> sensors;

    public UnshareDialog(Controller c) {
        super(c);
    }

    private void closeWindow() {
        setBusy(false);
        this.window.hide();
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorsEvents.ShowUnshareDialog)) {
            Log.d(TAG, "Show");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onShow(sensors);
        } else if (type.equals(SensorsEvents.UnshareSuccess)) {
            // Log.d(TAG, "UnshareSuccess");
            onRemoveSuccess();

        } else if (type.equals(SensorsEvents.UnshareFailure)) {
            Log.w(TAG, "UnshareFailure");
            onRemoveFailure();

        } else {
            Log.w(TAG, "Unexpected event type");
        }

    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button button = ce.getButton();
                if (button.equals(removeButton)) {
                    remove();
                } else if (button.equals(cancelButton)) {
                    closeWindow();
                }

            }
        };

        this.removeButton = new Button("Yes", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.cancelButton = new Button("No", l);
        this.window.setButtonAlign(HorizontalAlignment.CENTER);
        this.window.addButton(this.removeButton);
        this.window.addButton(this.cancelButton);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Remove sensors");
        this.window.setLayout(new FitLayout());
        this.window.setSize(323, 220);

        this.text = new Text();
        this.text.setStyleAttribute("font-size", "13px");
        this.text.setStyleAttribute("margin", "10px");
        this.window.add(text);

        initButtons();

        setBusy(false);
    }

    private void onRemoveFailure() {
        setBusy(false);
        closeWindow();

        String msg = "Failed to stop sharing!";
        msg += "<br><br>";
        msg += "Please make sure that you really are the owner of the sensor you are trying to modify.";
        MessageBox.info("Failure", msg, null);
    }

    private void onRemoveSuccess() {
        setBusy(false);
        closeWindow();

        String message = "The sensor is no longer shared with this user.";
        if (sensors.size() > 1) {
            message = "The sensors are no longer shared with the selected users?";
        }
        MessageBox.info(null, message, null);
    }

    private void onShow(final List<SensorModel> sensors) {

        this.sensors = sensors;

        String message = "Are you sure you want to stop sharing the selected sensor with this user?";
        if (sensors.size() > 1) {
            message = "Are you sure you want to stop sharing the " + sensors.size()
                    + " selected sensors?";
        }
        message += "<br><br>";
        message += "The user will no longer be able to see the sensor in his or her list. The user will also not be able to access any of its data anymore.";
        message += "<br><br>";
        message += "Attention: you can only modify the sharing settings for sensors that you are the owner of.";

        this.text.setText(message);
        this.window.show();
        this.window.center();
    }

    private void remove() {
        setBusy(true);
        AppEvent delete = new AppEvent(SensorsEvents.UnshareRequest);
        delete.setData("sensors", sensors);
        fireEvent(delete);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.removeButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
        } else {
            this.removeButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
        }
    }

}
