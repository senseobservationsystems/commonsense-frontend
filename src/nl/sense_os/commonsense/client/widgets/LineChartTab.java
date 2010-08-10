package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;

import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;

public class LineChartTab extends LayoutContainer {

    private static final String TAG = "LineChartTab";
    private AnnotatedTimeLine chart;
    private DataTable data;
    private List<SensorValueModel> values;

    public LineChartTab(List<SensorValueModel> values) {        
        this.values = values;         
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        // create data table for chart
        this.data = DataTable.create();
        this.data.addColumn(ColumnType.DATETIME, "Date/Time");
        this.data.addColumn(ColumnType.NUMBER, "Value");

        // fill table if values are present
        if (values.size() > 0) {

            this.data.addRows(values.size());
            for (int i = 0; i < values.size(); i++) {
                FloatValueModel value = (FloatValueModel) values.get(i);
                
                this.data.setValue(i, 0, value.getTimestamp());
                this.data.setValue(i, 1, value.getValue());
//                Log.d(TAG, "Sensor value: " + value.getTimestamp() + ", " + value.getValue());
            }
        } else {
            Log.d(TAG, "Zero values received!");
        }

        // create linechart
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setDisplayAnnotations(true);
        options.setDisplayZoomButtons(true);
        options.setScaleType(AnnotatedTimeLine.ScaleType.ALLFIXED);
        this.chart = new AnnotatedTimeLine(this.data, options, "750px", "500px");

        // set up this TabItem
        this.setLayout(new FitLayout());
        this.add(this.chart);        
    }
}
