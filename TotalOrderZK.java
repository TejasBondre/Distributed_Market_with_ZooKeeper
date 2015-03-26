import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.*;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs.Ids;

public class TotalOrderZK {
	private static final int SESSION_TIMEOUT = 10000;
	public static ZooKeeper zk;
	public Worker w;
	public Dispatcher dp;
	public Watcher wh = new Watcher() { public void process (WatchedEvent event) { } };

	public static void main (String args[]) throws Exception {
		String processID = args[0];
		int operation_num = Integer.parseInt(args[1]);
		int clockRate = Integer.parseInt(args[2]);
		String address = "ece-acis-dc091.acis.ufl.edu:2181";
		String root = "/hw6_ROOT";   

		Currency currency = new Currency();
		currency.setBuyRate(100);
		currency.setSellRate(100);
		Clock clock = new Clock();
		clock.setClockRate(clockRate);

		LocalClock lc = new LocalClock();
		lc.localClock = clock;
		Thread lcthread = new Thread(lc);
		lcthread.start();

		File log = new File("log"+processID+".txt");
		OutputStream output = new FileOutputStream (log, false);

		TotalOrderZK to = new TotalOrderZK();
		to.createZKInstance(address);
		String connected = "P"+processID+" is connected to ZooKeeper at " + address;
		output.write(connected.getBytes());
		System.out.println(connected);

		if(processID.equals("0")) {
			Thread.sleep(2000);
			try { 
				List<String> children = zk.getChildren(root, true);
				for (String c: children) {
					zk.delete(root+"/" + c, 0);
				}
				zk.delete(root, -1); 
			} catch (Exception e) { }
			try { 
				if(to.zk.exists(root,false)==null) {
					to.zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
					System.out.println("create "+root+" zk node");
				}
			}
			catch(KeeperException e) { e.printStackTrace(); }
			catch(InterruptedException e) { e.printStackTrace(); }
		}
		else { Thread.sleep(5000); }

		Worker worker = new Worker();

		worker.zk = to.zk;
		worker.root = root;
		worker.operation_num = operation_num;
		worker.workerClock = clock;
		worker.processID = processID;

		Dispatcher dis = new Dispatcher();
		dis.processID = processID;
		dis.currency = currency;
		dis.dis_clock = clock;
		dis.zk = to.zk;
		dis.root = root;

		if(processID.equals("0")) {
			Thread.sleep(5000);
		}

		Thread worker_thread = new Thread(worker);
		worker_thread.start();
		System.out.println("worker start");
		Thread dp_thread = new Thread(dis);
		dp_thread.start();
		System.out.println("dispatcher start");

		worker_thread.join();
		dp_thread.join();

	}

	private void createZKInstance(String address) {
	try {
			zk = new ZooKeeper(address, this.SESSION_TIMEOUT, this.wh);
		} catch (IOException e) {
			System.out.println("Cannot create ZooKeeper Instance");
			System.out.println("Got an exception:" + e.getMessage());
		}
	}

	private void closeZKInstance() {
	try {
			zk.close();
		} catch (InterruptedException e) {
			System.out.println("Cannot close ZooKeeper Instance");
			System.out.println("Got an exception:" + e.getMessage());
		}
	}

	public static String realTime(){
		Calendar c = Calendar.getInstance();
		String realTime= "["+c.get(Calendar.MONTH)+"/"+c.get(Calendar.DATE)+" "
							+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+"]";
		return realTime;
	}
}
