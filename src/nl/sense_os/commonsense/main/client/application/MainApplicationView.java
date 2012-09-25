package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.main.client.event.CurrentUserChangedEvent.Handler;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface MainApplicationView extends IsWidget, Handler,
		PlaceChangeEvent.Handler {

	AcceptsOneWidget getActivityPanel();

	LayoutContainer getGxtActivityPanel();
}
