package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HttpServer.HttpRequestHandler;

public class BlockingQueue {
	/*static final Logger logger = Logger.getLogger(WorkerThread.class);*/
	//static Logger log = Logger.getLogger(WorkerThread.class.getName());
	private ArrayList<HttpRequestHandler> queue = new ArrayList<HttpRequestHandler>();
  //private List queue = new LinkedList();
 // private int  limit = 10;

  public BlockingQueue(){
    //this.limit = limit;
	 // BasicConfigurator.configure();
  }


  public void enqueue(HttpRequestHandler item) throws InterruptedException  {	
	  //logger.info("[Output from log4j] Adding element to queue");
	  //System.out.println("enqueued!");
	  synchronized(queue){
		  queue.add(item);
		  queue.notify();
    }
  }


  public HttpRequestHandler dequeue() throws InterruptedException{
    while(queue.isEmpty()){
    	synchronized(queue)
    	{
    		queue.wait();
    	}
    }
    synchronized(queue){
    	return queue.remove(0);
    }
  }
}