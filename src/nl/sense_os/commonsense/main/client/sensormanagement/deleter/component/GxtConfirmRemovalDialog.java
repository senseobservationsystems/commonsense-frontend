package nl.sense_os.commonsense.main.client.sensormanagement.deleter.component;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.sensormanagement.deleter.ConfirmRemovalView;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class GxtConfirmRemovalDialog extends CenteredWindow implements ConfirmRemovalView {

    private Presenter presenter;

    private Text confirmation;
    private Button deleteButton;
    private Button cancelButton;

    public GxtConfirmRemovalDialog() {
        setHeading("Remove sensors");
        setLayout(new FitLayout());
        setSize(323, 200);

        confirmation = new Text();
        confirmation.setStyleAttribute("font-size", "13px");
        confirmation.setStyleAttribute("margin", "10px");
        add(confirmation);

        initButtons();

        addButton(deleteButton);
        addButton(cancelButton);

        setBusy(false);
    }

    private void initButtons() {

        deleteButton = new Button("Yes", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onDeleteClick();
                }
            }
        });
        deleteButton.setIconStyle("sense-btn-icon-go");

        cancelButton = new Button("No", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            deleteButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.setEnabled(false);
        } else {
            deleteButton.setIconStyle("sense-btn-icon-go");
            cancelButton.setEnabled(true);
        }
    }

    @Override
    public void setConfirmationText(String text) {
        this.confirmation.setText(text);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
