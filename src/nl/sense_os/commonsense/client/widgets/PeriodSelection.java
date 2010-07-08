package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import java.util.Date;

import nl.sense_os.commonsense.client.utility.Log;

public class PeriodSelection extends ContentPanel {

    private static final String TAG = "PeriodSelection";
    private final DatePicker picker = new DatePicker();
    private final RadioGroup radioGroup = new RadioGroup();  
    
    public PeriodSelection() {
        
        final Text textRange = new Text("Select time range:");  
        final Text textDate = new Text("Select END date for sensor values:");    
        
        this.picker.setValue(new Date());
      
        Radio radio1d = new Radio();
        radio1d.setId("1d");
        radio1d.setBoxLabel("1d");
        radio1d.setValue(true);
      
        Radio radio5d = new Radio();
        radio5d.setId("5d");
        radio5d.setBoxLabel("5d");
      
        Radio radio1m = new Radio();
        radio1m.setId("1m");
        radio1m.setBoxLabel("1m");
      
        Radio radio3m = new Radio();
        radio3m.setId("3m");
        radio3m.setBoxLabel("3m");
         
        radioGroup.add(radio1d);
        radioGroup.add(radio5d);
        radioGroup.add(radio1m);
        radioGroup.add(radio3m);
        
        this.setLayout(new RowLayout(Orientation.VERTICAL));  
        this.setHeading("Time range selection");
        this.setCollapsible(true);
        this.add(textRange, new RowData(1, -1, new Margins(10,10,0,10)));
        this.add(radioGroup, new RowData(1, -1, new Margins(5)));
        this.add(textDate, new RowData(1, -1, new Margins(10,10,0,10)));
        this.add(this.picker, new RowData(1, -1, new Margins(5)));
    }
    
    public long[] getTimeRange() {
        
        final long end = this.picker.getValue().getTime();
        long start = 0;
        final String radioId = radioGroup.getValue().getId();
        if (radioId.equals("1d")) {
            start = end - 1 * 24 * 60 * 60 * 1000;
        } else if (radioId.equals("5d")) {
            start = end - 5 * 24 * 60 * 60 * 1000;
        } else if (radioId.equals("1m")) {
            start = end - 31 * 24 * 60 * 60 * 1000;
        } else if (radioId.equals("3m")) {
            start = end - 3* 31 * 24 * 60 * 60 * 1000;
        } else {
            Log.w(TAG, "Unexpected time range: " + radioId);
        }
        
        return new long[] {start, end};
    }
}
