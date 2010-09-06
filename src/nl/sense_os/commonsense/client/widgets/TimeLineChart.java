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
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;

public class TimeLineChart extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineChart";
    private AnnotatedTimeLine chart;
    private final Map<TagModel, SensorValueModel[]> data = new HashMap<TagModel, SensorValueModel[]>();
    private DataTable dataTable;
    private Grid<TagModel> grid;
    private final List<TagModel> shownCharts = new ArrayList<TagModel>();
    private final ListStore<TagModel> store = new ListStore<TagModel>();

    public TimeLineChart(List<TaggedDataModel> taggedDatas, String title) {

        for (final TaggedDataModel taggedData : taggedDatas) {
            addData(taggedData);
        }

        setupLayout(title);
    }

    public TimeLineChart(TaggedDataModel taggedData, String title) {

        addData(taggedData);

        setupLayout(title);
    }

    /**
     * Adds data to the collection of displayed data.
     * 
     * @param taggedData
     */
    public void addData(TaggedDataModel taggedData) {

        // add data to data table
        addDataColumn(taggedData);

        // draw new data table (if chart is visible)
        if (null != this.chart) {
            final AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
            options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
            options.setWindowMode(WindowMode.OPAQUE);
            this.chart.draw(this.dataTable, options);
        }
    }

    /**
     * Adds a new column to the data table that is backing the chart.
     * 
     * @param taggedData
     */
    private void addDataColumn(TaggedDataModel taggedData) {
        final TagModel tag = taggedData.getTag();
        final SensorValueModel[] values = taggedData.getData();

        // create dataTable if necessary
        if (null == this.dataTable) {
            this.dataTable = DataTable.create();
            this.dataTable.addColumn(ColumnType.DATETIME, "Date/Time");
        }

        this.dataTable.addColumn(ColumnType.NUMBER, tag.<String> get("text"));

        // fill table with values of next tag
        this.dataTable.addRows(values.length);
        final int offset = this.dataTable.getNumberOfRows() - values.length;
        final int colIndex = this.dataTable.getNumberOfColumns() - 1;

        for (int i = 0; i < values.length; i++) {
            final FloatValueModel value = (FloatValueModel) values[i];

            this.dataTable.setValue(i + offset, 0, value.getTimestamp());
            this.dataTable.setValue(i + offset, colIndex, value.getValue());
            // Log.d(TAG, "Sensor value: " + value.getTimestamp() + ", " + value.getValue());
        }

        tag.set("original_column", colIndex - 1);

        this.data.put(tag, values);
        this.store.add(tag);

        // first select all charts without triggering events
        if (null != this.grid) {
            this.shownCharts.add(tag);
            this.grid.getSelectionModel().setFiresEvents(false);
            this.grid.getSelectionModel().selectAll();
            this.grid.getSelectionModel().setFiresEvents(true);
        }
    }

    /**
     * @return content panel containing the chart
     */
    private LayoutContainer createChartPanel() {

        // create line chart
        final AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
        options.setWindowMode(WindowMode.OPAQUE);
        this.chart = new AnnotatedTimeLine(this.dataTable, options, "95%", "100%");

        final LayoutContainer panel = new LayoutContainer(new CenterLayout());
        panel.setBorders(false);
        panel.add(this.chart);
        panel.setId("chartPanel");
        return panel;
    }

    /**
     * Creates the left panel with tag selector grid.
     * 
     * @return the panel
     */
    private ContentPanel createTagSelector() {

        // column config model
        final CheckBoxSelectionModel<TagModel> selectMdl = new CheckBoxSelectionModel<TagModel>();
        selectMdl.addSelectionChangedListener(new SelectionChangedListener<TagModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TagModel> se) {

                final List<TagModel> newSelection = se.getSelection();
                final List<TagModel> oldSelection = TimeLineChart.this.shownCharts;

                // find newly selected items
                final List<TagModel> toAdd = new ArrayList<TagModel>();
                for (final TagModel m : newSelection) {

                    if (false == oldSelection.contains(m)) {
                        toAdd.add(m);
                    }
                }

                // find newly deselected items
                final List<TagModel> toRemove = new ArrayList<TagModel>();
                for (final TagModel m : oldSelection) {

                    if (false == newSelection.contains(m)) {
                        toRemove.add(m);
                    }
                }

                // add new charts
                updateChart(toAdd, toRemove);
            }
        });
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(selectMdl.getColumn());
        configs.add(new ColumnConfig("text", "Tag", 175));
        final ColumnModel columns = new ColumnModel(configs);

        this.grid = new Grid<TagModel>(this.store, columns);
        this.grid.setAutoExpandColumn("text");
        this.grid.setSelectionModel(selectMdl);
        this.grid.addPlugin(selectMdl);

        // first select all charts without triggering events
        this.grid.getSelectionModel().setFiresEvents(false);
        this.grid.getSelectionModel().selectAll();
        this.grid.getSelectionModel().setFiresEvents(true);
        this.shownCharts.addAll(this.store.getModels());

        final ContentPanel panel = new ContentPanel();
        final VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        panel.setSize("100%", "100%");
        panel.setHeaderVisible(false);
        panel.setLayout(new FitLayout());
        panel.setBorders(true);
        panel.add(this.grid);

        return panel;
    }

    /**
     * Recreates the annotated time line chart after the chart panel is resized. The time line
     * widget does not seem to be able to resize after being created.
     */
    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        LayoutContainer chartPanel = (LayoutContainer) this.getItemByItemId("chartPanel");
        if (null != chartPanel) {

            chartPanel.remove(this.chart);
            
            // recreate line chart
            final AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
            options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
            options.setWindowMode(WindowMode.OPAQUE);
            this.chart = new AnnotatedTimeLine(this.dataTable, options, "95%", "100%");
            
            chartPanel.add(this.chart);
        }
    }

    /**
     * Sets up the initial layout with the chart panel and tag panel.
     * 
     * @param title
     *            (optional) title of this chart
     */
    private void setupLayout(String title) {
        final LayoutContainer chartPanel = createChartPanel();

        chartPanel.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.0)");
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));

        final ContentPanel tagSelectPanel = createTagSelector();
        tagSelectPanel.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.0)");
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 200, 200, 300);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(true);

        if (null != title) {
            setHeading(title);
            setHeaderVisible(true);
        } else {
            setHeaderVisible(false);
        }
        setLayout(new BorderLayout());
        this.add(tagSelectPanel, westLayout);
        this.add(chartPanel, centerLayout);
    }

    /**
     * Updates the chart after a tag has been (de)selected in the tag panel.
     * 
     * @param toAdd
     *            list of tags to show in the chart
     * @param toRemove
     *            list of tags to remove from the chart
     */
    private void updateChart(List<TagModel> toAdd, List<TagModel> toRemove) {
        final int[] showCols = new int[toAdd.size()];
        for (int i = 0; i < toAdd.size(); i++) {
            final TagModel mn = toAdd.get(i);
            showCols[i] = mn.get("original_column");
            // Log.d(TAG, "show " + mn.get("original_column"));
            this.shownCharts.add(mn);
        }
        this.chart.showDataColumns(showCols);

        final int[] hideCols = new int[toRemove.size()];
        for (int i = 0; i < toRemove.size(); i++) {
            final TagModel mn = toRemove.get(i);
            hideCols[i] = mn.get("original_column");
            // Log.d(TAG, "hide " + mn.get("original_column"));
            this.shownCharts.remove(mn);
        }
        this.chart.hideDataColumns(hideCols);
    }
}