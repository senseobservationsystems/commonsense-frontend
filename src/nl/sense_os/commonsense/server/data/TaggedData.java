package nl.sense_os.commonsense.server.data;

public class TaggedData {

	Tag tag;
	SensorValue[] data;
	
	public TaggedData() {
		
	}
	
	public TaggedData(Tag tag, SensorValue[] data) {
		setTag(tag);
		setData(data);
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public SensorValue[] getData() {
		return data;
	}

	public void setData(SensorValue[] data) {
		this.data = data;
	}

}
