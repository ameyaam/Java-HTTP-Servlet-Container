package edu.upenn.cis.cis455.webserver;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HttpServer.HttpRequestHandler;

public class WorkerThread extends Thread{
		public String currentUrl = null;
	    private BlockingQueue taskQueue = null;
	    private boolean isStopped = false;
	    private boolean isRunning = false; 
	    static Logger log = Logger.getLogger(WorkerThread.class.getName());
	    public WorkerThread(BlockingQueue queue){
	        taskQueue = queue;
	    }

	    public void run(){
	        while(!isStopped()){
	            try{
	                HttpRequestHandler runnable = taskQueue.dequeue();
	                currentUrl = runnable.currentUrl;
	                /*Set running flag to indicate the thread is about to start servicing
	                 * a new request*/
	                isRunning = true;
	                runnable.run();
	                currentUrl = null;
	                isRunning = false;
	                
	            } catch(InterruptedException e){
	            	log.debug("Shutting down thread");
	            	return;
	                //log or otherwise report exception,
	            }catch(Exception e)
	            {
	            	
	            }
	        }
	    }

	    public synchronized void doStop(){
	    	/*If the thread is waiting, simply kill it by sending interrupt,
	    	 * the interrupt is handled above
	    	 * If the isRunning flag is unset it means that the thread has not serviced the
	    	 * current url but is not waiting (eg polling the blocking queue to see if new 
	    	 * request is there to be processed. In this case also, kill the thread by interrupting
	    	 * it*/
	    	if((this.getState() == Thread.State.WAITING)||(isRunning == false))
	    	{
	    		this.interrupt();
	    	}
	    	/*If thread has isRunning flag set, then dont interrupt it, but set the isStopped
	    	 * flag to false. So the when the thread checks the while loop in the run method
	    	 * it will fail the condition and break from the loop and terminate itself*/
	    	else
	    	{
	    		isStopped = true;
	    	}
	    }

	    public synchronized boolean isStopped(){
	        return isStopped;
	    }
}
