package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.PhoneModel;

public class PhoneStateGrid extends LayoutContainer {

    private static final String TAG = "PhoneStateGrid"; 
    private ListStore<BaseModel> store;
    
    public PhoneStateGrid(PhoneModel phone) {
        this.store = new ListStore<BaseModel>();

        this.setLayout(new FitLayout());
        
        // create ListStore for the current phone info
        createListStore(phone);
        
        Grid<BaseModel> grid = createGrid();
        this.add(grid, new FitData(0));
    }
    
    private ColumnModel createColumnModel() {
        
        List<ColumnConfig> colConfigs = new ArrayList<ColumnConfig>();
        
        ColumnConfig column = new ColumnConfig();
        column.setDataIndex("key");
        column.setId("key");
        column.setHeader("State");
        column.setWidth(50);
        colConfigs.add(column);

        column = new ColumnConfig();
        column.setDataIndex("value");
        column.setId("value");
        column.setHeader("Value");
        column.setWidth(125);
        colConfigs.add(column);        

        return new ColumnModel(colConfigs);
    }
    
    private Grid<BaseModel> createGrid() {
        Grid<BaseModel> grid = new Grid<BaseModel>(this.store, createColumnModel());
        grid.setStyleAttribute("borderTop", "none");
        grid.setStripeRows(true);
        grid.setHeight(200);
        return grid;
    }
    
    private void createListStore(PhoneModel phone) {

        if (null == this.store) {
            this.store = new ListStore<BaseModel>();
        } else {
            this.store.removeAll();
        }
        
        BaseModel id = new BaseModel();
        id.set("key", "ID");
        id.set("value", phone.getId());
        this.store.add(id);

        BaseModel brand = new BaseModel();
        brand.set("key", "Brand");
        brand.set("value", phone.getBrand());
        this.store.add(brand);

        BaseModel type = new BaseModel();
        type.set("key", "Type");
        type.set("value", phone.getType());
        this.store.add(type);

        BaseModel number = new BaseModel();
        number.set("key", "Number");
        number.set("value", phone.getNumber());
        this.store.add(number);

        BaseModel imei = new BaseModel();
        imei.set("key", "IMEI");
        imei.set("value", phone.getImei());
        this.store.add(imei);

        BaseModel ip = new BaseModel();
        ip.set("key", "IP");
        ip.set("value", phone.getIp());
        this.store.add(ip);

        BaseModel date = new BaseModel();
        date.set("key", "Date");
        date.set("value", phone.getDate());
        this.store.add(date);
    }
    
    public void setPhone(PhoneModel phone) {
        Log.d(TAG, "setPhone");        
        createListStore(phone);
    }
}
