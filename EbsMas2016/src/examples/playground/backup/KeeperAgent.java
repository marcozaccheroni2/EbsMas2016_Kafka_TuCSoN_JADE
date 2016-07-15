package examples.playground.backup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import alice.logictuple.LogicTuple;
import alice.logictuple.exceptions.InvalidLogicTupleException;
import alice.tucson.api.TucsonTupleCentreId;
import alice.tucson.api.exceptions.TucsonInvalidAgentIdException;
import alice.tucson.api.exceptions.TucsonInvalidTupleCentreIdException;
import alice.tucson.api.exceptions.TucsonOperationNotPossibleException;
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
import jade.core.behaviours.CyclicBehaviour;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka4jade.KafkaConsumerAssistant;
import utils.Json;

public class KeeperAgent extends Agent {

	private void log(final String msg) {
        System.err.println("[" + this.getLocalName() + "]: " + msg);
        this.debug.log(this.getLocalName(), msg);
    }
	
	/** */
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
    
    private final String zkHosts = "localhost:2181";
	private final int zkSessionTimeoutMs = 15 * 1000;
    private final int zkConnectionTimeoutMs = 10 * 1000;
    private final ZkClient zkClient = new ZkClient(zkHosts, zkSessionTimeoutMs, zkConnectionTimeoutMs, ZKStringSerializer$.MODULE$);
    private final ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zkHosts), false);
    
    private final String playgroundTopic = "playgroundTopic";
    
    private DebugGUI debug;
    private int players_per_team;
    
    private int team_white_score = 0;
    private int team_black_score = 0;
    
    public KeeperAgent(DebugGUI debug, int players_per_team) {
		this.debug = debug;
		this.players_per_team = players_per_team;
	}
    
	@Override
    protected void setup() {
        this.log("The Playground is now open!");
        
        try {
            /*
             * First of all, get the helper for the service you want to exploit
             */
            this.helper = (TucsonHelper) this.getHelper(TucsonService.NAME);
            /*
             * Then, start a TuCSoN Node (if not already up) as the actual
             * executor of the service
             */
            if (!this.helper.isActive("localhost", 20504, 10000)) {
                this.log("Booting local TuCSoN Node on default port...");
                this.helper.startTucsonNode(20504);
            }
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
            
            for (int i = 0; i < this.players_per_team; i++) {
        		LogicTuple intention = LogicTuple.parse("team('WHITE')");
                final Out out = new Out(this.tcid, intention);
                this.bridge.asynchronousInvocation(out);
			}
            
        	for (int i = 0; i < this.players_per_team; i++) {
        		LogicTuple intention = LogicTuple.parse("team('BLACK')");
                final Out out = new Out(this.tcid, intention);
                this.bridge.asynchronousInvocation(out);
			}
        	
        	LogicTuple intention = LogicTuple.parse("ball(1)");
            final Out out = new Out(this.tcid, intention);
            this.bridge.asynchronousInvocation(out);
        	
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
        } catch (final TucsonOperationNotPossibleException e) {
            this.log(">>> TuCSoN Node cannot be installed, check if given port is already in use <<<");
            this.doDelete();
        } catch (InvalidLogicTupleException e) {
        	// should not happen
            e.printStackTrace();
            this.doDelete();
		} catch (Exception e){
			// should not happen
            e.printStackTrace();
            this.doDelete();
		}
        
        int partitions = 1;
        int replication = 1;
        Properties topicConfiguration = new Properties();
        
        if (!AdminUtils.topicExists(zkUtils, this.playgroundTopic)){
        	AdminUtils.createTopic(zkUtils, this.playgroundTopic, partitions, replication, topicConfiguration, null);
        }

        this.addBehaviour(new ObserveGameBehaviour());
        
    }
	
	private class ObserveGameBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		private KafkaConsumerAssistant kca;
		
		public ObserveGameBehaviour() {
			
			kca = new KafkaConsumerAssistant(this, KeeperAgent.this.getAID().getName());
			kca.subscribe(KeeperAgent.this.playgroundTopic);
        }
		
        @Override
		public void action() {
        	
        	ConsumerRecords<String, String> records = kca.consume(1000);
        	if (records.count() != 0){
        		
        		for (ConsumerRecord<String, String> record : records) {
        			KeeperAgent.this.log(record.value());
        			try {
						JsonNode message = Json.parse(record.value());
						switch (message.get("type").asText()) {
						case "shot_made":
							
							if ("'WHITE'".equals(message.get("team").asText())) {
								KeeperAgent.this.team_white_score += 2;
							} else if ("'BLACK'".equals(message.get("team").asText())) {
								KeeperAgent.this.team_black_score += 2;
							}
							
							KeeperAgent.this.log("WHITE " + KeeperAgent.this.team_white_score +
									" - " +
									"BLACK " + KeeperAgent.this.team_black_score);
							
							break;
						
						default:
							break;
						}
						
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
        		}
        	}
			
		}
		
	}
	
	/*
     * Remember to deregister the services offered by the agent upon shutdown,
     * because the JADE platform does not do it by itself!
     */
    @Override
    protected void takeDown() {
    	
    	this.log("The playground is now closed!");
    	if (this.helper.isActive("localhost", 20504, 10000)) {
            this.log("Stopping local TuCSoN Node on default port...");
            try {
            	this.helper.stopTucsonNode(20504);
            } catch(Exception ex) {
			    ex.printStackTrace();
			}
        }
    	
    	AdminUtils.deleteTopic(zkUtils, this.playgroundTopic);
    	
    }
	
}
