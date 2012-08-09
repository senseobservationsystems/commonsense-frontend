package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.common.client.event.CurrentUserChangedHandler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

public interface MainApplicationView extends IsWidget, CurrentUserChangedHandler {

	SimplePanel getActivityPanel();
}
