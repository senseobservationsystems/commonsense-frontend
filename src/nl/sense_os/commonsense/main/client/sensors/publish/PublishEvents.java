package nl.sense_os.commonsense.main.client.sensors.publish;

import com.extjs.gxt.ui.client.event.EventType;

public class PublishEvents {
	public static final EventType ShowPublisher = new EventType();
	protected static final EventType PublishRequest = new EventType();
	protected static final EventType PublicationSuccess = new EventType();
	protected static final EventType PublicationError = new EventType();
	protected static final EventType DatasetUrlRequest = new EventType();
	protected static final EventType DatasetUrlSuccess = new EventType();
	protected static final EventType DatasetUrlError = new EventType();
}
