package nl.sense_os.commonsense.main.client.groupmanagement.component;

import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementView;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;

public class GroupManagementViewGxt extends Composite implements GroupManagementView {

	public GroupManagementViewGxt() {
		ContentPanel panel = new ContentPanel();
		panel.setHeading("Group management");
		initComponent(panel);
	}

	@Override
	public void onListUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLibChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusy(boolean busy) {
		// TODO Auto-generated method stub

	}

}
