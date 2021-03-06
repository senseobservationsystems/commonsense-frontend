package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.common.client.event.CurrentUserChangedHandler;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface MainApplicationView extends IsWidget, CurrentUserChangedHandler {

	LayoutContainer getActivityPanelGxt();

	AcceptsOneWidget getActivityPanel();
}
