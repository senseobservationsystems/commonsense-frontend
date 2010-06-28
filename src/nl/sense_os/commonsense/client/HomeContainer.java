package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.UserModel;

public class HomeContainer extends LayoutContainer {

    DataServiceAsync dataSvc;
    AsyncCallback<Void> mainCallback;
    UserModel user;
    ContentPanel contentPanel;
    LayoutContainer centerContainer;
    LayoutContainer westContainer;
    Text phoneMsg;
    ListStore<PhoneModel> phoneStore;
    Grid<BaseModel> phoneGrid;

    ComboBox<PhoneModel> phoneCombo;
    List<PhoneModel> phones; // legacy from HomeScreen.java

    public HomeContainer(UserModel user, AsyncCallback<Void> callback) {
        this.dataSvc = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;

        this.phoneStore = new ListStore<PhoneModel>();
    }

    private LayoutContainer createCenterPanel() {
        System.out.println("createContentPanel");

        LayoutContainer panel = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        return panel;
    }

    private LayoutContainer createWestPanel() {

        LayoutContainer panel = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setLayout(layout);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);

        panel.add(new Text("Hello, " + this.user.getName() + "!"), new VBoxLayoutData(new Margins(
                10, 0, 10, 0)));

        if (this.phoneMsg == null) {
            this.phoneMsg = new Text();
        }
        panel.add(this.phoneMsg, new VBoxLayoutData(new Margins(10, 0, 10, 0)));

        if (null != this.phoneCombo) {
            panel.add(this.phoneCombo, new VBoxLayoutData(new Margins(10, 0, 10, 0)));
        }

        // if (this.phoneValues == null) {
        // this.phoneValues = new Grid();
        // }
        // panel.add(this.phoneValues, new VBoxLayoutData(new Margins(10, 0, 10, 0)));

        VBoxLayoutData flex = new VBoxLayoutData(new Margins(0, 0, 5, 0));
        flex.setFlex(1);
        panel.add(new Text(), flex);

        Button btnLogout = new Button("logout");
        btnLogout.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent be) {
                dataSvc.logout(new AsyncCallback<Void>() {
                    public void onFailure(Throwable ex) {
                        mainCallback.onFailure(ex);
                    }

                    public void onSuccess(Void result) {
                        mainCallback.onSuccess(null);
                    }
                });
            }
        });
        panel.add(btnLogout, new VBoxLayoutData(new Margins(0, 0, 10, 0)));

        return panel;
    }

    private void getPhoneDetails() {
        AsyncCallback<List<PhoneModel>> callback = new AsyncCallback<List<PhoneModel>>() {
            public void onFailure(Throwable ex) {
                System.out.println("Phone details fetching failed: " + ex.getMessage());
                showPhoneDetailsFailure();
            }

            public void onSuccess(List<PhoneModel> result) {
                System.out.println("Phone details received");
                HomeContainer.this.phones = result;
                showPhoneDetails();
            }
        };
        HomeContainer.this.dataSvc.getPhoneDetails(callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        this.contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense");
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setSize(1024, 768);
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);

        this.westContainer = createWestPanel();
        BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST);
        west.setMargins(new Margins(5));
        this.contentPanel.add(this.westContainer, west);

        this.centerContainer = createCenterPanel();
        BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
        center.setMargins(new Margins(5));
        this.contentPanel.add(this.centerContainer, center);

        this.setLayout(new CenterLayout());
        this.add(this.contentPanel);

        getPhoneDetails();
    }

    private void showPhoneDetails() {
        System.out.println("showPhoneDetails");
        if (this.phones.size() > 0) {

            // add the phone numbers to the item list
            this.phoneMsg.setText("Found " + phones.size() + " registered phones.");

            this.phoneStore = new ListStore<PhoneModel>();
            this.phoneStore.add(phones);

            ComboBox<PhoneModel> combo = new ComboBox<PhoneModel>();
            combo.setEmptyText("Select a phone...");
            combo.setStore(this.phoneStore);
            combo.setDisplayField("type");
            combo.setTriggerAction(TriggerAction.ALL);
            combo.addSelectionChangedListener(new SelectionChangedListener<PhoneModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<PhoneModel> se) {
                    showPhoneInfo(se.getSelectedItem());
                }
            });

            if (null != this.phoneCombo) {
                this.westContainer.remove(this.phoneCombo);
            }
            this.phoneCombo = combo;
            this.westContainer.insert(this.phoneCombo, 2);

        } else {
            phoneMsg.setText("Error: no registered phones found.");
        }
        this.doLayout();
    }

    private void showPhoneDetailsFailure() {
        // do nothing
    }

    private void showPhoneInfo(PhoneModel phone) {
        System.out.println("showSelectedPhoneInfo");

        if (phones != null) {

            List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

            ColumnConfig column = new ColumnConfig();
            column.setDataIndex("key");
            column.setId("key");
            column.setHeader("State");
            column.setWidth(50);
            configs.add(column);

            column = new ColumnConfig();
            column.setDataIndex("value");
            column.setId("value");
            column.setHeader("Value");
            column.setWidth(125);
            configs.add(column);

            ListStore<BaseModel> store = new ListStore<BaseModel>();

            BaseModel id = new BaseModel();
            id.set("key", "ID");
            id.set("value", phone.getId());
            store.add(id);

            BaseModel brand = new BaseModel();
            brand.set("key", "Brand");
            brand.set("value", phone.getBrand());
            store.add(brand);

            BaseModel type = new BaseModel();
            type.set("key", "Type");
            type.set("value", phone.getType());
            store.add(type);

            BaseModel number = new BaseModel();
            number.set("key", "Number");
            number.set("value", phone.getNumber());
            store.add(number);

            BaseModel imei = new BaseModel();
            imei.set("key", "IMEI");
            imei.set("value", phone.getImei());
            store.add(imei);

            BaseModel ip = new BaseModel();
            ip.set("key", "IP");
            ip.set("value", phone.getIp());
            store.add(ip);

            BaseModel date = new BaseModel();
            date.set("key", "Date");
            date.set("value", phone.getDate());
            store.add(date);

            ColumnModel cm = new ColumnModel(configs);

            Grid<BaseModel> grid = new Grid<BaseModel>(store, cm);
            grid.setStyleAttribute("borderTop", "none");
            grid.setStripeRows(true);
            grid.setSize("100%", "200px");

            if (this.phoneGrid != null) {
                this.westContainer.remove(phoneGrid);
            }
            this.phoneGrid = grid;
            this.westContainer.insert(this.phoneGrid, 3, new VBoxLayoutData(new Margins(10, 0, 10,
                    0)));

            // add new grid to main panel, replacing old one
            // this.westContainer.remove(this.phoneValues);
            // this.phoneValues = grid;
            // this.westContainer.insert(this.phoneValues, 3, new VBoxLayoutData(new Margins(10, 0,
            // 10, 0)));

            this.doLayout();
        }
    }
}
