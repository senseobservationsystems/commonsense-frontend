package nl.sense_os.commonsense.client.alerts.create.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.StringTrigger;
import nl.sense_os.commonsense.client.alerts.create.utils.MediaButton;
import nl.sense_os.commonsense.client.alerts.create.utils.MyWidget;
import nl.sense_os.commonsense.client.common.components.WizardFormPanel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StringTriggerForm extends WizardFormPanel {

	private Logger LOG = Logger.getLogger(StringTriggerForm.class.getName());
	private LabelField titleLabel;
	private SimpleComboBox<String> combo1;
	private SimpleComboBox<String> combo2;
	private MediaButton plusButton1;
	private MediaButton plusButton2;

	private final List<String> sensorValues = new ArrayList<String>();
	private final List<SimpleComboBox<String>> equalComboList = new ArrayList<SimpleComboBox<String>>();
	private final List<SimpleComboBox<String>> unequalComboList = new ArrayList<SimpleComboBox<String>>();
	private final List<String> originalIds = new ArrayList<String>();

	@SuppressWarnings("unused")
	private int numEqualFields;
	private int numUnequalFields;
	private StringTriggerForm form = this;
	private SelectionChangedListener<SimpleComboValue<String>> listener;
	private SimpleComboBox<String> controlBox;
	private TextField<String> controlBox1;

	public StringTriggerForm() {

		createTitleLabel();
		createSelectionListener();
		createControlBox();
		createControlField();
		createPlusCombo1();
		createPlusCombo2();
		getOriginalIds();

		// do the layout
		add(titleLabel, new FormData("-10"));
	}

	public void addData(JsArray<Timeseries> data) {

		if (data.length() > 0) {

			// populate the list of possible string values
			JsArray<DataPoint> dataPoints = data.get(0).getData();
			List<String> values = new ArrayList<String>();
			String pointValue;
			for (int i = 0; i < dataPoints.length(); i++) {
				pointValue = dataPoints.get(i).getRawValue();
				if (!values.contains(pointValue)) {
					LOG.finest("Found possible value: '" + pointValue + "'");
					values.add(pointValue);
				}
			}

			sensorValues.clear();
			sensorValues.addAll(values);

			for (SimpleComboBox<String> box : equalComboList) {
				box.add(sensorValues);
			}
			for (SimpleComboBox<String> box : unequalComboList) {
				box.add(sensorValues);
			}
		}
	}

	/**
	 * Create a ComboBox with a hidden label, and assign its Store
	 */
	private SimpleComboBox<String> createComboBox() {
		SimpleComboBox<String> combo = new SimpleComboBox<String>();
		combo.setWidth("100%");
		combo.setHideLabel(true);
		combo.setTypeAhead(true);
		combo.setAllowBlank(true);
		combo.setTriggerAction(TriggerAction.ALL);
		combo.setEmptyText("Select value");
		// combo.add(sensorValues);

		return combo;
	}

	private void createControlBox() {
		controlBox = new SimpleComboBox<String>();
		// controlBox.setAllowBlank(false);
		controlBox.setVisible(false);
		this.add(controlBox);
	}

	private void createControlField() {
		controlBox1 = new TextField<String>();
		controlBox1.setAllowBlank(false);
		controlBox1.setVisible(false);
		controlBox1.setValue(null);
		this.add(controlBox1);
	}

	/**
	 * Creates a minButton
	 * 
	 * @return
	 */
	private MediaButton createMinButton() {

		Image img = new Image("/img/common/minus.png");
		img.setHeight("25px");
		img.setWidth("17px");

		MediaButton minButton = new MediaButton(img);
		minButton.setWidth("15px");
		minButton.setHeight("15px");
		minButton.setStyleName("minButton");

		return minButton;
	}

	/**
	 * Creates and formats a grid to hold the MinButton
	 */
	private Grid createMinButtonGrid(MediaButton button) {
		Grid minButtonGrid = new Grid(1, 1);
		minButtonGrid.setWidget(0, 0, button);
		minButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);
		minButtonGrid.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_MIDDLE);
		return minButtonGrid;
	}

	/**
	 * Creates a combination of a comboBox and minButton
	 */
	private MyWidget createNewCombo(boolean equal) {

		final SimpleComboBox<String> combo = createComboBox();
		combo.setEditable(false);
		if (equal)
			equalComboList.add(combo);
		else
			unequalComboList.add(combo);

		MediaButton minButton = createMinButton();
		Grid minButtonGrid = createMinButtonGrid(minButton);

		final MyWidget panel = new MyWidget();
		panel.add(combo);
		panel.add(minButtonGrid);
		combo.setWidth(combo2.getWidth());// parent_width - PLUSBUTTONSIZE);

		minButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				String panelId = panel.getId();
				boolean equalField = panel.getEqual();

				if (equalField) {
					numEqualFields--;
					equalComboList.remove(combo);
				} else {
					numUnequalFields--;
					unequalComboList.remove(combo);
				}

				removeWidget(panelId);
			}
		});

		return panel;
	}

	/**
	 * Creates a plus button and adds a clickListener
	 * 
	 * @return
	 */
	private MediaButton createPlusButton1() {

		Image img = new Image("/img/common/plus.png");
		img.setHeight("15px");
		img.setWidth("15px");

		MediaButton plusButton1 = new MediaButton(img);
		plusButton1.setWidth("15px");
		plusButton1.setHeight("15px");
		plusButton1.setStyleName("plusButton");

		plusButton1.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				MyWidget panel = createNewCombo(true);
				VerticalPanel vp = new VerticalPanel();
				vp.setStyleName("comboPlusPanel");
				vp.add(panel);

				int count = form.getItemCount();
				int insertIndex = count - (numUnequalFields + 1);
				insert(vp, insertIndex, new FormData("-10"));

				String newId = getNewId();
				panel.setId(newId);
				panel.setEqual(true);
				numEqualFields++;
				// LOG.fine ("Plus Button 1 is clicked. New Id is " + newId);

			}
		});

		return plusButton1;
	}

	/**
	 * Creates a second plusButton and adds a clickListener
	 * 
	 * @return
	 */
	private MediaButton createPlusButton2() {

		Image img = new Image("/img/common/plus.png");
		img.setHeight("15px");
		img.setWidth("15px");

		MediaButton plusButton2 = new MediaButton(img);
		plusButton2.setWidth("15px");
		plusButton2.setHeight("15px");
		plusButton2.setStyleName("plusButton");

		plusButton2.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				MyWidget panel = createNewCombo(false);
				VerticalPanel vp = new VerticalPanel();
				vp.setStyleName("comboPlusPanel");
				vp.add(panel);
				add(vp, new FormData("-10"));

				String newId = getNewId();
				panel.setId(newId);
				panel.setEqual(false);
				numUnequalFields++;
				// LOG.fine ("Plus Button1 is clicked. New Id is " + newId);

			}
		});

		return plusButton2;
	}

	/**
	 * Creates and formats a grid to hold a plus Button a
	 */
	private Grid createPlusButtonGrid(MediaButton button) {
		Grid buttonGrid = new Grid(1, 1);
		buttonGrid.setWidget(0, 0, button);
		buttonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_LEFT);
		buttonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		return buttonGrid;
	}

	/**
	 * Creates a combination of a label, comboBox, and Plus button for "Alert if value is equal",
	 * and adds it to the layout
	 */
	private void createPlusCombo1() {

		LabelField alertEqualLabel = new LabelField("Alert if value is equal to: ");

		combo1 = createComboBox();
		combo1.addSelectionChangedListener(listener);
		equalComboList.add(combo1);

		plusButton1 = createPlusButton1();
		Grid plusButtonGrid = createPlusButtonGrid(plusButton1);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(combo1);
		panel.add(plusButtonGrid);

		VerticalPanel vp = new VerticalPanel();
		vp.setStyleName("comboPlusPanel");
		vp.add(alertEqualLabel);
		vp.add(panel);

		add(vp, new FormData("0"));
	}

	/**
	 * Creates a combination of a label, comboBox, and Plus button for
	 * "Alert if value is not equal", and adds it to the layout
	 */
	private void createPlusCombo2() {

		LabelField alertUnequalLabel = new LabelField("Alert if value is not equal to: ");

		combo2 = createComboBox();
		combo2.addSelectionChangedListener(listener);
		unequalComboList.add(combo2);

		plusButton2 = createPlusButton2();
		Grid plusButtonGrid = createPlusButtonGrid(plusButton2);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(combo2);
		panel.add(plusButtonGrid);

		VerticalPanel vp = new VerticalPanel();
		vp.setStyleName("comboPlusPanel");
		vp.add(alertUnequalLabel);
		vp.add(panel);

		add(vp, new FormData("0"));
	}

	private void createSelectionListener() {

		listener = new SelectionChangedListener<SimpleComboValue<String>>() {

			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> event) {
				boolean ok = false;

				for (int i = 0; i < equalComboList.size(); i++) {
					SimpleComboBox<String> box = equalComboList.get(i);
					String name = box.getSimpleValue();
					if (!name.equals(null) && !name.equals("(no selection)") && !name.equals("")) {
						ok = true;
						// LOG.fine("Good name is " + name);
						controlBox.setSimpleValue(name);
						controlBox1.setValue("1");
						break;
					}
				}

				for (int i = 0; i < unequalComboList.size(); i++) {
					SimpleComboBox<String> box = unequalComboList.get(i);
					String name = box.getSimpleValue();
					if (!name.equals(null) && !name.equals("(no selection)") && !name.equals("")) {
						ok = true;
						// LOG.fine ("Good name2 is " + name);
						controlBox.setSimpleValue(name);
						controlBox1.setValue("1");
						break;
					}
				}

				// LOG.fine ("OK is " + ok);

				if (!ok) {
					controlBox.setSimpleValue("");
					controlBox1.setValue("");
					// LOG.fine ("OK is " + ok);
				}

				// LOG.fine ("ControlBox value is " + controlBox.getRawValue());

			}
		};
	}

	/**
	 * Creates and adds a title label for the form
	 */
	private void createTitleLabel() {
		titleLabel = new LabelField("<b>Sensor with String Values</b>");
		titleLabel.setHideLabel(true);
	}

	private ArrayList<String> getEqualValues() {
		ArrayList<String> equalValues = new ArrayList<String>();
		for (int i = 0; i < equalComboList.size(); i++) {
			String str = equalComboList.get(i).getSimpleValue();
			if (str != null)
				equalValues.add(str);
		}
		return equalValues;
	}

	/**
	 * Gets the assigned Id of a newly created layout element
	 */
	private String getNewId() {
		int count = form.getItemCount();
		String newId = null;

		for (int i = 0; i < count; i++) {
			Component c2 = getItem(i);
			String Id = c2.getId();
			if (!originalIds.contains(Id)) {
				newId = Id;
				originalIds.add(newId);
			}
		}

		return newId;
	}

	/**
	 * Gets the assigned Ids of layout elements and puts them in a new ArrayList
	 */
	private void getOriginalIds() {

		int count = form.getItemCount();
		// LOG.fine ("Item count is " + count);

		for (int i = 0; i < count; i++) {
			Component c2 = getItem(i);
			String Id = c2.getId();
			originalIds.add(Id);
		}
	}

	public StringTrigger getStringTrigger() {
		StringTrigger strTrigger = new StringTrigger();

		ArrayList<String> equalValues = getEqualValues();
		ArrayList<String> unequalValues = getUnequalValues();

		if (equalValues.size() == 0 && unequalValues.size() == 0)
			return null;

		else {
			if (equalValues.size() > 0)
				strTrigger.setEqualValues(equalValues);
			if (unequalValues.size() > 0)
				strTrigger.setUnequalValues(unequalValues);
			return strTrigger;
		}
	}

	public ArrayList<String> getUnequalValues() {
		ArrayList<String> unequalValues = new ArrayList<String>();
		for (int i = 0; i < unequalComboList.size(); i++) {
			String str = unequalComboList.get(i).getSimpleValue();
			if (str != null)
				unequalValues.add(str);
		}
		return unequalValues;
	}

	/**
	 * Removes the panel with a specified panelId from the layout
	 * 
	 * @param panelId
	 */
	private void removeWidget(String panelId) {

		int count = form.getItemCount();

		for (int i = 0; i < count; i++) {
			Component c2 = getItem(i);
			String Id = c2.getId();
			// LOG.fine ("Getting component " + i + " id is " + Id);

			if (Id.equals(panelId)) {
				// LOG.fine ("Found!");
				form.remove(form.getWidget(i));
				// layout();
				break;
			}
		}

		// LOG.fine ("The panel Id from createNewCombo is " + panelId);
	}
}
