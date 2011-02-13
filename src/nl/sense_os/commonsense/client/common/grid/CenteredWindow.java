package nl.sense_os.commonsense.client.common.grid;

import com.extjs.gxt.ui.client.widget.Window;

/**
 * Window that automatically keeps itself centered.
 */
public class CenteredWindow extends Window {

    public CenteredWindow() {
        super();
        setMonitorWindowResize(true);
    }

    protected void onWindowResize(int width, int height) {
        final int x = (width - this.getWidth()) >> 1;
        final int y = (height - this.getHeight()) >> 1;
        setPagePosition(x, y);
    }

    @Override
    protected void onShow() {
        super.onShow();
        final int width = com.google.gwt.user.client.Window.getClientWidth();
        final int height = com.google.gwt.user.client.Window.getClientHeight();
        onWindowResize(width, height);
    }
}
