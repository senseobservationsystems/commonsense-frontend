package nl.sense_os.commonsense.main.client.sensormanagement.publishing.component;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.ConfirmPublicationView;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class GxtConfirmPublicationDialog extends CenteredWindow implements ConfirmPublicationView {

    private Text text;
    private Button publishButton;
    private Button cancelButton;
    private CheckBox anonymous;
    private int numberOfSensors;

    private Presenter presenter;

    public GxtConfirmPublicationDialog() {

        setHeadingText("Publish sensors");
        setSize(400, 250);
        setScrollMode(Scroll.AUTOY);

        initButtons();
        initForm();

        addButton(publishButton);
        addButton(cancelButton);

        setBusy(false);
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button button = ce.getButton();
                if (button.equals(publishButton)) {
                    onPublishClick();
                } else if (button.equals(cancelButton)) {
                    onCancelClick();
                }

            }
        };

        publishButton = new Button("Yes", l);
        publishButton.setIconStyle("sense-btn-icon-go");
        cancelButton = new Button("No", l);
    }

    private void initForm() {

        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        anonymous = new CheckBox();
        anonymous.setBoxLabel("Publish anonymously");
        anonymous.setHideLabel(true);
        form.add(anonymous);

        text = new Text();
        text.setStyleAttribute("font-size", "13px");
        text.setStyleAttribute("margin", "10px");

        LayoutContainer container = new LayoutContainer();
        container.add(text);
        container.add(form);

        add(container);
    }

    @Override
    public boolean isAnonymous() {
        return anonymous.getValue().booleanValue();
    }

    private void onCancelClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    private void onPublishClick() {
        if (null != presenter) {
            presenter.onPublishClick();
        }
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            publishButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.setEnabled(false);
        } else {
            publishButton.setIconStyle("sense-btn-icon-go");
            cancelButton.setEnabled(true);
        }
    }

    @Override
    public void setNumberOfSensors(int nr) {
        numberOfSensors = nr;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show() {
        String message = "This will add a link to your sensor data on the Rotterdam Open Data Store (RODS). For more information, please go to <a href=\"http://data.rotterdamopendata.nl\" target=\"_blank\">data.rotterdamopendata.nl</a>.";
        message += "<br/><br/>";
        if (numberOfSensors > 1) {
            message += "Are you sure you want to continue with the publication of the "
                    + numberOfSensors + " selected sensors?";
        } else {
            message += "Are you sure you want to continue with the publication of the selected sensor?";
        }

        text.setText(message);

        super.show();
    }
}
