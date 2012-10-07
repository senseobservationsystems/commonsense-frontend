package nl.sense_os.commonsense.main.client.visualization.component;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.viz.panels.map.DateSlider;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MapVisualizationControls extends Composite {

	/**
	 * Timer class for animation
	 */
	private class AnimationTimer extends Timer {

		@Override
		public void run() {
			stepAnimationSlider();
		}
	}

	private static final Logger LOG = Logger.getLogger(MapVisualizationControls.class.getName());
	private boolean animationMode = false;
	private ToggleButton animateButton;
	private SliderField animateField;
	private VerticalPanel animatePanel;
	private boolean animationPaused = false;
	/**
	 * Slider for controlling the animation time
	 */
	private DateSlider animationSlider;
	private AnimationTimer animationTimer;
	private SliderField displayEndField;
	/**
	 * Slider for controlling the max time of the displayed traces
	 */
	private DateSlider displayEndSlider;
	private SliderField displayStartField;
	/**
	 * Slider for controlling the start time of the displayed traces
	 */
	private DateSlider displayStartSlider;
	private MapPanel mapPanel;
	private PushButton pauseButton;
	private PushButton playButton;
	private PushButton replayButton;
	private HorizontalPanel playPanel;
	private boolean replayButtonState = false;
	private long sliderMax;
	private long sliderMin;
	private FormPanel slidersForm;
	private long sliderValue;
	private Label timeLabel;

	public MapVisualizationControls(MapPanel mapPanel) {

		LOG.setLevel(Level.ALL);

		this.mapPanel = mapPanel;

		animationSlider = createAnimationSlider();

		playButton = createPlayButton();
		pauseButton = createPauseButton();
		replayButton = createReplayButton();
		animateButton = createAnimateButton();
		timeLabel = new Label();
		timeLabel.setStyleName("timeLabel");

		slidersForm = createControlPanel();

		initComponent(slidersForm);
	}

	/**
	 * Finds the maximum and the minimum values in a set of location timeseries
	 * 
	 * @param dataset
	 */
	private void calcSliderRange(Map<Integer, LocationData> dataset) {

		// find maximum and minimum timestamps
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (LocationData locationData : dataset.values()) {
			Timeseries latitudePoints = locationData.getLatitude();

			int localMin = (int) Math.floor(latitudePoints.getStart() / 1000l);
			if (localMin < min) {
				min = localMin;
			}

			int localMax = (int) Math.ceil(latitudePoints.getEnd() / 1000l);
			if (localMax > max) {
				max = localMax;
			}
		}

		// set display start slider range
		int interval = (max - min) / 25;
		displayStartSlider.setMinValue(min);
		displayStartSlider.setMaxValue(max);
		displayStartSlider.setIncrement(interval);
		displayStartSlider.disableEvents(true);
		// if you set the value to min, the slider starts with the second value
		// for some reason; so i set it to min - 100000, then it equals min anyway
		displayStartSlider.setValue(min - 100000);
		displayStartSlider.enableEvents(true);

		// set display end slider range
		displayEndSlider.setMinValue(min);
		displayEndSlider.setMaxValue(max);
		displayEndSlider.setIncrement(interval);
		displayEndSlider.disableEvents(true);
		// if you set the value to max, the slider starts with the second value
		// for some reason; so i add something to the max, then it equals max anyway
		displayEndSlider.setValue(max + 100000);
		displayEndSlider.enableEvents(true);

		// set anymation slider range
		int playInterval = (max - min) / 500;
		animationSlider.setMinValue(min);
		animationSlider.setMaxValue(max);
		animationSlider.setIncrement(playInterval);
		animationSlider.disableEvents(true);
		animationSlider.setValue(min - 100000);
		animationSlider.enableEvents(true);

		// save the slider ranges in memory
		sliderMin = min * 1000l;
		sliderMax = max * 1000l;
	}

	/**
	 * Stops and removes the animation timers
	 */
	private void cancelAnimationTimer() {
		LOG.fine("Cancel animation");
		if (animationTimer != null) {
			animationTimer.cancel();
			animationTimer = null;
		}
	}

	/**
	 * Create a set of sliders on the bottom, to filter the points to draw according to a time
	 * specified with the sliders. Add the Animate panel.
	 */
	private FormPanel createControlPanel() {

		FormPanel slidersForm = new FormPanel();
		slidersForm.setHeaderVisible(false);
		slidersForm.setBorders(false);
		slidersForm.setBodyBorder(false);
		slidersForm.setPadding(0);

		Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

			@Override
			public void handleEvent(SliderEvent be) {
				if (displayStartSlider.equals(be.getSource())) {
					mapPanel.setDisplayStart(displayStartSlider.getValue() * 1000l);
				} else if (displayEndSlider.equals(be.getSource())) {
					mapPanel.setDisplayEnd(displayEndSlider.getValue() * 1000l);
				} else {
					LOG.warning("Unexpected slide event!");
				}
			}
		};

		// slider for display start time
		displayStartSlider = new DateSlider();
		displayStartSlider.setMessage("{0}");
		displayStartSlider.setId("viz-map-startSlider");
		displayStartSlider.addListener(Events.Change, slideListener);

		displayStartField = new SliderField(displayStartSlider);
		displayStartField.setFieldLabel("Trace start");

		// slider for display end time
		displayEndSlider = new DateSlider();
		displayEndSlider.setMessage("{0}");
		displayEndSlider.setValue(displayEndSlider.getMaxValue());
		displayEndSlider.setId("viz-map-endSlider");
		displayEndSlider.addListener(Events.Change, slideListener);

		displayEndField = new SliderField(displayEndSlider);
		displayEndField.setFieldLabel("Trace end");

		// slider for animation time
		animateField = new SliderField(animationSlider);
		animateField.setHideLabel(true);

		playPanel = new HorizontalPanel();
		playPanel.addStyleName("playPanel");
		animatePanel = new VerticalPanel();
		animatePanel.add(animateButton);

		// ADD THE animate PANEL HERE!
		slidersForm.add(animatePanel);
		animatePanel.add(playPanel);
		playPanel.add(playButton);
		playPanel.add(timeLabel);

		slidersForm.add(animateField, new FormData("-5"));

		displayEndSlider.setValue(displayEndSlider.getMaxValue() + 100000);
		animationSlider.setValue(animationSlider.getMinValue() - 100000);

		sliderValue = sliderMin;

		slidersForm.layout();

		return slidersForm;
	}

	private String formatDate(long timestamp) {
		DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
		String formatDate = format.format(new Date(timestamp));
		return formatDate;
	}

	private DateSlider createAnimationSlider() {
		Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

			@Override
			public void handleEvent(SliderEvent be) {
				if (animationPaused == true && animationMode == true) {

					// update the map
					mapPanel.setAnimationTime(animationSlider.getValue() * 1000);

					// show the play button
					if (replayButtonState == true) {
						playPanel.remove(replayButton);
						playPanel.insert(playButton, 0);
						replayButtonState = false;
					}
				}

			}
		};

		DateSlider slider = new DateSlider();
		slider.setMessage("{0}");
		slider.addListener(Events.Change, slideListener);
		slider.addStyleName("playSlider");
		slider.setValue(slider.getMinValue() - 100000);

		return slider;
	}

	private PushButton createPlayButton() {

		Image playImage = new Image("/img/map/Play4.png");
		playImage.setWidth("26px");
		playImage.setHeight("20px");

		PushButton button = new PushButton(playImage, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				animationMode = true;
				animationPaused = false;

				animationSlider.setValue(animationSlider.getMinValue() - 100000);

				mapPanel.setAnimationMode(true);

				playPanel.remove(playButton);
				playPanel.insert(pauseButton, 0);
				cancelAnimationTimer();
				sliderValue = animationSlider.getValue() * 1000l;
				startAnimationTimer();
			}
		});

		button.setStyleName("mediaButton");

		return button;
	}

	private PushButton createPauseButton() {
		Image pauseImage = new Image("/img/map/pause5.png");
		pauseImage.setWidth("20px");
		pauseImage.setHeight("20px");

		PushButton button = new PushButton(pauseImage, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				if (animationPaused == false) {
					animationPaused = true;
					playPanel.remove(pauseButton);
					playPanel.insert(playButton, 0);
					cancelAnimationTimer();
				}

				else {
					animationPaused = false;
					cancelAnimationTimer();
					startAnimationTimer();
				}

			}
		});

		button.setStyleName("pauseButton");

		return button;
	}

	private PushButton createReplayButton() {
		Image replayImage = new Image("/img/map/replay.png");
		replayImage.setWidth("18px");
		replayImage.setHeight("18px");

		PushButton button = new PushButton(replayImage, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				if (animationPaused == true) {
					animationPaused = false;
					replayButtonState = false;
					playPanel.remove(replayButton);
					playPanel.insert(pauseButton, 0);
					cancelAnimationTimer();

					animationSlider.setValue(animationSlider.getMinValue() - 100000);
					sliderValue = animationSlider.getValue() * 1000l;
					startAnimationTimer();
				}

			}
		});

		button.setStyleName("replayButton");

		return button;
	}

	private ToggleButton createAnimateButton() {

		ToggleButton button = new ToggleButton("Show static mode", "Show animation",
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						setAnimationMode(!animateButton.isDown());
					}
				});

		button.addStyleName("animateButton");

		return button;
	}

	private void setAnimationMode(boolean enable) {
		if (enable) {
			animationMode = true;
			animationPaused = false;
			// LOG.fine ("Animate Button pressed: sliderValue is " + sliderValue);
			animationSlider.setMinValue(displayStartSlider.getValue());
			animationSlider.setMaxValue(displayEndSlider.getValue());
			sliderMin = displayStartSlider.getValue() * 1000l;
			sliderMax = displayEndSlider.getValue() * 1000l;
			sliderValue = displayStartSlider.getValue() * 1000l;
			animationSlider.setValue(displayStartSlider.getValue());

			slidersForm.remove(displayStartField);
			slidersForm.remove(displayEndField);

			animatePanel.add(playPanel);
			slidersForm.add(animateField, new FormData("-5"));
			playPanel.add(pauseButton);
			playPanel.add(timeLabel);
			animationPaused = false;

			mapPanel.setAnimationMode(true);

			cancelAnimationTimer();
			startAnimationTimer();

			slidersForm.layout();

		} else {
			// LOG.fine ("Animate button is off");
			animationMode = false;
			cancelAnimationTimer();

			animatePanel.remove(playPanel);
			playPanel.remove(pauseButton);
			playPanel.remove(replayButton);
			playPanel.remove(playButton);
			playPanel.remove(timeLabel);

			mapPanel.setAnimationMode(false);

			slidersForm.remove(animateField);
			slidersForm.add(displayStartField, new FormData("-5"));
			slidersForm.add(displayEndField, new FormData("-5"));

			slidersForm.layout();

			animationSlider.setValue(animationSlider.getMinValue() - 100000);
			displayStartSlider.setValue(displayStartSlider.getMinValue() - 100000);
			displayEndSlider.setValue(displayEndSlider.getMaxValue() + 100000);

		}
	}

	/**
	 * Create animation timers for each trace
	 */
	private void startAnimationTimer() {
		LOG.fine("Start animation");
		animationTimer = new AnimationTimer();
		animationTimer.scheduleRepeating(10);
	}

	private void stepAnimationSlider() {
		LOG.fine("Update animation slider");

		long minim = sliderMin;
		long maxim = sliderMax;
		long step = (maxim - minim) / 500;

		if (sliderValue + step < maxim) {
			// step animation slider
			sliderValue = sliderValue + step;
			animationSlider.setValue((int) (sliderValue / 1000l));
			timeLabel.setText(formatDate(sliderValue));

		} else {
			// stop the animation slider at the max
			sliderValue = sliderMax;
			animationSlider.setValue((int) (sliderMax / 1000l + 100000));
			timeLabel.setText(formatDate(sliderValue));

			cancelAnimationTimer();

			if (animationPaused == false) {
				animationPaused = true;
				replayButtonState = true;
				playPanel.remove(pauseButton);
				playPanel.insert(replayButton, 0);
			}
		}

		mapPanel.setAnimationTime(sliderValue);
	}

	public void setLocatonDataSet(Map<Integer, LocationData> dataset) {
		calcSliderRange(dataset);
	}
}
