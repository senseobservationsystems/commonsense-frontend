package nl.sense_os.commonsense.main.client.gxt.component;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

/**
 * Window that automatically keeps itself centered.
 */
public class CenteredWindow extends Window {

    public CenteredWindow() {
        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });
    }
}
