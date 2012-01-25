package nl.sense_os.commonsense.client.groups.join;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupJoinView extends View {

    private static final Logger LOG = Logger.getLogger(GroupJoinView.class.getName());
    private GroupJoinDialog window;
    private ListLoader<ListLoadResult<GroupModel>> loader;

    public GroupJoinView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupJoinEvents.Show)) {
            LOG.finest("Show");
            show();

        } else if (type.equals(GroupJoinEvents.JoinSuccess)) {
            LOG.finest("JoinSuccess");
            onSuccess();

        } else if (type.equals(GroupJoinEvents.JoinFailure)) {
            LOG.warning("JoinFailure");
            onFailure();

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void hideWindow() {
        window.hide();
    }

    private void onFailure() {
        window.setBusy(false);

        GroupModel group = window.getGrid().getSelectionModel().getSelectedItem();
        MessageBox.confirm("CommonSense", "Failed to join the group " + group.getName()
                + "! Do you want to retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    submitForm();
                } else {
                    hideWindow();
                }
            }
        });
    }

    private void onSuccess() {
        window.setBusy(false);

        GroupModel group = window.getGrid().getSelectionModel().getSelectedItem();
        MessageBox.info("CommonSense", "You have joined the group " + group.getName() + ".", null);
    }

    private void show() {

        // data proxy
        DataProxy<ListLoadResult<GroupModel>> proxy = new DataProxy<ListLoadResult<GroupModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<GroupModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<GroupModel>> callback) {
                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    LOG.finest("Load library...");
                    AppEvent loadRequest = new AppEvent(GroupJoinEvents.PublicGroupsRequested);
                    loadRequest.setData("callback", callback);
                    fireEvent(loadRequest);
                } else {
                    LOG.warning("Unexpected load config: " + loadConfig);
                    callback.onFailure(null);
                }
            }
        };

        // list loader
        loader = new BaseListLoader<ListLoadResult<GroupModel>>(proxy);

        // create dialog
        window = new GroupJoinDialog(loader);
        window.setHeading("Join a public group");
        window.setSize(540, 480);
        window.setLayout(new FitLayout());

        window.getBtnSubmit().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                submitForm();
            }
        });
        window.getBtnCancel().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                hideWindow();
            }
        });

        window.show();

        loader.load();
    }

    private void submitForm() {

        GroupModel group = window.getGrid().getSelectionModel().getSelectedItem();
        AppEvent event = new AppEvent(GroupJoinEvents.JoinRequest);
        event.setData("group", group);
        event.setSource(this);
        Dispatcher.forwardEvent(event);

        window.setBusy(true);
    }
}
