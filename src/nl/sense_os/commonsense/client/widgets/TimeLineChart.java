package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;

public class TimeLineChart extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineChart";
    private AnnotatedTimeLine chart;
    private Map<TagModel, SensorValueModel[]> data;
    private DataTable dataTable;
    private Grid<TagModel> grid;
    private List<TagModel> shownCharts;

    public TimeLineChart(Map<TagModel, SensorValueModel[]> data, String title) {
        this.data = data;

        ContentPanel chartPanel = createChartPanel();
        if (null != title) {
            chartPanel.setHeading(title);
            chartPanel.setHeaderVisible(true);
        }
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));

        final ContentPanel tagSelectPanel = createTagSelector();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 200, 200, 300);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(true);

        this.setLayout(new BorderLayout());
        this.add(tagSelectPanel, westLayout);
        this.add(chartPanel, centerLayout);
    }

    private ContentPanel createChartPanel() {
        // create data table for chart
        this.dataTable = DataTable.create();
        this.dataTable.addColumn(ColumnType.DATETIME, "Date/Time");

        for (Map.Entry<TagModel, SensorValueModel[]> entry : this.data.entrySet()) {
            TagModel tag = entry.getKey();
            SensorValueModel[] values = entry.getValue();

            this.dataTable.addColumn(ColumnType.NUMBER, tag.<String> get("text"));

            // fill table with values of next tag
            this.dataTable.addRows(values.length);
            int offset = this.dataTable.getNumberOfRows() - values.length;
            int colIndex = this.dataTable.getNumberOfColumns() - 1;

            for (int i = 0; i < values.length; i++) {
                FloatValueModel value = (FloatValueModel) values[i];

                this.dataTable.setValue(i + offset, 0, value.getTimestamp());
                this.dataTable.setValue(i + offset, colIndex, value.getValue());
                // Log.d(TAG, "Sensor value: " + value.getTimestamp() + ", " + value.getValue());
            }

            tag.set("original_column", colIndex - 1);
        }

        // create line chart
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
        options.setScaleType(AnnotatedTimeLine.ScaleType.ALLFIXED);
        this.chart = new AnnotatedTimeLine(this.dataTable, options, "95%", "95%");

        ContentPanel panel = new ContentPanel(new CenterLayout());
        panel.setHeaderVisible(false);
        panel.setBorders(true);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(this.chart);

        return panel;
    }

    private ContentPanel createTagSelector() {

        // column config model
        final CheckBoxSelectionModel<TagModel> selectMdl = new CheckBoxSelectionModel<TagModel>();
        selectMdl.addSelectionChangedListener(new SelectionChangedListener<TagModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TagModel> se) {

                final List<TagModel> newSelection = se.getSelection();
                final List<TagModel> oldSelection = TimeLineChart.this.shownCharts;

                // find newly selected items
                List<TagModel> toAdd = new ArrayList<TagModel>();
                for (TagModel m : newSelection) {

                    if (false == oldSelection.contains(m)) {
                        toAdd.add(m);
                    }
                }

                // find newly deselected items
                List<TagModel> toRemove = new ArrayList<TagModel>();
                for (TagModel m : oldSelection) {

                    if (false == newSelection.contains(m)) {
                        toRemove.add(m);
                    }
                }

                // add new charts
                updateChart(toAdd, toRemove);
            }
        });
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(selectMdl.getColumn());
        configs.add(new ColumnConfig("text", "Tag", 175));
        ColumnModel columns = new ColumnModel(configs);

        // list store
        List<TagModel> tagList = new ArrayList<TagModel>(this.data.size());
        for (Map.Entry<TagModel, SensorValueModel[]> entry : this.data.entrySet()) {
            tagList.add(entry.getKey());
        }
        ListStore<TagModel> store = new ListStore<TagModel>();
        store.add(tagList);

        this.grid = new Grid<TagModel>(store, columns);
        this.grid.setAutoExpandColumn("text");
        this.grid.setSelectionModel(selectMdl);
        this.grid.addPlugin(selectMdl);

        this.shownCharts = new ArrayList<TagModel>();
        this.grid.getSelectionModel().setFiresEvents(false);
        this.grid.getSelectionModel().selectAll();
        this.grid.getSelectionModel().setFiresEvents(true);
        this.shownCharts.addAll(this.data.keySet());

        ContentPanel panel = new ContentPanel();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setSize("100%", "100%");
        panel.setHeaderVisible(false);
        panel.setLayout(new FitLayout());
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setBorders(true);
        panel.add(this.grid);

        return panel;
    }

    private void updateChart(List<TagModel> toAdd, List<TagModel> toRemove) {
        int[] showCols = new int[toAdd.size()];
        for (int i = 0; i < toAdd.size(); i++) {
            TagModel mn = toAdd.get(i);
            showCols[i] = mn.get("original_column");
            // Log.d(TAG, "show " + mn.get("original_column"));
            this.shownCharts.add(mn);
        }
        this.chart.showDataColumns(showCols);

        int[] hideCols = new int[toRemove.size()];
        for (int i = 0; i < toRemove.size(); i++) {
            TagModel mn = toRemove.get(i);
            hideCols[i] = mn.get("original_column");
            // Log.d(TAG, "hide " + mn.get("original_column"));
            this.shownCharts.remove(mn);
        }
        this.chart.hideDataColumns(hideCols);
    }
}
