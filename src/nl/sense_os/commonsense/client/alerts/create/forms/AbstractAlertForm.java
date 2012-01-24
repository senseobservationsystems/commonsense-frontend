package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

public abstract class AbstractAlertForm extends VizPanel {
	
    protected final FormData layoutData = new FormData("-10");
	
	public AbstractAlertForm() {
	
		super();
	    setHeaderVisible(false);
	    setBodyBorder(false);
	    setLayout(new FormLayout(LabelAlign.TOP));
	    setScrollMode(Scroll.AUTOY);
	}
	
	public void goVisualize(List<SensorModel> sensors, long start, long end, boolean subsample) {		
        visualize(sensors, start, end, subsample);
	}

}
