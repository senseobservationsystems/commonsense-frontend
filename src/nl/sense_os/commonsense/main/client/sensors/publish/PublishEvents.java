package nl.sense_os.commonsense.main.client.sensors.publish;

import com.extjs.gxt.ui.client.event.EventType;

public class PublishEvents {
	public static final EventType ShowPublisher = new EventType();
	protected static final EventType PublishRequest = new EventType();
	protected static final EventType PublicationComplete = new EventType();
	protected static final EventType PublicationError = new EventType();
}
