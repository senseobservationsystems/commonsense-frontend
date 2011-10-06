package nl.sense_os.commonsense.client.common.components;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.AlertCreator;

/**
 * Window that automatically keeps itself centered.
 */
public class CenteredWindow extends Window {
	
	private Logger LOG = Logger.getLogger(CenteredWindow.class.getName());
	

//	public int getWidth() {
//		return this.getWidth();
//	}
//	
//	public int getHeight() {
//		return this.getHeight();
//	}
	
	
    public CenteredWindow() {
        super();
        LOG.setLevel(Level.ALL);
        setMonitorWindowResize(true);
        setPlain(true);
        
        
    }
    
    /**
     * Adds a listener to receive window events.
     * 
     * @param listener the listener
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
        LOG.fine ("The width is " + this.getWidth() + " The height is " + this.getHeight());
        
    }

    @Override
    protected void onShow() {
        super.onShow();
        final int width = com.google.gwt.user.client.Window.getClientWidth();
        final int height = com.google.gwt.user.client.Window.getClientHeight();
        //onWindowResize(width, height);
      
    }
    
    /**
     * Adds a {@link ResizeEvent} handler.
     * 
     * @param handler the handler
     * @return returns the handler registration
     */
    

}
