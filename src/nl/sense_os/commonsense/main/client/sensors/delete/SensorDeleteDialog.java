package nl.sense_os.commonsense.main.client.sensors.delete;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class SensorDeleteDialog extends View {

    private static final Logger LOG = Logger.getLogger(SensorDeleteDialog.class.getName());
    private Window window;
    private Text text;
    private Button removeButton;
    private Button cancelButton;
    private List<ExtSensor> sensors;

    public SensorDeleteDialog(Controller c) {
        super(c);
    }

    private void closeWindow() {
        setBusy(false);
        window.hide();
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorDeleteEvents.ShowDeleteDialog)) {
            LOG.fine("Show");
            final List<ExtSensor> sensors = event.<List<ExtSensor>> getData("sensors");
            onShow(sensors);

        } else if (type.equals(SensorDeleteEvents.DeleteSuccess)) {
            LOG.fine("DeleteSuccess");
            onRemoveSuccess();

        } else if (type.equals(SensorDeleteEvents.DeleteFailure)) {
            LOG.warning("DeleteFailure");
            onRemoveFailure();

        } else {
            LOG.warning("Unexpected event type");
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

        removeButton = new Button("Yes", l);
        removeButton.setIconStyle("sense-btn-icon-go");
        cancelButton = new Button("No", l);
        window.addButton(removeButton);
        window.addButton(cancelButton);
    }

    @Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeadingText("Remove sensors");
        window.setLayout(new FitLayout());
        window.setSize(323, 200);

        text = new Text();
        text.setStyleAttribute("font-size", "13px");
        text.setStyleAttribute("margin", "10px");
        window.add(text);

        initButtons();

        setBusy(false);
    }

    private void onRemoveFailure() {
        text.setText("Removal failed, retry?");
        setBusy(false);
        window.show();
    }

    private void onRemoveSuccess() {
        setBusy(false);
        closeWindow();
        MessageBox.info(null, "Removal complete.", null);
    }

    private void onShow(final List<ExtSensor> sensors) {

        this.sensors = sensors;

        String message = "Are you sure you want to remove the selected sensor from your list?";
        if (sensors.size() > 1) {
            message = "Are you sure you want to remove all " + sensors.size()
                    + " selected sensors from your list?";
        }
        message += "<br><br>";
        message += "Warning: the removal can not be undone! Any data you stored for this sensor will be lost. Forever.";

        text.setText(message);
        window.show();
        window.center();
    }

    private void remove() {
        setBusy(true);
        AppEvent delete = new AppEvent(SensorDeleteEvents.DeleteRequest);
        delete.setData("sensors", sensors);
        fireEvent(delete);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            removeButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.setEnabled(false);
        } else {
            removeButton.setIconStyle("sense-btn-icon-go");
            cancelButton.setEnabled(true);
        }
    }
}
