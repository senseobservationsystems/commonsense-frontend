package nl.sense_os.commonsense.main.client.alerts.create.triggers;

public class Notification {
	 private String type;
	 private String address;
	 private String description;
	 
	 public void setType(String type) {
		 this.type = type;
	 }
	 
	 public void setAddress(String address) {
		 this.address = address;
	 }
	 
	 public void setDescription(String description) {
		 this.description = description;
	 }
	 
	 public String getType() {
		 return this.type;
	 }
	 
	 public String getAddress() {
		 return this.address;
	 }
	 
	 public String getDescription() {
		 return this.description;
	 }
	 
}