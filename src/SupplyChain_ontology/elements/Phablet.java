package SupplyChain_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Phablet extends Device {
	private Screen screen; 
	private Battery battery;

	@Slot(mandatory = true)
	public Screen getScreen() {
		return screen;
	}
	public void setScreen(Screen screen) {
		this.screen = screen;
	} 
	
	@Slot(mandatory = true)
	public Battery getBattery() {
		return battery;
	}
	public void setBattery(Battery battery) {
		this.battery = battery;
	} 
	
	
}
