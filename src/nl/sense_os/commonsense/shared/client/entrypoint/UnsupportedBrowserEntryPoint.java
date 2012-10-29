package nl.sense_os.commonsense.shared.client.entrypoint;

import nl.sense_os.commonsense.shared.client.component.UnsupportedBrowserComponent;

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
