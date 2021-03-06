package nl.sense_os.commonsense.main.client.groups.join.components;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.main.client.ext.component.WizardFormPanel;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.ext.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.ext.util.SensorTextFilter;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.sensors.library.SensorGroupRenderer;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class ShareSensorsForm extends WizardFormPanel {

	private List<String> reqSensorNames;
	private LabelField lblReqSensors;

	private GroupingStore<ExtSensor> store;
	private Grid<ExtSensor> grid;
	private ToolBar filterBar;

	public ShareSensorsForm(final List<String> reqSensors, List<String> optSensors,
			List<ExtSensor> sensors) {

		reqSensorNames = reqSensors;

		initGrid(sensors);
		initFilters();
		ContentPanel panel = new ContentPanel(new FitLayout());
		panel.setHeaderVisible(false);
		panel.setStyleAttribute("backgroundColor", "white");
		panel.setTopComponent(filterBar);
		panel.add(grid);

		AdapterField field = new AdapterField(panel) {

			@Override
			public boolean isValid() {
				return isValid(false);
			}

			@Override
			public boolean isValid(boolean silent) {

				selectRequiredSensors();

				String missingSensors = "";
				List<ExtSensor> selected = grid.getSelectionModel().getSelectedItems();
				for (String reqName : reqSensorNames) {
					boolean reqSensorSelected = false;
					for (ExtSensor sensor : selected) {
						if (sensor.getName().equals(reqName)) {
							reqSensorSelected = true;
							break;
						}
					}
					if (!reqSensorSelected) {
						missingSensors += reqName + ", ";
					}
				}
				boolean valid = missingSensors.length() == 0;

				// display message
				// TODO does not work
				if (!valid && !silent) {
					if (missingSensors.length() > 0) {
						missingSensors = missingSensors.substring(0, missingSensors.length() - 2);
					}
					markInvalid("You need to select " + missingSensors + " sensor(s)");
				}
				return valid;
			}
		};
		field.setHeight(325);
		field.setResizeWidget(true);
		field.setFieldLabel("Select the sensors that you want to share with the group");
		add(field, new FormData(anchorSpec));

		lblReqSensors = new LabelField("foo");
		lblReqSensors.setHideLabel(true);
		setReqSensors(reqSensors);
		add(lblReqSensors, new FormData("-5"));
	}

	public Grid<ExtSensor> getGrid() {
		return grid;
	}

	public List<ExtSensor> getSharedSensors() {
		return grid.getSelectionModel().getSelection();
	}

	/**
	 * Initializes filter toolbar for the grid with sensors. The bar contains text filter and an
	 * owner filter.
	 */
	private void initFilters() {

		// text filter
		SensorTextFilter<ExtSensor> textFilter = new SensorTextFilter<ExtSensor>();
		textFilter.bind(store);

		// add filters to filter bar
		filterBar = new ToolBar();
		filterBar.add(new LabelToolItem("Filter: "));
		filterBar.add(textFilter);
	}

	private void initGrid(List<ExtSensor> sensors) {

		// list store
		store = new GroupingStore<ExtSensor>();
		store.setKeyProvider(new SenseKeyProvider<ExtSensor>());
		store.setMonitorChanges(true);
		store.add(sensors);

		// selection model for the grid
		CheckBoxSelectionModel<ExtSensor> selectionModel = new CheckBoxSelectionModel<ExtSensor>();

		// Column model
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(selectionModel.getColumn());
		configs.addAll(LibraryColumnsFactory.create().getColumns());
		ColumnModel cm = new ColumnModel(configs);

		// grouping view for the grid
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(true);
		view.setForceFit(true);
		view.setGroupRenderer(new SensorGroupRenderer(cm));

		grid = new Grid<ExtSensor>(store, cm);
		grid.setModelProcessor(new SensorProcessor<ExtSensor>());
		grid.setView(view);
		grid.setBorders(false);
		grid.setId("group-sensor-share-grid");
		grid.setStateful(true);
		grid.setLoadMask(true);
		grid.setSelectionModel(selectionModel);
	}

	private void selectRequiredSensors() {

		List<ExtSensor> sensors = store.getModels();
		List<ExtSensor> requiredSensors = new ArrayList<ExtSensor>();
		for (ExtSensor sensor : sensors) {
			boolean required = false;
			for (String reqName : reqSensorNames) {
				if (sensor.getName().equals(reqName)) {
					required = true;
					break;
				}
			}
			if (required) {
				requiredSensors.add(sensor);
			}
		}

		// select the sensors in the grid
		grid.getSelectionModel().select(true,
				requiredSensors.toArray(new ExtSensor[requiredSensors.size()]));
	}

	public void setReqSensors(List<String> sensorNames) {

		reqSensorNames = sensorNames;

		String labelTxt = "You are required to share the following sensors to join this group: ";
		boolean visible = true;
		if (sensorNames.size() > 1) {
			for (String sensor : sensorNames) {
				labelTxt += "'" + sensor + "', ";
			}
			labelTxt = labelTxt.substring(0, labelTxt.length() - 2);
			visible = true;

		} else if (sensorNames.size() == 1) {
			labelTxt = "You are required to share your '" + sensorNames.get(0) + "' sensor.";
			visible = true;

		} else {
			labelTxt = "This group does not require you to share certain sensors.";
			visible = false;
		}

        lblReqSensors.setValue(labelTxt);
		lblReqSensors.setVisible(visible);

		selectRequiredSensors();
	}
}
