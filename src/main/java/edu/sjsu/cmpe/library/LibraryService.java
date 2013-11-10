package edu.sjsu.cmpe.library;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Connection connection;
    private static MessageProducer producer;
    private static String instanceName = "";
   
    private static String user = "";
    private static String password = "";
    private static String host = "";
    private static int port = 0;
    
    
    public static void main(String[] args) throws Exception {
	 
     new LibraryService().run(args);	
		 
    }
    
    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");	
	bootstrap.addBundle(new ViewBundle());
	bootstrap.addBundle(new AssetsBundle());
    }
    
    public static void OrderForNewBook(Long lostisbn)throws JMSException{
    	
    	String queue = "/queue/05829.book.orders";
    	System.out.println("**********************************************************************");   	
	 	System.out.println("Library: "+ instanceName +" is ordering for following lost books through: " + queue); 
	 	
    	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
    	factory.setBrokerURI("tcp://" + host + ":" + port);
    	connection = factory.createConnection(user, password);
    	connection.start();
    	
    	Destination dest = new StompJmsDestination(queue);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
       
        producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        
        String data = instanceName+":"+lostisbn.toString();
        TextMessage msg = session.createTextMessage(data);
        System.out.println("Ordering book: " + data);
        System.out.println("**********************************************************************");   	        
        producer.send(msg);
        
        connection.close();
    }
    
    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
    
	           
				String queueName = configuration.getStompQueueName();
				String topicName = configuration.getStompTopicName();
				instanceName = configuration.getDefaultName();
				user = configuration.getApolloUser();
				password = configuration.getApolloPassword();
				host= configuration.getApolloHost();
				port= configuration.getApolloPort();
				
			   log.debug("Queue name is {}. Topic name is {}", queueName,
		          topicName);
    
                /** Root API */
				environment.addResource(RootResource.class);
				
				/** Books APIs */
				BookRepositoryInterface bookRepository = new BookRepository();
				environment.addResource(new BookResource(bookRepository));
			
				/** UI Resources */
				environment.addResource(new HomeResource(bookRepository,instanceName));
    }
    
   }
