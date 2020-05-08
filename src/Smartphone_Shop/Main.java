package Smartphone_Shop;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class Main {

	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
				 String[] test1 = {"zero"};
				 String[] test2 = {"one"};

				 

				AgentController supplier = myContainer.createNewAgent("supplier0", SupplierAgent.class.getCanonicalName(), test1);
				supplier.start();		
				AgentController supplier1 = myContainer.createNewAgent("supplier1", SupplierAgent.class.getCanonicalName(), test2);
				supplier1.start();	
			
			for( int i=0; i<3; i++) {
			AgentController simulationAgent = myContainer.createNewAgent("buyer" + i, BuyerAgent.class.getCanonicalName(), null);
			simulationAgent.start();
			}
			
			AgentController seller = myContainer.createNewAgent("manufacturer" , ManufacturerAgent.class.getCanonicalName(), null);
				seller.start();
				
			//AgentController warehouse = myContainer.createNewAgent("warehouse" , WarehouseAgent.class.getCanonicalName(), null);
				//warehouse.start();
			
			AgentController tickerAgent = myContainer.createNewAgent("ticker", TickerAgent.class.getCanonicalName(),
					null);
			tickerAgent.start();
			
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}


	}

}
