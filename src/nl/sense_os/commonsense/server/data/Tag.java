package nl.sense_os.commonsense.server.data;

public class Tag {

    public static final int TYPE_GROUP = 1;
    public static final int TYPE_DEVICE = 2;
    public static final int TYPE_SENSOR = 3;
    public static final int TYPE_USER = 4;    

    private int type;
	private String path;

	public Tag() {
		
	}
	
	public Tag(String path, int type) {
		setPath(path);
		setType(type);
	}
	
    public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

    
}
