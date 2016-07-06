package examples.playground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import alice.logictuple.LogicTuple;
import alice.logictuple.exceptions.InvalidLogicTupleException;
import alice.tucson.api.TucsonTupleCentreId;
import alice.tucson.api.exceptions.TucsonInvalidAgentIdException;
import alice.tucson.api.exceptions.TucsonInvalidTupleCentreIdException;
import alice.tucson.asynchSupport.actions.ordinary.Inp;
import alice.tucson.asynchSupport.actions.ordinary.Out;
import alice.tucson.asynchSupport.actions.ordinary.bulk.RdAll;
import alice.tucson.service.TucsonOpCompletionEvent;
import alice.tuplecentre.api.exceptions.InvalidOperationException;

import it.unibo.tucson.jade.coordination.IAsynchCompletionBehaviour;
import it.unibo.tucson.jade.exceptions.CannotAcquireACCException;
import it.unibo.tucson.jade.glue.BridgeToTucson;
import it.unibo.tucson.jade.service.TucsonHelper;
import it.unibo.tucson.jade.service.TucsonService;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import kafka4jade.KafkaConsumerAssistant;
import utils.Json;

public class PlayerAgent extends Agent {
	
	private class PlayGameBehaviour extends TickerBehaviour {
	
		private static final long serialVersionUID = 1L;
		
		private boolean assist_received = false;
		private boolean ball;
		
		private KafkaProducer<String, String> producer;
		
		public PlayGameBehaviour(Agent a, long period, boolean ball) {
			super(a, period);
			this.ball = ball;
			
			Properties producer_props = new Properties();
            producer_props.put("bootstrap.servers", "localhost:9092");
            producer_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        	
        	this.producer = new KafkaProducer<String, String>(producer_props);
			
			PlayerAgent.this.addBehaviour(new ObserveGameBehaviour(PlayerAgent.this, 1000));
			PlayerAgent.this.log("TEAMMATES: [" + String.join(", ", PlayerAgent.this.teammates) + "]");
			PlayerAgent.this.log("OPPONENTS: [" + String.join(", ", PlayerAgent.this.opponents) + "]");
		}
		
		private void shoot() {
			String shot;
			String ball_to_opponent;
			
			int shot_p = new Random().nextInt(100);
			if (shot_p + mood > 40){
				shot = "shot_missed";
				ball_to_opponent = "rebound";
			} else {
				shot = "shot_made";
				ball_to_opponent = "after_shot_made";
			}
			
			Json msg = new Json();
			msg.add("type", shot);
			msg.add("player", PlayerAgent.this.getAID().getName());
			msg.add("team", PlayerAgent.this.team);
			
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(PlayerAgent.this.playgroundTopic, msg.toString());
			this.producer.send(record);
			
			String opponent = PlayerAgent.this.opponents.get(new Random().nextInt(PlayerAgent.this.opponents.size()));
			
			PlayerAgent.this.log("Rebound to " + opponent);
			
			ACLMessage ball = new ACLMessage(ACLMessage.PROPOSE);
			ball.addReceiver(new AID(opponent, AID.ISGUID));
			ball.setOntology("ball");
			ball.setContent(ball_to_opponent);
			send(ball);
		}
		
		private void pass(String target_agent) {
			
			String pass_quality;
			
			int pass_quality_p = new Random().nextInt(100);
			if (pass_quality_p + mood > 20){
				pass_quality = "pass";
			} else {
				pass_quality = "assist";
			}
			
			Json msg = new Json();
			msg.add("type", pass_quality);
			msg.add("player", PlayerAgent.this.getAID().getName());
			msg.add("to", target_agent);
			
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(PlayerAgent.this.playgroundTopic, msg.toString());
			this.producer.send(record);
		
			ACLMessage ball = new ACLMessage(ACLMessage.PROPOSE);
			ball.addReceiver(new AID(target_agent, AID.ISGUID));
			ball.setOntology("ball");
			ball.setContent(pass_quality);
			send(ball);
			
		}
		
		@Override
		protected void onTick() {
			
			if (this.ball) {
				
				if (this.assist_received){
					
					this.shoot();
					this.assist_received = false;
					
				} else {
					
					int index = new Random().nextInt(PlayerAgent.this.teammates.size());
					String target_agent = PlayerAgent.this.teammates.get(index);
					
					if (target_agent.equals(PlayerAgent.this.getAID().getName())){
						
						// The agent will shoot
						
						this.shoot();
	    				
					} else {
						
						// Agent will pass the ball
						
						this.pass(target_agent);
	    				
					}
					
				}
				
				this.ball = false;
				
			} else {
				
				final ACLMessage msg = this.myAgent.receive(MessageTemplate.MatchOntology("ball"));
				if (msg != null) {
					String msg_content = msg.getContent();
					if ("rebound".equals(msg_content)){
						Json msg2 = new Json();
						msg2.add("type", msg_content);
						msg2.add("player", PlayerAgent.this.getAID().getName());
						msg2.add("team", PlayerAgent.this.team);
						
						ProducerRecord<String, String> record = new ProducerRecord<String, String>(PlayerAgent.this.playgroundTopic, msg2.toString());
						this.producer.send(record);
						PlayerAgent.this.log("Rebound taken");
					} else if ("assist".equals(msg_content)) {
						PlayerAgent.this.log("Assist received");
						this.assist_received = true;
					} else if ("pass".equals(msg_content)) {
						PlayerAgent.this.log("Pass received");
					}
					
					this.ball = true;
				}
				
			}
			
			
		}
		
	}
	
	private class ArrivedToPlaygroundBehaviour extends WakerBehaviour {
		
		public ArrivedToPlaygroundBehaviour(Agent a, long timeout) {
			super(a, timeout);
		}

		private static final long serialVersionUID = 1L;
		
		@Override
		protected void onWake() { 
    		/*
             * Arrived to the playground, the player will declare his intention to play
             */
    		try {
                PlayerAgent.this.log("I am arrived to the playground");
                LogicTuple team = LogicTuple.parse("team(S)");
                final Inp inp = new Inp(PlayerAgent.this.tcid, team);
                PlayerAgent.this.bridge.asynchronousInvocation(inp, new DraftBehaviour(PlayerAgent.this, 1000), PlayerAgent.this);
            } catch (final InvalidLogicTupleException e) {
                // should not happen
                e.printStackTrace();
                PlayerAgent.this.doDelete();
            }
    	}
	}
	
	public class DraftBehaviour extends TickerBehaviour implements IAsynchCompletionBehaviour {
		
		private static final long serialVersionUID = 1L;
        private TucsonOpCompletionEvent res;

        public DraftBehaviour(Agent a, long period) {
			super(a, period);
		}
        
		public void onTick(){
			if (this.res != null) {
				if (this.res.resultOperationSucceeded()){
					
					PlayerAgent.this.team = this.res.getTuple().getArg(0).toString();
					PlayerAgent.this.log("I will play for " + PlayerAgent.this.team);
					
					LogicTuple player;
					try {
					
						player = LogicTuple.parse("player(name('" + PlayerAgent.this.getAID().getName() + "'),team(" + PlayerAgent.this.team + "))");
					
						final Out out = new Out(PlayerAgent.this.tcid, player);
						PlayerAgent.this.bridge.asynchronousInvocation(out);
					
					} catch (InvalidLogicTupleException e) {
						e.printStackTrace();
						PlayerAgent.this.doDelete();
					} catch (ServiceException e) {
						e.printStackTrace();
						PlayerAgent.this.doDelete();
					}
					
			        PlayerAgent.this.addBehaviour(new WaitToPlayBehaviour(PlayerAgent.this, 1000));
					
				} else {
					PlayerAgent.this.log("Oh...ok, then I will just watch the game...");
					PlayerAgent.this.addBehaviour(new ObserveGameBehaviour(PlayerAgent.this, 1000));
				}

		        this.stop();
            }
		}
		
		public void setTucsonOpCompletionEvent(TucsonOpCompletionEvent ev) {
			this.res = ev;
		}
		
	}
	
	private class TryToGetTheBallBehaviour extends OneShotBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void action() { 
    		/*
             * Try to get the ball
             */
    		try {
                LogicTuple ball = LogicTuple.parse("ball(1)");
                final Inp inp = new Inp(PlayerAgent.this.tcid, ball);
                PlayerAgent.this.bridge.asynchronousInvocation(inp, new BallResultBehaviour(PlayerAgent.this, 1000), PlayerAgent.this);
            } catch (final InvalidLogicTupleException e) {
                // should not happen
                e.printStackTrace();
                PlayerAgent.this.doDelete();
            }
    	}
	}
	
	public class BallResultBehaviour extends TickerBehaviour implements IAsynchCompletionBehaviour{
		
		private static final long serialVersionUID = 1L;
        private TucsonOpCompletionEvent res;

        public BallResultBehaviour(Agent a, long period) {
			super(a, period);
		}
        
		public void onTick(){
			if (this.res != null) {
				
				boolean ball = false;
				
				if (this.res.resultOperationSucceeded()){

					PlayerAgent.this.log("The ball is MINE!");
					ball = true;
					
				}

				PlayerAgent.this.addBehaviour(new PlayGameBehaviour(PlayerAgent.this, 5000, ball));
		        this.stop();
            }
		}

		public void setTucsonOpCompletionEvent(TucsonOpCompletionEvent ev) {
			this.res = ev;
		}
		
	}
	
	private class WaitToPlayBehaviour extends TickerBehaviour {
		
		public WaitToPlayBehaviour(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 1L;
		
		protected void onTick(){
			LogicTuple players_white;
			LogicTuple players_black;
			LogicTuple missing_players;
            TucsonOpCompletionEvent res_white = null;
            TucsonOpCompletionEvent res_black = null;
            TucsonOpCompletionEvent res_missing = null;
            try {
            	players_white = LogicTuple.parse("player(name(S), team('WHITE'))");
            	players_black = LogicTuple.parse("player(name(S), team('BLACK'))");
            	missing_players = LogicTuple.parse("team(S)");
            	
                final RdAll rdall_white = new RdAll(PlayerAgent.this.tcid, players_white);
                final RdAll rdall_black = new RdAll(PlayerAgent.this.tcid, players_black);
                final RdAll rdall_missing = new RdAll(PlayerAgent.this.tcid, missing_players);
                res_white = PlayerAgent.this.bridge.synchronousInvocation(
                		rdall_white, null, this);
                res_black = PlayerAgent.this.bridge.synchronousInvocation(
                		rdall_black, null, this);
                res_missing = PlayerAgent.this.bridge.synchronousInvocation(
                		rdall_missing, null, this);
            } catch (final InvalidLogicTupleException e) {
                // should not happen
                e.printStackTrace();
                PlayerAgent.this.doDelete();
            } catch (final ServiceException e) {
            	PlayerAgent.this.log(">>> No TuCSoN service active, reboot JADE with -services it.unibo.tucson.jade.service.TucsonService option <<<");
            	PlayerAgent.this.doDelete();
            }
            if (res_white != null && res_black != null && res_missing != null) {
                try {
                	if (res_white.getTupleList().size() == res_black.getTupleList().size() && res_missing.getTupleList().size() == 0){
                		
                		// There are enough players to start the game 
                		PlayerAgent.this.log("It's time to play!");
                		
                		for (LogicTuple tuple : res_white.getTupleList()) {
                			
                			String tuple_player_name = tuple.getArg(0).getArg(0).toString();
                			tuple_player_name = StringUtils.stripStart(tuple_player_name, "'");
                			tuple_player_name = StringUtils.stripEnd(tuple_player_name, "'");
                			
                			String tuple_team_name = tuple.getArg(1).getArg(0).toString();
                			
                			if (tuple_team_name.equals(PlayerAgent.this.team)) {
								PlayerAgent.this.teammates.add(tuple_player_name);
							} else {
								PlayerAgent.this.opponents.add(tuple_player_name);
							}
						}
                		
                		for (LogicTuple tuple : res_black.getTupleList()) {
                			
                			String tuple_player_name = tuple.getArg(0).getArg(0).toString();
                			tuple_player_name = StringUtils.stripStart(tuple_player_name, "'");
                			tuple_player_name = StringUtils.stripEnd(tuple_player_name, "'");
                			
                			String tuple_team_name = tuple.getArg(1).getArg(0).toString();
                			
                			if (tuple_team_name.equals(PlayerAgent.this.team)) {
								PlayerAgent.this.teammates.add(tuple_player_name);
							} else {
								PlayerAgent.this.opponents.add(tuple_player_name);
							}
						}
                		
                		PlayerAgent.this.addBehaviour(new TryToGetTheBallBehaviour());
                		this.stop();
                		
                	} else {
                		PlayerAgent.this.log("Still waiting...");
                	}
                	PlayerAgent.this.bridge.clearTucsonOpResult(this);
                } catch (final InvalidOperationException e) {
                    // should not happen
                    e.printStackTrace();
                    PlayerAgent.this.doDelete();
                }
            }
		}
		
	}
	
	private class ObserveGameBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		private KafkaConsumerAssistant kca;
		private PlayerAgent myAgent;
		
		public ObserveGameBehaviour(PlayerAgent a, long period) {
			super(a, period);
			
			myAgent = a;
			
			kca = new KafkaConsumerAssistant(this, myAgent.getAID().getName());
			kca.subscribe(myAgent.playgroundTopic);
		}
		
		@Override
		public void onTick() {
        	ConsumerRecords<String, String> records = kca.consume(1000);
        	if (records.count() != 0){
        		for (ConsumerRecord<String, String> record : records) {
        			PlayerAgent.this.log(record.value());
        			if (PlayerAgent.this.team != null){
        				try {
							JsonNode message = Json.parse(record.value());
							switch (message.get("type").asText()) {
							case "shot_made":
								
								if (PlayerAgent.this.teammates.contains(message.get("player").asText())) {
									PlayerAgent.this.mood += 10;
									PlayerAgent.this.log("Yeah! [mood +10]");
								} else {
									PlayerAgent.this.mood -= 10;
									PlayerAgent.this.log("Damn! [mood -10]");
								}
								
								break;
							
							case "shot_missed":
								
								if (PlayerAgent.this.teammates.contains(message.get("player").asText())) {
									PlayerAgent.this.mood -= 5;
									PlayerAgent.this.log("... [mood -5]");
								} else {
									PlayerAgent.this.mood += 5;
									PlayerAgent.this.log("Com'on! [mood +5]");
								}
								
								break;
							
							case "rebound":
								
								if (PlayerAgent.this.teammates.contains(message.get("player").asText())) {
									PlayerAgent.this.mood += 2;
									PlayerAgent.this.log("Good! [mood +2]");
								}
								
								break;
							
							case "assist":
								
								if (PlayerAgent.this.teammates.contains(message.get("player").asText())) {
									PlayerAgent.this.mood += 2;
									PlayerAgent.this.log("Wow! [mood +2]");
								}
								
								break;
								
							case "pass":
								
								if (PlayerAgent.this.teammates.contains(message.get("player").asText())) {
									PlayerAgent.this.mood += 1;
									PlayerAgent.this.log("Keep going! [mood +1]");
								}
								
								break;
							
							default:
								break;
							}
							
							PlayerAgent.this.log("Current mood: " + PlayerAgent.this.mood);
							
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
        			}
        		}
        	}
		}
		
	}

	
	/** serialVersionUID **/
    private static final long serialVersionUID = 1L;
    /*
     * The bridge class to execute TuCSoN operations
     */
    private BridgeToTucson bridge;
    private TucsonHelper helper;
    /*
     * ID of tuple centre used for objective coordination
     */
    private TucsonTupleCentreId tcid;
    
    private final String playgroundTopic = "playgroundTopic";
    
    private String team = null;
    private ArrayList<String> teammates = new ArrayList<String>();
    private ArrayList<String> opponents = new ArrayList<String>();
    
    private DebugGUI debug;
    
    /* If the player takes part in the game, it will change depending of what happens */
    private int mood = 0;
    
    public PlayerAgent(DebugGUI gui) {
		this.debug = gui;
	}
    
    private void log(final String msg) {
        System.err.println("[" + this.getLocalName() + "]: " + msg);
        this.debug.log(this.getLocalName(), msg);
    }

    @Override
    protected void setup() {
        this.log("I'm going to the playground.");
        
        try {
            /*
             * First of all, get the helper for the service you want to exploit
             */
            this.helper = (TucsonHelper) this.getHelper(TucsonService.NAME);
            /*
             * Obtain ACC (which is actually given to the bridge, not directly
             * to your agent)
             */
            this.helper.acquireACC(this);
            /*
             * Get the univocal bridge for the agent. Now, mandatory, set-up
             * actions have been carried out and you are ready to coordinate
             */
            this.bridge = this.helper.getBridgeToTucson(this);
            /*
             * build a tuple centre id
             */
            this.tcid = this.helper.buildTucsonTupleCentreId("default",
                    "localhost", 20504);
            
        } catch (final ServiceException e) {
            this.log(">>> No TuCSoN service active, reboot JADE with -services it.unibo.tucson.jade.service.TucsonService option <<<");
            this.doDelete();
        } catch (final TucsonInvalidAgentIdException e) {
            this.log(">>> TuCSoN Agent ids should be compliant with Prolog sytnax (start with lowercase letter, no special symbols), choose another agent id <<<");
            this.doDelete();
        } catch (final TucsonInvalidTupleCentreIdException e) {
            // should not happen
            e.printStackTrace();
            this.doDelete();
        } catch (final CannotAcquireACCException e) {
            // should not happen
            e.printStackTrace();
            this.doDelete();
        } catch (Exception e){
			// should not happen
            e.printStackTrace();
            this.doDelete();
		}
        
        int seconds = new Random().nextInt(6) + 5; // numero random da 5 a 10
        this.addBehaviour(new ArrivedToPlaygroundBehaviour(this, seconds * 1000));
    }
    
    /*
     * Remember to deregister the services offered by the agent upon shutdown,
     * because the JADE platform does not do it by itself!
     */
    @Override
    protected void takeDown() {
    	this.log("Bye, I have to go home...");
    	if (this.team != null){
    		LogicTuple intention;
			try {
				intention = LogicTuple.parse("team(" + this.team + ")");
				final Out out = new Out(this.tcid, intention);
		        this.bridge.asynchronousInvocation(out);
			} catch (InvalidLogicTupleException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
    	}
    }
}
