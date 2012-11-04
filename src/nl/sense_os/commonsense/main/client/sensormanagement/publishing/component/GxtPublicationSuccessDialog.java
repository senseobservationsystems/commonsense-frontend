package nl.sense_os.commonsense.main.client.sensormanagement.publishing.component;

import nl.sense_os.commonsense.main.client.sensormanagement.publishing.ConfirmPublicationView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.PublicationCompleteView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtPublicationSuccessDialog implements PublicationCompleteView {

    private Presenter presenter;
    private MessageBox messageBox;
    private String url;

    @Override
    public void hide() {
        if (null != messageBox) {
            messageBox.close();
            messageBox = null;
        }
    }

    private void onOkClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    @Override
    public void setInfo(String url, String title, String name, int[] sensorIds) {
        this.url = url;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show() {
        String msg = "Publication complete! Your data is published ";
        msg += "<a href='http://data.rotterdamopendata.nl:9090/nl/dataset/" + url
                + "' target='_blank'>here</a>.";
        messageBox = MessageBox.info("Success!", msg, new Listener<MessageBoxEvent>() {
            
            @Override
            public void handleEvent(MessageBoxEvent be) {
                onOkClick();
            }
        });
    }
}
