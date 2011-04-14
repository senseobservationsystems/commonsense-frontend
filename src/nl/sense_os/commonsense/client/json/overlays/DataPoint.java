package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

import com.google.gwt.i18n.client.NumberFormat;

public class DataPoint implements AbstractDataPoint {

    private String rawValue;
    private Date timestamp;

    public DataPoint(String rawValue, Date timestamp) {
        this.rawValue = rawValue;
        this.timestamp = timestamp;
    }

    @Override
    public String getCleanValue() {
        return getRawValue().replaceAll("//", "");
    }
    @Override
    public String getDate() {
        long time = this.timestamp.getTime();
        return NumberFormat.getFormat("#.000").format(time / 1000d);
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMonth() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRawValue() {
        return rawValue;
    }

    @Override
    public String getSensorId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String getWeek() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getYear() {
        // TODO Auto-generated method stub
        return null;
    }

}
