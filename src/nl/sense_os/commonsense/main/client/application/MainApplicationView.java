package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.main.client.shared.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface MainApplicationView extends IsWidget, CurrentUserChangedEvent.Handler,
		PlaceChangeEvent.Handler, NewVisualizationEvent.Handler {

	AcceptsOneWidget getActivityPanel();

	LayoutContainer getGxtActivityPanel();
}
