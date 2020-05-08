package SupplyChain_ontology.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class Customer_Order implements AgentAction {
	private double price; 
	private double dueDate; 
	private double penalty;
	private Device device;
	private double numOfDevices;
	private AID buyer;

	public double getNumOfDevices() {
		return numOfDevices;
	}
	public void setNumOfDevices(double numOfDevices) {
		this.numOfDevices = numOfDevices;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getDueDate() {
		return dueDate;
	}
	public void setDueDate(double dueDate) {
		this.dueDate = dueDate;
	}
	public double getPenalty() {
		return penalty;
	}
	public void setPenalty(double penalty) {
		this.penalty = penalty;
	}
	
	public Smartphone getSmartphone() {
		return (Smartphone) device;
	}
	public Phablet getPhablet() {
		return (Phablet) device;
	}

	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}

	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
}
