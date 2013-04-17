package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;

public class MainApplicationViewGxt extends Composite implements MainApplicationView {

    private ContentPanel center;
    private SimplePanel simplePanel = new SimplePanel();

	public MainApplicationViewGxt() {

        center = new ContentPanel(new FitLayout());
        center.setId("gxt-center");
        center.setHeight("100%");
        center.setHeaderVisible(false);
        center.setBodyBorder(false);

        initComponent(center);
	}

	@Override
	public AcceptsOneWidget getActivityPanel() {
		return simplePanel;
	}

	@Override
    public LayoutContainer getActivityPanelGxt() {
        return center;
	}

	@Override
	public void onCurrentUserChanged(CurrentUserChangedEvent event) {
        // do nothing
	}
}
