package nl.sense_os.commonsense.client.sensors;

import java.util.List;

import nl.sense_os.commonsense.client.common.grid.CenteredWindow;
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

public class RemoveDialog extends View {

    private static final String TAG = "RemoveDialog";
    private Window window;
    private Text text;
    private Button removeButton;
    private Button cancelButton;
    private List<SensorModel> sensors;

    public RemoveDialog(Controller c) {
        super(c);
    }

    private void closeWindow() {
        setBusy(false);
        this.window.hide();
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorsEvents.ShowRemoveDialog)) {
            Log.d(TAG, "Show");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onShow(sensors);
        } else if (type.equals(SensorsEvents.DeleteSuccess)) {
            // Log.d(TAG, "DeleteSuccess");
            onRemoveSuccess();

        } else if (type.equals(SensorsEvents.DeleteFailure)) {
            // Log.d(TAG, "DeleteFailure");
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
        this.window.setLayout(new FitLayout());
        this.window.setSize(323, 200);
        this.window.setResizable(false);
        this.window.setPlain(true);
        this.window.setHeading("Remove sensors");
        this.window.setMonitorWindowResize(true);
        this.window.setResizable(false);

        this.text = new Text();
        this.text.setStyleAttribute("font-size", "13px");
        this.text.setStyleAttribute("margin", "2px");
        this.window.add(text);

        initButtons();

        setBusy(false);
    }

    private void onRemoveFailure() {
        this.text.setText("Removal failed, retry?");
        setBusy(false);
        this.window.show();
    }

    private void onRemoveSuccess() {
        setBusy(false);
        closeWindow();
        MessageBox.info(null, "Removal complete.", null);
    }

    private void onShow(final List<SensorModel> sensors) {

        this.sensors = sensors;

        String message = "Are you sure you want to remove the selected sensor?";
        if (sensors.size() > 1) {
            message = "Are you sure you want to remove the " + sensors.size()
                    + " selected sensors?";
        }

        this.text.setText(message);
        this.window.show();
        this.window.center();
    }

    private void remove() {
        setBusy(true);
        AppEvent delete = new AppEvent(SensorsEvents.DeleteRequest);
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
