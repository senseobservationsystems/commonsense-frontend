package nl.sense_os.testing.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

import nl.sense_os.testing.client.widgets.GroupSearchForm;
import nl.sense_os.testing.client.widgets.NavigationPanel;
import nl.sense_os.testing.client.widgets.SensorDataGrid;

public class MainContainer implements EntryPoint {

	@Override
    public void onModuleLoad() {
		/*
        Viewport vp = new Viewport();
        vp.setLayout(new FitLayout());
        vp.setLayout(new RowLayout());

        NavigationPanel nav = new NavigationPanel();
        vp.add(nav, new MarginData(0));

        SensorDataGrid sg = new SensorDataGrid();
        vp.add(sg, new MarginData(10));
        
        RootPanel.get().add(vp);
        */        
        
        LayoutContainer main = new LayoutContainer();
        main.setLayout(new BorderLayout());

        LayoutContainer north = new LayoutContainer();
        
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 80);
        northData.setSplit(true);
        northData.setMargins(new Margins(3));
        
        final Image logo = new Image("/img/logo_sense-150.png");
        logo.setPixelSize(131, 68);        
        north.add(logo);

        LayoutContainer west = new LayoutContainer();
        west.setBorders(true);
        //west.setLayout(new AccordionLayout());        
        NavigationPanel nav = new NavigationPanel();
        //nav.setZIndex(10);
        west.add(nav);        
        new Draggable(west);

        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 300);  
        westData.setMargins(new Margins(5, 0, 5, 5));  
        westData.setCollapsible(true);
        
        SensorDataGrid sg = new SensorDataGrid();
        
        LayoutContainer portal = new LayoutContainer();
        portal.setHeight(500);
        //portal.setLayout(new FlowLayout());
        portal.setBorders(true);
        portal.add(sg);
        
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);  
        centerData.setMargins(new Margins(5));      
        
        main.add(north, northData);
        main.add(west, westData);
        //main.add(portal, centerData);
        //main.add(new AlertSettingsForm(), centerData);
        main.add(new GroupSearchForm(), centerData);
        	
        RootPanel.get().add(main);
	}

}
