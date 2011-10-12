package nl.sense_os.commonsense.client.alerts.create.utils;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.Image;

public class MediaButton extends CustomButton {

    public MediaButton(Image img, ClickHandler handler) {
        super(img, handler);
    }

    public MediaButton(Image img) {
        super(img);
    }
}