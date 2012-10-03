package nl.sense_os.commonsense.common.client.entrypoint;

import nl.sense_os.commonsense.common.client.component.UnsupportedBrowserComponent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class UnsupportedBrowserEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
		RootPanel loadingPanel = RootPanel.get("loading");
		if (null != loadingPanel) {
			loadingPanel.setVisible(false);
		}
		RootPanel.get().clear();
		RootPanel.get().add(new UnsupportedBrowserComponent());
	}
}
