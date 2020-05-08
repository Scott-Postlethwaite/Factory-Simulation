package Smartphone_Shop;

import Smartphone_Shop.BuyerAgent.TickerWaiter;

import java.io.FileWriter;
import java.io.IOException;
//import java.lang.ProcessHandle.Info;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import SupplyChain_ontology.SupplyChainOntology;
import SupplyChain_ontology.elements.Battery;
import SupplyChain_ontology.elements.Component;
import SupplyChain_ontology.elements.Customer_Order;
import SupplyChain_ontology.elements.Delivered;
import SupplyChain_ontology.elements.Device;
import SupplyChain_ontology.elements.Manufacturer_Order;
import SupplyChain_ontology.elements.Phablet;
import SupplyChain_ontology.elements.RAM;
import SupplyChain_ontology.elements.Recieved;
import SupplyChain_ontology.elements.Screen;
import SupplyChain_ontology.elements.Smartphone;
import SupplyChain_ontology.elements.Storage;

import java.util.ArrayList;
import java.util.HashMap;

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
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.Agent;

import SupplyChain_ontology.SupplyChainOntology;
import SupplyChain_ontology.elements.Customer_Order;
import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ManufacturerAgent extends Agent{
	private AID tickerAgent;
	private int numQueriesSent;
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	private AID seller1AID;
	private AID seller2AID;
	private int totalOrders;
	private int orderNum;
	private int profit;
	private AID warehouseAID;
	private int day;
	private AID buyer;
	HashMap<Integer, Customer_Order> orderManager = new HashMap<Integer, Customer_Order>();
	HashMap<Integer, Integer> orderDays = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> warehouseHM = new HashMap<Integer, Integer>();
	private int orderId=0;
	private int messages = 0;
	private boolean looped;
	private int numberOfRequests1;
	private int numberOfRequests2;
	private int today =0;
	private int dayOfDelivery;

	//private List<Component> warehouse = new ArrayList<Component>(); 

	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		seller1AID = new AID("supplier0",AID.ISLOCALNAME);
		seller2AID = new AID("supplier1",AID.ISLOCALNAME);
		buyer = new AID("buyer",AID.ISLOCALNAME);

		warehouseAID = new AID("warehouse",AID.ISLOCALNAME);
		profit=0;

		addBehaviour(new TickerWaiter(this));
	}
	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {

			//messages=0;
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					totalOrders =0;

					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new OffersServer(myAgent));
					dailyActivity.addSubBehaviour(new DeliveredServer(myAgent));
					dailyActivity.addSubBehaviour(new orderParts(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					myAgent.addBehaviour(dailyActivity);
					//				myAgent.addBehaviour(new endServer(myAgent));
				}
				else {
					try {
						FileWriter pw = new FileWriter("D:\\Multi Agent Systems\\Coursework\\Reports\\report2.csv",true);
						pw.append( Integer.toString(profit));
						 pw.append("\n");
						 pw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					//termination message to end simulation
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}


	}

	public class OffersServer extends Behaviour {

		public OffersServer(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				try {
					ContentElement ce = null;

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);


					if(ce instanceof Action) {
						messages ++;
						AgentAction act = (AgentAction) ((Action) ce).getAction();


						if (act instanceof Customer_Order) {
							Customer_Order order = (Customer_Order) act;
							Device device = order.getDevice();
							dayOfDelivery=(int)order.getDueDate();
							int ordersToday=0;
							for(int i=0; i < orderManager.size();i++) {

								Customer_Order order2 = orderManager.get(i);
								if(order2!=null) {
									int orderDate= (int) order2.getDueDate();
									if(orderDate==dayOfDelivery)
									{
										ordersToday+=order2.getNumOfDevices();
									}
								}
							}
							if(device instanceof Smartphone) {

								int minPrice=170;
								if(device.getRAM().getSerialNumber()==301) {minPrice+=20;}
								else {minPrice+=35;}
								if(device.getStorage().getSerialNumber()==401) {minPrice+=15;}
								else {minPrice+=40;}
								if(order.getDueDate()<4){minPrice+=50;}
								
								
								if(order.getPrice() >= minPrice && order.getNumOfDevices() + ordersToday <= 50){

									//if(warehouseHM.get(device.getRAM())>=order.getNumOfDevices())

									/*
									if(warehouseHM.containsKey(device.getRAM().getSerialNumber())&&warehouseHM.containsKey(device.getStorage().getSerialNumber())&&warehouseHM.containsKey(((Smartphone) device).getBattery().getSerialNumber())&&warehouseHM.containsKey(((Smartphone) device).getScreen().getSerialNumber()))
									{
										if(warehouseHM.get(device.getRAM().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(device.getStorage().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(((Smartphone) device).getBattery().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(((Smartphone) device).getScreen().getSerialNumber())>=order.getNumOfDevices())
										{
											orderManager.put(orderNum, order);
											orderNum++;
											totalOrders+=order.getNumOfDevices();
											ACLMessage reply = msg.createReply();
											reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
											myAgent.send(reply);
											int num =  warehouseHM.get(device.getRAM().getSerialNumber());
											warehouseHM.put(device.getRAM().getSerialNumber(),num-(int)order.getNumOfDevices());
											 num =  warehouseHM.get(device.getStorage().getSerialNumber());
											warehouseHM.put(device.getStorage().getSerialNumber(),num-(int)order.getNumOfDevices());
											 num =  warehouseHM.get(((Smartphone) device).getBattery().getSerialNumber());
											warehouseHM.put(((Smartphone) device).getBattery().getSerialNumber(),num-(int)order.getNumOfDevices());
											 num =  warehouseHM.get(((Smartphone) device).getScreen().getSerialNumber());
											warehouseHM.put(((Smartphone) device).getScreen().getSerialNumber(),num-(int)order.getNumOfDevices());




											/*
											ACLMessage shipped = new ACLMessage(ACLMessage.INFORM);
											shipped.addReceiver(AID);
											shipped.setLanguage(codec.getName());
											shipped.setOntology(ontology.getName());

											Manufacturer_Order query = new Manufacturer_Order();
											query.setComponent(device.getStorage());
											Action request = new Action();
											request.setAction(query);
											request.setActor(BUYERAGENT);
											try {
										// Let JADE convert from Java objects to string
											getContentManager().fillContent(shipped, request); //send the wrapper object
											send(shipped);
										}
										catch (CodecException cex) {
											cex.printStackTrace();

									}
										catch (OntologyException oe) {
											oe.printStackTrace();
											 System.out.println(oe.toString());
										}

									 */
									//									}
									//									}




									if(order.getDueDate() >= 4 && order.getNumOfDevices() + ordersToday <= 50) {

										orderManager.put(orderNum, order);
										orderNum++;
										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
										myAgent.send(reply);
										ordersToday+=order.getNumOfDevices();









									}
									else if(order.getPrice()>minPrice&& order.getNumOfDevices() + ordersToday <= 50) {

										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
										myAgent.send(reply);
										ordersToday+=order.getNumOfDevices();










									}
									else {
										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
										reply.setContent("No");
										myAgent.send(reply);
									}



								}
								else {

									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
									reply.setContent("No");
									myAgent.send(reply);	

								}

							}





















							//If it is a phablet

							else {
								
								int minPrice=250;
								if(device.getRAM().getSerialNumber()==301) {minPrice+=20;}
								else {minPrice+=35;}
								if(device.getStorage().getSerialNumber()==401) {minPrice+=15;}
								else {minPrice+=40;}
								if(order.getDueDate()<4){minPrice+=50;}
								
								if(order.getPrice() >= minPrice && order.getNumOfDevices() + ordersToday <= 50){

									if(order.getDueDate() >= 4) {

										orderManager.put(orderNum, order);
										orderNum++;
										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
										myAgent.send(reply);
										ordersToday+=order.getNumOfDevices();











									}
									else if(order.getPrice()>minPrice&& order.getNumOfDevices() + ordersToday <= 50) {
										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
										myAgent.send(reply);
										ordersToday+=order.getNumOfDevices();

										orderManager.put(orderNum, order);
										orderNum++;





									}
									else {
										ACLMessage reply = msg.createReply();
										reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
										reply.setContent("No");
										myAgent.send(reply);
									}
								}
								else {

									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
									reply.setContent("No");
									myAgent.send(reply);	

								}}

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
			else {
				block();
			}
		}

		@Override
		public boolean done() {
			if (messages >= 3) { messages=0;return true;}			return false;
		}

	}



	public class orderParts extends Behaviour {

		public orderParts(Agent a) {
			super(a);
		}

		@Override
		public void action() {


			looped=false;
			for(int i=0; i < orderManager.size();i++) {

				Customer_Order order = orderManager.get(i);
				if(order!=null) {
					int orderDate= (int) order.getDueDate();
					//System.out.println(order.getDueDate());

					if (orderDate==1){


						if(order.getDevice() instanceof Smartphone) {

							ACLMessage enquiry2 = new ACLMessage(ACLMessage.REQUEST);
							enquiry2.addReceiver(seller1AID);
							enquiry2.setLanguage(codec.getName());
							enquiry2.setOntology(ontology.getName());

							Manufacturer_Order screen = new Manufacturer_Order();
							screen.setComponent(order.getSmartphone().getScreen());
							int numOfDevices = (int) Math.round(order.getNumOfDevices());
							screen.setNumOfComponents(numOfDevices);
							screen.setDaysLeft(today+1);
							Action request2 = new Action();
							request2.setAction(screen);
							request2.setActor(seller1AID);
							// the agent that you request to perform the action
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(enquiry2, request2); //send the wrapper object
								send(enquiry2);
								profit-=150*order.getNumOfDevices();

							}
							catch (CodecException cex) {
								cex.printStackTrace();

							}
							catch (OntologyException oe) {
								oe.printStackTrace();
								System.out.println(oe.toString());
							}






							ACLMessage enquiry3 = new ACLMessage(ACLMessage.REQUEST);
							enquiry3.addReceiver(seller1AID);
							enquiry3.setLanguage(codec.getName());
							enquiry3.setOntology(ontology.getName());

							Manufacturer_Order battery = new Manufacturer_Order();
							battery.setDaysLeft(today+1);
							battery.setNumOfComponents(numOfDevices);

							battery.setComponent(order.getSmartphone().getBattery());
							Action request3 = new Action();
							request3.setAction(battery);
							request3.setActor(seller1AID);
							// the agent that you request to perform the action
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(enquiry3, request3); //send the wrapper object
								send(enquiry3);
								profit-=100*order.getNumOfDevices();

							}
							catch (CodecException cex) {
								cex.printStackTrace();

							}
							catch (OntologyException oe) {
								oe.printStackTrace();
								System.out.println(oe.toString());
							}

						}
						else {

							ACLMessage enquiry2 = new ACLMessage(ACLMessage.REQUEST);
							enquiry2.addReceiver(seller1AID);
							enquiry2.setLanguage(codec.getName());
							enquiry2.setOntology(ontology.getName());

							Manufacturer_Order screen = new Manufacturer_Order();
							screen.setDaysLeft(today+1);
							int numOfDevices = (int) Math.round(order.getNumOfDevices());
							screen.setNumOfComponents(numOfDevices);
							screen.setComponent(order.getPhablet().getScreen());
							Action request2 = new Action();
							request2.setAction(screen);
							request2.setActor(seller1AID);
							// the agent that you request to perform the action
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(enquiry2, request2); //send the wrapper object
								send(enquiry2);
								profit-=150*order.getNumOfDevices();
								numberOfRequests1++;


							}
							catch (CodecException cex) {
								cex.printStackTrace();

							}
							catch (OntologyException oe) {
								oe.printStackTrace();
								System.out.println(oe.toString());
							}






							ACLMessage enquiry3 = new ACLMessage(ACLMessage.REQUEST);
							enquiry3.addReceiver(seller1AID);
							enquiry3.setLanguage(codec.getName());
							enquiry3.setOntology(ontology.getName());

							Manufacturer_Order battery = new Manufacturer_Order();
							battery.setComponent(order.getPhablet().getBattery());
							battery.setDaysLeft(today+1);
							battery.setNumOfComponents(numOfDevices);

							Action request3 = new Action();
							request3.setAction(battery);
							request3.setActor(seller1AID);
							// the agent that you request to perform the action
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(enquiry3, request3); //send the wrapper object
								send(enquiry3);
								profit-=100*order.getNumOfDevices();
								numberOfRequests1++;

							}
							catch (CodecException cex) {
								cex.printStackTrace();

							}
							catch (OntologyException oe) {
								oe.printStackTrace();
								System.out.println(oe.toString());
							}

						}


						order.setDueDate(orderDate-1);
						orderManager.put(i, order);

					}
					else if(orderDate==4) {	


						ACLMessage storage = new ACLMessage(ACLMessage.REQUEST);
						storage.addReceiver(seller2AID);
						storage.setLanguage(codec.getName());
						storage.setOntology(ontology.getName());

						Manufacturer_Order query = new Manufacturer_Order();
						query.setComponent(order.getDevice().getStorage());
						query.setDaysLeft(today+4);
						int numOfDevices = (int) Math.round(order.getNumOfDevices());

						query.setNumOfComponents(numOfDevices);


						Action request = new Action();
						request.setAction(query);
						request.setActor(seller2AID);
						// the agent that you request to perform the action
						try {
							// Let JADE convert from Java objects to string
							getContentManager().fillContent(storage, request); //send the wrapper object
							send(storage);
							profit-=40*order.getNumOfDevices();
							numberOfRequests2++;


						}
						catch (CodecException cex) {
							cex.printStackTrace();

						}
						catch (OntologyException oe) {
							oe.printStackTrace();
							System.out.println(oe.toString());
						}



						ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
						enquiry.addReceiver(seller2AID);
						enquiry.setLanguage(codec.getName());
						enquiry.setOntology(ontology.getName());

						Manufacturer_Order ram = new Manufacturer_Order();
						ram.setComponent(order.getDevice().getRAM());
						ram.setDaysLeft(today+4);
						ram.setNumOfComponents(numOfDevices);

						Action request1 = new Action();
						request1.setAction(ram);
						request1.setActor(seller2AID);
						// the agent that you request to perform the action
						try {
							// Let JADE convert from Java objects to string
							getContentManager().fillContent(enquiry, request1); //send the wrapper object
							send(enquiry);
							profit-=35*order.getNumOfDevices();
							numberOfRequests2++;


						}
						catch (CodecException cex) {
							cex.printStackTrace();

						}
						catch (OntologyException oe) {
							oe.printStackTrace();
							System.out.println(oe.toString());
						}


						order.setDueDate(orderDate-1);
						orderManager.put(i, order);} 

					else if(orderDate==0) {

						if(order.getDevice() instanceof Smartphone) {

							if(warehouseHM.get(order.getDevice().getRAM().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(order.getDevice().getStorage().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(((Smartphone) order.getDevice()).getBattery().getSerialNumber())>=order.getNumOfDevices()&&warehouseHM.get(((Smartphone) order.getDevice()).getScreen().getSerialNumber())>=order.getNumOfDevices())
							{
								Device device = order.getDevice();
								int num =  warehouseHM.get(device.getRAM().getSerialNumber());
								warehouseHM.put(device.getRAM().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(device.getStorage().getSerialNumber());
								warehouseHM.put(device.getStorage().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(((Smartphone) device).getBattery().getSerialNumber());
								warehouseHM.put(((Smartphone) device).getBattery().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(((Smartphone) device).getScreen().getSerialNumber());
								warehouseHM.put(((Smartphone) device).getScreen().getSerialNumber(),num-(int)order.getNumOfDevices());
								profit+=order.getPrice()*order.getNumOfDevices();
								ACLMessage enquiry = new ACLMessage(ACLMessage.INFORM);
								enquiry.addReceiver(order.getBuyer());
								enquiry.setLanguage(codec.getName());
								enquiry.setOntology(ontology.getName());


								Action request1 = new Action();
								request1.setAction(order);
								request1.setActor(buyer);
								// the agent that you request to perform the action
								try {
									// Let JADE convert from Java objects to string
									getContentManager().fillContent(enquiry, request1); //send the wrapper object
									send(enquiry);
								}
								catch (CodecException cex) {
									cex.printStackTrace();

								}
								catch (OntologyException oe) {
									oe.printStackTrace();
									System.out.println(oe.toString());
								}

								orderManager.remove(i, order);

							}
						}
						else 
							if(warehouseHM.get(order.getDevice().getRAM().getSerialNumber())>=(int)order.getNumOfDevices()&&warehouseHM.get(order.getDevice().getStorage().getSerialNumber())>=(int)order.getNumOfDevices()&&warehouseHM.get(((Phablet) order.getDevice()).getBattery().getSerialNumber())>=(int)order.getNumOfDevices()&&warehouseHM.get(((Phablet) order.getDevice()).getScreen().getSerialNumber())>=(int)order.getNumOfDevices())
							{
								Device device = order.getDevice();

								int num =  warehouseHM.get(device.getRAM().getSerialNumber());
								warehouseHM.put(device.getRAM().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(device.getStorage().getSerialNumber());
								warehouseHM.put(device.getStorage().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(((Phablet) device).getBattery().getSerialNumber());
								warehouseHM.put(((Phablet) device).getBattery().getSerialNumber(),num-(int)order.getNumOfDevices());
								num =  warehouseHM.get(((Phablet) device).getScreen().getSerialNumber());
								warehouseHM.put(((Phablet) device).getScreen().getSerialNumber(),num-(int)order.getNumOfDevices());
								profit+=order.getPrice()*order.getNumOfDevices();
								Delivered delivered = new Delivered();
								delivered.setOrder(order);
								ACLMessage enquiry = new ACLMessage(ACLMessage.INFORM);
								enquiry.addReceiver(order.getBuyer());
								enquiry.setLanguage(codec.getName());
								enquiry.setOntology(ontology.getName());


								try {
								    myAgent.getContentManager().fillContent(enquiry, delivered);
								    myAgent.send(enquiry);
								} catch (Exception e) {
								     throw new RuntimeException("cannot fill message.", e);
								}

								orderManager.remove(i, order);

							}
							else {
								
								profit-=order.getPenalty();

							}


					}
					else {
						order.setDueDate(orderDate-1);
						orderManager.put(i, order);
					}


				}}

			looped =true;

		}

		@Override
		public boolean done() {
			if(looped==true) {
				return true;
			}

			return false;
		}
	}


	/*
	public class SelectOrder extends Behaviour{
		int highestOrder;
		boolean loop1;
		public SelectOrder(Agent a) {
			super(a);
		}

		@Override
		public void action() {

			loop1=false;
			for(int i=0; i < potentialOrders.size();i++) {

				Customer_Order order = potentialOrders.get(i);
				if(order!=null) {
					int orderDate= (int) order.getDueDate();
					if(orderDate>=4 && (int)order.getNumOfDevices()<=50) 
					{
						int potentialProfit = (int) (order.getPrice() * order.getNumOfDevices());
						if(order.getDevice().getRAM().getSerialNumber()==301) 
						{
							potentialProfit-=20*order.getNumOfDevices();
						}
						else {
							potentialProfit-=35*order.getNumOfDevices();

						}


						if(order.getDevice().getStorage().getSerialNumber()==401) 
						{
							potentialProfit-=15*order.getNumOfDevices();
						}
						else {
							potentialProfit-=40*order.getNumOfDevices();

						}

						if(order.getDevice() instanceof Smartphone) 
						{

							potentialProfit-=100*order.getNumOfDevices();
							potentialProfit-=70*order.getNumOfDevices();
						}
						else {
							potentialProfit-=150*order.getNumOfDevices();
							potentialProfit-=100*order.getNumOfDevices();


						}
						orderMap.put(i, potentialProfit);


					}

					else if( order.getDueDate()>0&&(int)order.getNumOfDevices()<=50)
					{

						int potentialProfit = (int) (order.getPrice() * order.getNumOfDevices());
						if(order.getDevice().getRAM().getSerialNumber()==301) 
						{
							potentialProfit-=30*order.getNumOfDevices();
						}
						else {
							potentialProfit-=60*order.getNumOfDevices();

						}


						if(order.getDevice().getStorage().getSerialNumber()==401) 
						{
							potentialProfit-=25*order.getNumOfDevices();
						}
						else {
							potentialProfit-=50*order.getNumOfDevices();

						}

						if(order.getDevice() instanceof Smartphone) 
						{

							potentialProfit-=100*order.getNumOfDevices();
							potentialProfit-=70*order.getNumOfDevices();
						}
						else {
							potentialProfit-=150*order.getNumOfDevices();
							potentialProfit-=100*order.getNumOfDevices();


						}
						orderMap.put(i, potentialProfit);

					}
					else
					{
						ACLMessage enquiry = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						enquiry.addReceiver(order.getBuyer());
						enquiry.setLanguage(codec.getName());
						enquiry.setOntology(ontology.getName());
						Action request1 = new Action();
						request1.setAction(order);
						request1.setActor(order.getBuyer());
						// the agent that you request to perform the action
						try {
							// Let JADE convert from Java objects to string
							getContentManager().fillContent(enquiry, request1); //send the wrapper object
							send(enquiry);


						}
						catch (CodecException cex) {
							cex.printStackTrace();

						}
						catch (OntologyException oe) {
							oe.printStackTrace();
							System.out.println(oe.toString());
						}
						orderMap.put(i,0);
					}


				}


			}
			loop1=true;



		}

		@Override
		public boolean done() {

			if (loop1=true){
				int maxOrder =1000;
				int maxProfit =0;
				for(int i=0; i < orderMap.size();i++) {
					if(maxProfit<orderMap.get(i)) {
						maxOrder=i;
					}

				}
				if(maxOrder!=1000) {
					Customer_Order order = potentialOrders.get(maxOrder);
					orderManager.put(orderNum,order);
					orderNum++;
					ACLMessage enquiry = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					enquiry.addReceiver(order.getBuyer());
					enquiry.setLanguage(codec.getName());
					enquiry.setOntology(ontology.getName());
					Action request1 = new Action();
					request1.setAction(order);
					request1.setActor(order.getBuyer());
					// the agent that you request to perform the action
					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(enquiry, request1); //send the wrapper object
						send(enquiry);


					}
					catch (CodecException cex) {
						cex.printStackTrace();

					}
					catch (OntologyException oe) {
						oe.printStackTrace();
						System.out.println(oe.toString());
					}

				int order2=1000;
				int maxprofit2=0;
				for(int i=0; i < orderMap.size();i++) {

					if(potentialOrders.get(i)!=potentialOrders.get(maxOrder)&&potentialOrders.get(maxOrder).getNumOfDevices()+potentialOrders.get(i).getNumOfDevices()<=50) {}
					{
						if (maxprofit2<potentialOrders.get(i).getPrice()*potentialOrders.get(i).getNumOfDevices()) {
							order2=i;
						}
					}

				}
				if(order2<1000) {
					Customer_Order orderr = potentialOrders.get(order2);
					orderManager.put(orderNum,orderr);
					orderNum++;

					ACLMessage enquiryy = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					enquiryy.addReceiver(orderr.getBuyer());
					enquiryy.setLanguage(codec.getName());
					enquiryy.setOntology(ontology.getName());
					Action request = new Action();
					request.setAction(orderr);
					request.setActor(orderr.getBuyer());
					// the agent that you request to perform the action
					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(enquiryy, request); //send the wrapper object
						send(enquiryy);


					}
					catch (CodecException cex) {
						cex.printStackTrace();

					}
					catch (OntologyException oe) {
						oe.printStackTrace();
						System.out.println(oe.toString());
					}




				int order3=1000;
				int maxprofit3=0;
				for(int i=0; i < orderMap.size();i++) {

					if(potentialOrders.get(i)!=potentialOrders.get(maxOrder)&&potentialOrders.get(i)!=potentialOrders.get(order2)&&potentialOrders.get(maxOrder).getNumOfDevices()+potentialOrders.get(i).getNumOfDevices()+potentialOrders.get(order2).getNumOfDevices()<=50) {}
					{
						if (maxprofit3<potentialOrders.get(i).getPrice()*potentialOrders.get(i).getNumOfDevices()) {
							order3=i;
						}
					}

				}
				if(order3<1000) {
					Customer_Order orderC = potentialOrders.get(order3);
					orderManager.put(orderNum,orderC);
					orderNum++;

					ACLMessage enquiry1 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					enquiry1.addReceiver(order.getBuyer());
					enquiry1.setLanguage(codec.getName());
					enquiry1.setOntology(ontology.getName());
					Action request2 = new Action();
					request2.setAction(orderC);
					request2.setActor(orderC.getBuyer());
					// the agent that you request to perform the action
					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(enquiry1, request2); //send the wrapper object
						send(enquiry1);


					}
					catch (CodecException cex) {
						cex.printStackTrace();

					}
					catch (OntologyException oe) {
						oe.printStackTrace();
						System.out.println(oe.toString());
					}
				}

			}

			}
				potentialOrders.clear();
				orderMap.clear();
				return true;
			}

			// TODO Auto-generated method stub
			return loop1;
		}
	}
	 */














	public class DeliveredServer extends Behaviour {

		private boolean loop;
		private int numOfReplies;

		public DeliveredServer(Agent a) {
			super(a);
			loop = false;	
			numOfReplies=0;
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				if (msg.getContent().equals("done")){
					numOfReplies++;
					if (numOfReplies==2){
						loop = true;

					}


				}
				else {
					try {
						ContentManager cm = myAgent.getContentManager();
						ContentElement ce = cm.extractContent(msg);


						if(ce instanceof Recieved) {
							//AgentAction act = (AgentAction) ((Action) ce).getAction();
							Recieved act = (Recieved)ce;
							if (act.getOrder() != null) {
								Manufacturer_Order order = act.getOrder();
								if(warehouseHM.get(order.getComponent().getSerialNumber()) != null) {
									int numberOfComponents=warehouseHM.get(order.getComponent().getSerialNumber());
									warehouseHM.put(order.getComponent().getSerialNumber(), numberOfComponents + order.getNumOfComponents());
								}
								else {

									warehouseHM.put(order.getComponent().getSerialNumber(),  order.getNumOfComponents());
								}
							}
						}
						else {
							System.out.println("Its not an order");

						}

					}


					catch (CodecException ce) {
						ce.printStackTrace();
						System.out.println("1");
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
						System.out.println("2");
					}
				}
			}



		}

		@Override
		public boolean done() {

			return loop;
		}
	}















	/*




		public class endServer extends Behaviour {

			private boolean loop;
			private int numOfReplies;

			public endServer(Agent a) {
				super(a);
				loop = false;	
				numOfReplies=0;
			}

			@Override
			public void action() {

				MessageTemplate messt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
				ACLMessage messg = myAgent.receive(messt);
				if (messg!=null && messg.getContent().equals("done")){
					numOfReplies++;
					System.out.println("Testing");
					if (numOfReplies==2){
						loop = true;
						if(warehouseHM.size()!=0) {
							System.out.println(warehouseHM);
						}
					}


				}
				else {

					block();
				}
			}

			@Override
			public boolean done() {
				myAgent.addBehaviour(new EndDay(myAgent));
				return loop;
			}
		}











	 */





















	public class EndDay extends OneShotBehaviour {

		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for (Integer value : warehouseHM.values()) {
				profit-=value*5;
			}
			System.out.println("Total profit is: "+profit);
			//	System.out.println("The total number of warehouse orders is "+totalOrders);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);

			ACLMessage sellerDone = new ACLMessage(ACLMessage.QUERY_IF);
			sellerDone.setContent("done");
			sellerDone.addReceiver(seller1AID);
			sellerDone.addReceiver(seller2AID);
			myAgent.send(sellerDone);
			today++;


		}

	}

}
