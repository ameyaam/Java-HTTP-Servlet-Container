package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis.cis455.webserver.HttpServer.HttpRequestHandler;

public class ThreadPool {
    private BlockingQueue queue = null;
    public List<WorkerThread> threads = new ArrayList<WorkerThread>();
    private boolean isStopped = false;

    public ThreadPool(int noOfThreads, int maxNoOfTasks){
        queue = new BlockingQueue();

        for(int i=0; i<noOfThreads; i++){
            threads.add(new WorkerThread(queue));
        }
        for(WorkerThread thread : threads){
            thread.start();
        }
    }

    public void execute(HttpRequestHandler task) throws Exception{
        if(this.isStopped) throw
            new IllegalStateException("ThreadPool is stopped");

        this.queue.enqueue(task);
    }

    public synchronized void stop(){
        this.isStopped = true;
        for(WorkerThread thread : threads){
           thread.doStop();
           try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupt while joining threads");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }
}
