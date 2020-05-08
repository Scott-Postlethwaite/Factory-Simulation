package Smartphone_Shop;

import jade.core.Agent;

import java.io.IOException;
import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import SupplyChain_ontology.SupplyChainOntology;
import SupplyChain_ontology.elements.Battery;
import SupplyChain_ontology.elements.Component;
import SupplyChain_ontology.elements.Customer_Order;
import SupplyChain_ontology.elements.Delivered;
import SupplyChain_ontology.elements.Manufacturer_Order;
import SupplyChain_ontology.elements.RAM;
import SupplyChain_ontology.elements.Recieved;
import SupplyChain_ontology.elements.Screen;
import SupplyChain_ontology.elements.Smartphone;
import SupplyChain_ontology.elements.Storage;


public class SupplierAgent extends Agent{

	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	//stock list, with serial number as the key
	//private List<Component> components = new ArrayList<Component>(); 
	private List<Integer> components = new ArrayList<Integer>();
	HashMap<Integer, Manufacturer_Order> orderManager = new HashMap<Integer, Manufacturer_Order>();
	HashMap<Integer, Integer> orderDays = new HashMap<Integer, Integer>();

	private AID buyerAID;
	private String args[];
	private AID tickerAgent;
	private int numberOfRequests;
	private boolean looped;
	private int orderNum =0;
	private int day=0;

	@Override
	protected void setup() {





		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supplier");
		sd.setName(getLocalName() + "-supplier-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		args = (String[])this.getArguments();

		buyerAID = new AID("manufacturer",AID.ISLOCALNAME);

		if(args[0] == "zero") {
			Screen screen = new Screen();
			screen.setInch("5inch");
			screen.setSerialNumber(101);
			components.add(screen.getSerialNumber());
			Battery battery = new Battery();
			battery.setSerialNumber(201);
			battery.setCapacity("2000mAh");

			components.add(battery.getSerialNumber());

			RAM ram = new RAM();
			ram.setSerialNumber(301);
			ram.setAmmount("4Gb");
			components.add(ram.getSerialNumber());

			Storage storage = new Storage();
			storage.setSpace("64GB");
			storage.setSerialNumber(401);
			components.add(storage.getSerialNumber());

			Screen screen2 = new Screen();
			screen2.setInch("7inch");
			screen2.setSerialNumber(102);
			components.add(screen2.getSerialNumber());

			Battery battery2 = new Battery();
			battery2.setCapacity("3000mAh");
			battery2.setSerialNumber(202);
			components.add(battery2.getSerialNumber());

			RAM ram2 = new RAM();
			ram2.setAmmount("8Gb");
			ram2.setSerialNumber(302);
			components.add(ram2.getSerialNumber());

			Storage storage2 = new Storage();
			storage2.setSpace("256GB");
			storage2.setSerialNumber(402);
			components.add(storage2.getSerialNumber());
		}else {

			RAM ram = new RAM();
			ram.setAmmount("4Gb");
			ram.setSerialNumber(301);
			components.add(ram.getSerialNumber());

			Storage storage = new Storage();
			storage.setSpace("64GB");
			storage.setSerialNumber(401);
			components.add(storage.getSerialNumber());

			RAM ram2 = new RAM();
			ram2.setAmmount("8Gb");
			ram2.setSerialNumber(302);
			components.add(ram2.getSerialNumber());

			Storage storage2 = new Storage();
			storage2.setSpace("256GB");
			storage2.setSerialNumber(402);
			components.add(storage2.getSerialNumber());

		}

		/*
		if(args[0] == "1") {
			Screen screen = new Screen();
			screen.setInch("5inch");
			components.put(screen.getSerialNumber(),screen);

			Battery battery = new Battery();
			battery.setCapacity("2000mAh");
			components.put(battery.getSerialNumber(),battery);

			RAM ram = new RAM();
			ram.setAmmount("4Gb");
			components.put(ram.getSerialNumber(),ram);

			Storage storage = new Storage();
			storage.setSpace("64GB");
			components.put(storage.getSerialNumber(),storage);

			Screen screen2 = new Screen();
			screen2.setInch("7inch");
			components.put(screen2.getSerialNumber(),screen2);

			Battery battery2 = new Battery();
			battery2.setCapacity("3000mAh");
			components.put(battery2.getSerialNumber(),battery2);

			RAM ram2 = new RAM();
			ram2.setAmmount("8Gb");
			components.put(ram2.getSerialNumber(),ram2);

			Storage storage2 = new Storage();
			storage2.setSpace("256GB");
			components.put(storage2.getSerialNumber(),storage2);
		}else {

			RAM ram = new RAM();
			ram.setAmmount("4Gb");
			components.put(ram.getSerialNumber(),ram);

			Storage storage = new Storage();
			storage.setSpace("64GB");
			components.put(storage.getSerialNumber(),storage);

			RAM ram2 = new RAM();
			ram2.setAmmount("8Gb");
			components.put(ram2.getSerialNumber(),ram2);

			Storage storage2 = new Storage();
			storage2.setSpace("256GB");
			components.put(storage2.getSerialNumber(),storage2);

		}

		 */


		addBehaviour(new TickerWaiter(this));
	}

	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {

					Behaviour os = new OffersServer(myAgent);
					Behaviour ord = new Orders(myAgent);

					myAgent.addBehaviour(os);
					myAgent.addBehaviour(ord);
					//ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					//cyclicBehaviours.add(os);
					//myAgent.addBehaviour(new EndDayListener(myAgent,cyclicBehaviours));
				}
				else {
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}



		public class OffersServer extends CyclicBehaviour {

			public OffersServer(Agent a) {
				super(a);
			}

			@Override
			public void action() {

				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					numberOfRequests++;
					try {
						ContentElement ce = null;

						// Let JADE convert from String to Java objects
						// Output will be a ContentElement
						ce = getContentManager().extractContent(msg);
						if(ce instanceof Action) {

							AgentAction act = (AgentAction) ((Action) ce).getAction();

							if (act instanceof Manufacturer_Order) {

								Manufacturer_Order order = (Manufacturer_Order) act;
								if(components.contains(order.getComponent().getSerialNumber())) {
									orderManager.put(orderNum,order);
									orderNum++;
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
									myAgent.send(reply);
								}
								else {
									System.out.println(order.getComponent().getSerialNumber());
									System.out.println(components);
									System.out.println(order.getComponent());
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
									myAgent.send(reply);

								}
								 
							}
						}
					}

					catch (CodecException ce) 
					{
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}

				}
				else
				{
					block();
				}
				
				MessageTemplate messt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
				ACLMessage messg = myAgent.receive(messt);
				if (messg!=null && messg.getContent()=="done"){
					if (messg!=null) {
						
						
						
						
							myAgent.removeBehaviour(this);
							
						
						}
					
				
				
			}
			
		}
		}
		

			public class Orders extends Behaviour {
				
				public Orders(Agent a) {
					super(a);
				}

				@Override
				public void action() {


					looped=false;
					for(int i=0; i < orderManager.size();i++) {

						Manufacturer_Order order = orderManager.get(i);
						if(order!=null) {
							

						int orderDate= (int) order.getDaysLeft();
						//System.out.println(order.getDueDate());

						if (orderDate==day){


							ACLMessage enquiry = new ACLMessage(ACLMessage.CONFIRM);
							enquiry.addReceiver(buyerAID);
							enquiry.setLanguage(codec.getName());
							enquiry.setOntology(ontology.getName());
							Action request1 = new Action();
							request1.setAction(order);
							request1.setActor(buyerAID);
							
							
							
							Recieved recieved = new Recieved();
							recieved.setOrder(order);
							


							try {
							    myAgent.getContentManager().fillContent(enquiry, recieved);
							    myAgent.send(enquiry);
							} catch (Exception e) {
							     throw new RuntimeException("cannot fill message.", e);
							}

							
							
					
						}
					/*	else
						{
							order.setDaysLeft(orderDate-1);
							orderManager.put(i,order);
							}
	*/					}
						}
					
					looped =true;
					
				}

				@Override
				public boolean done() {
					if(looped==true) {
						
						
						ACLMessage sellerDone = new ACLMessage(ACLMessage.QUERY_IF);
						sellerDone.setContent("done");
						sellerDone.addReceiver(buyerAID);
						myAgent.send(sellerDone);
						
						
						
						ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
						tick.setContent("done");
						tick.addReceiver(tickerAgent);
						myAgent.send(tick);
						day++;
						return true;
					}
					
					return false;
				}
			}
	}
}

			/*
			@Override
			public boolean done() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					int requests =  Integer.parseInt(msg.getContent());
					System.out.println(requests);
					if (requests==numberOfRequests) {
				
					/*
					
					MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
					ACLMessage msg1 = myAgent.receive(mt1);
					if(msg1 != null) {
						try {
 
							ContentElement ce = null;

							// Let JADE convert from String to Java objects
							// Output will be a ContentElement
							ce = getContentManager().extractContent(msg1);
							if(ce instanceof Action) {

								AgentAction act = (AgentAction) ((Action) ce).getAction();

								if (act instanceof Manufacturer_Order) {

									Manufacturer_Order order = (Manufacturer_Order) act;
									if(components.contains(order.getComponent().getSerialNumber())) {
										System.out.println("Sold a component");
										ACLMessage reply = msg1.createReply();
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
										myAgent.send(reply);
									}
									else {
										System.out.println(order.getComponent().getSerialNumber());
										System.out.println(components);
										System.out.println(order.getComponent());
										ACLMessage reply = msg1.createReply();
										reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
										myAgent.send(reply);

									}
									 
								}
							}
						}
						catch (CodecException ce) {
							ce.printStackTrace();
						}
						catch (OntologyException oe) {
							oe.printStackTrace();
						}

						
					}
					
					
					*/
					
					
					/*
					
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("done");
					tick.addReceiver(tickerAgent);
					myAgent.send(tick);
					
					return true;
				}
				}
					
				
				return false;
			}

			/*
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}*/

	//	}

/*

		public class EndDayListener extends CyclicBehaviour {
			private int buyersFinished = 0;
			private List<Behaviour> toRemove;

			public EndDayListener(Agent a, List<Behaviour> toRemove) {
				super(a);
				this.toRemove = toRemove;
			}

			@Override
			public void action() {
				
				MessageTemplate messt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
				ACLMessage messg = myAgent.receive(messt);
				
				
				if(messg != null) {
					int requests =  Integer.parseInt(messg.getContent());
					System.out.println("Agent " +args[0] +" Should Have Recieved "+requests + " Requests but only got " + numberOfRequests );
					if (requests==numberOfRequests) {
				
			//	if (messg!=null && messg.getContent()=="done"){
				if (messg!=null) {
					
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("done");
					tick.addReceiver(tickerAgent);
					myAgent.send(tick);
						myAgent.removeBehaviour(this);
						
					
					}
				
	
				
			//	MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
				//ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					buyersFinished++;
				}
				else {
					block();
				}
				if(buyersFinished == 1) {
					
					//remove behaviours
					for(Behaviour b : toRemove) {
						myAgent.removeBehaviour(b);
					}
					myAgent.removeBehaviour(this);
				}
			}
		}


	}

*/

