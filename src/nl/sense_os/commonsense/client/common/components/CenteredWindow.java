package nl.sense_os.commonsense.client.common.components;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.Window;

/**
 * Window that automatically keeps itself centered.
 */
public class CenteredWindow extends Window {

    public CenteredWindow() {
        super();
        // LOG.setLevel(Level.ALL);
        setMonitorWindowResize(true);
        setPlain(true);
    }

    /**
     * Adds a listener to receive window events.
     * 
     * @param listener
     *            the listener
     */
    public void addWindowListener(WindowListener listener) {
        addListener(Events.Activate, listener);
        addListener(Events.Resize, listener);
        addListener(Events.Deactivate, listener);
        addListener(Events.Minimize, listener);
        addListener(Events.Maximize, listener);
        addListener(Events.Restore, listener);
        addListener(Events.Hide, listener);
        addListener(Events.Show, listener);
    }

    @Override
    protected void onWindowResize(int width, int height) {
        final int x = (width - this.getWidth()) >> 1;
        final int y = (height - this.getHeight()) >> 1;
        setPagePosition(x, y);
    }

    @Override
    protected void onShow() {
        super.onShow();
    }
}
