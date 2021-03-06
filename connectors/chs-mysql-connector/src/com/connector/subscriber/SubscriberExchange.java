package com.connector.subscriber;
import java.io.IOException;

import javax.swing.JTextArea;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class SubscriberExchange implements Runnable{

	private Thread t;
    private final String EXCHANGE_NAME = "chs";
    private JTextArea jta;
    private String[] subscribe;

    public SubscriberExchange(String[] subscribe, JTextArea jtaSubscription) {
    	this.subscribe = subscribe;
    	this.jta = jtaSubscription;
	}

	public void run() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.48.21");
        factory.setPort(5672);
        factory.setUsername("sahyog");factory.setPassword("sahyog");
        
        Connection connection;
        QueueingConsumer consumer = null;;
		try {
			connection = factory.newConnection();
		    Channel channel = connection.createChannel();
	
	        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
	        // Queue made durable so that it receives message even during mode is not running
	        boolean durable = true;
	        String queueName = channel.queueDeclare("aiims", durable, false, false, null).getQueue();
	
	        for(String severity : subscribe){
	            channel.queueBind(queueName, EXCHANGE_NAME, severity);
	        }
	
	        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	
	        consumer = new QueueingConsumer(channel);
	        channel.basicConsume(queueName, true, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
        while (true) {
            QueueingConsumer.Delivery delivery;
			try {
				delivery = consumer.nextDelivery();
				
	            String message = new String(delivery.getBody());
	            String routingKey = delivery.getEnvelope().getRoutingKey();
	
	            System.out.println("[x] Received '" + routingKey + "':'" + message + "'");
	            this.jta.append("[x] Received " + routingKey + "':'" + message + "'\n");
            
			} catch (ShutdownSignalException | ConsumerCancelledException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    public void start ()
    {
       System.out.println("Subscriber For ICMR Started");
       if (t == null)
       {
          t = new Thread (this);
          t.start ();
       }
    }

	
}