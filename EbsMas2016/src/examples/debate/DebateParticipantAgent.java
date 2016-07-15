package examples.debate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka4jade.KafkaConsumerAssistant;

public class DebateParticipantAgent extends Agent {

	private class PayAttentionToModeratorBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		private ObjectMapper mapper = new ObjectMapper();
		private KafkaConsumerAssistant k4j_bridge;
		
		public PayAttentionToModeratorBehaviour() {
			String consumer_group = DebateParticipantAgent.this.getAID().getName();
			k4j_bridge = new KafkaConsumerAssistant(this, consumer_group);
			k4j_bridge.subscribe(topicName);
		}
		
        @Override
		public void action() {
        	DebateParticipantAgent.this.log("[PayAttentionToModeratorBehaviour]");
        	ConsumerRecords<String, String> records = k4j_bridge.consume(Long.MAX_VALUE);
        	if (records.count() != 0){
        		for (ConsumerRecord<String, String> record : records) {
        			try {
						JsonNode msg = mapper.readTree(record.value());
						
						if ("new-argument".equals(msg.get("type").asText())) {
							currentArgument = msg.get("argument").asText();
							DebateParticipantAgent.this.log("[NEW ARGUMENT] " + currentArgument);
						}
						
					} catch (JsonProcessingException e) {
						DebateParticipantAgent.this.log("What?! Moderator seems drunk...");
						e.printStackTrace();
					} catch (IOException e) {
						DebateParticipantAgent.this.log("Ops! Something went wrong...");
						e.printStackTrace();
					}
        		}
        		
        	}
		}
		
	}
	
	private class DebateBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		private String[] questions = {"What do you think about it?", "What is your opinion?", "And you?"};
		private String[] answers = {"I agree", "I disagree", "Absolutely", "Winter is coming"};
		private String[] opinions = {"I know nothing about %s.", "%s is total bullshit!", "A participant has no opinion about %s."};
		
		private final MessageTemplate debateQuestionTemplate = MessageTemplate.and(
				MessageTemplate.MatchOntology("Debate-question"),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		
		public DebateBehaviour(Agent a, long period) {
			super(a, period);
		}
		
		@Override
		public void onTick() {
			
			DebateParticipantAgent.this.log("[DebateBehaviour]");
			
			if ("".equals(currentArgument)) return;
			
			final ACLMessage msg = this.myAgent.receive(debateQuestionTemplate);
			if (msg != null) {
				
				final ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				
				int answer_index = new Random().nextInt(opinions.length);
				reply.setContent(answers[answer_index]);
				
				this.myAgent.send(reply);
				
				DebateParticipantAgent.this.log("[ANSWER TO " + msg.getSender().getLocalName() + "] " + answers[answer_index]);
				
			} else {
				
				/*
                 * 1- Create the agent description template.
                 */
                final DFAgentDescription template = new DFAgentDescription();
                /*
                 * 2- Create the service description template.
                 */
                final ServiceDescription sd = new ServiceDescription();
                /*
                 * 3- Fill its fields you look for.
                 */
                sd.setType("debate-participation");
                /*
                 * 4- Add the service template to the agent template.
                 */
                template.addServices(sd);
                /*
                 * 5- Setup your preferred search constraints.
                 */
                final SearchConstraints all = new SearchConstraints();
                all.setMaxResults(new Long(-1));
                DFAgentDescription[] result = null;
                try {
                    /*
                     * 6- Query the DF about the service you look for.
                     */
                    result = DFService.search(this.myAgent, template, all);
                    if (result.length > 1) {
                    	DebateParticipantAgent.this.participantAgents = new AID[result.length-1];
                    	int participant_index = 0;
                        for (int i = 0; i < result.length; ++i) {
                            /*
                             * 7- Collect found service providers' AIDs. Excluding 
                             */
                        	if (!result[i].getName().equals(DebateParticipantAgent.this.getAID().getName())){
                        		DebateParticipantAgent.this.participantAgents[participant_index] = result[i].getName();
                        		participant_index++;
                        	}
                        }
                        
                        int target_index = new Random().nextInt(DebateParticipantAgent.this.participantAgents.length);
        				AID target_agent = DebateParticipantAgent.this.participantAgents[target_index];
                        
        				int opinion_index = new Random().nextInt(opinions.length);
        				int question_index = new Random().nextInt(questions.length);
        				String msg_content = String.format(opinions[opinion_index], currentArgument) + " " + questions[question_index];
        				
        				ACLMessage question = new ACLMessage(ACLMessage.REQUEST);
        				question.addReceiver(target_agent);
        				question.setLanguage("English");
        				question.setOntology("Debate-question");
        				question.setContent(msg_content);
        				send(question);
        				
        				DebateParticipantAgent.this.log("[QUESTION TO " + target_agent.getLocalName() + "] " + msg_content);
                        
                    } else {
                    	DebateParticipantAgent.this.log("Great, I'm alone, what a beautiful debate...");
                    }
                } catch (final FIPAException fe) {
                    fe.printStackTrace();
                }
                
			}
			
			int wait_millis = (new Random().nextInt(3) + 1) * 1000;
			this.reset((long) wait_millis);
			
		}
		
	}
	
	private static final long serialVersionUID = 1L;

	private final String zkHosts = "localhost:2181";
	private final int zkSessionTimeoutMs = 15 * 1000;
    private final int zkConnectionTimeoutMs = 10 * 1000;
    private final ZkClient zkClient = new ZkClient(zkHosts, zkSessionTimeoutMs, zkConnectionTimeoutMs, ZKStringSerializer$.MODULE$);
    private final ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zkHosts), false);
    
    private String topicName = "debateTopic";
	private String currentArgument = "";
    
	 /*
     * The list of discovered participant agents.
     */
	private AID[] participantAgents;
	
	public DebateParticipantAgent(){
		
	}
	
	private void log(final String msg) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println(timeStamp + " [" + this.getAID().getLocalName() + "]: " + msg);
    }
	
	@Override
    protected void setup() {
        this.log("Participant Started.");
        
        /*
         * 1- Create the agent description.
         */
        final DFAgentDescription dfd = new DFAgentDescription();
        /*
         * 2- Fill its mandatory fields.
         */
        dfd.setName(this.getAID());
        /*
         * 3- Create the service description.
         */
        final ServiceDescription sd = new ServiceDescription();
        /*
         * 4- Fill its mandatory fields.
         */
        sd.setType("debate-participation");
        sd.setName("K4F-debate-participation");
        /*
         * 5- Add the service description to the agent description.
         */
        dfd.addServices(sd);
        try {
            /*
             * 6- Register the service (through the agent description multiple
             * services can be registered in one shot).
             */
//            this.log("Registering '" + sd.getType() + "' service named '"
//                    + sd.getName() + "'" + "to the default DF...");
            DFService.register(this, dfd);
        } catch (final FIPAException fe) {
            fe.printStackTrace();
        }
        
        this.addBehaviour(new PayAttentionToModeratorBehaviour());
        this.addBehaviour(new DebateBehaviour(this, 2000));
        
    }
	
	@Override
    protected void takeDown() {
		try {
//            this.log("De-registering myself from the default DF...");
            DFService.deregister(this);
        } catch (final FIPAException fe) {
            fe.printStackTrace();
        }
		this.log("Bye, I'm leaving");
    }
	
}
