import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Random;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.Math;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

public class Worker implements Runnable {

	public ZooKeeper zk;
	public OutputStream output;
	public String processID;
	public String root;
	public int operation_num;
	public Clock workerClock;

	public void run() {
		try {
			File log = new File("log"+processID+".txt");
			OutputStream output = new FileOutputStream (log, true);
			for(int j=0; j<operation_num+1; j++) {
				Random rand = new Random();
				int deltaBuy = (-80) + (int) rand.nextInt(Integer.MAX_VALUE) % 160;
				int deltaSell = (-80) + (int) rand.nextInt(Integer.MAX_VALUE) % 160;
				int interval = Math.abs(rand.nextInt() % 1000 + 2000); 
				try {
					Thread.sleep ((long) interval);
				} catch (InterruptedException e) { }            
				List<String> list = zk.getChildren(root, true);
				int update_no = list.size();
				String newZnode = root+"/"+"update"+(update_no);
				String update_value = deltaSell+","+deltaBuy;
				if (j==operation_num) {
					update_value = "End of updates";
					output.write(("P"+processID+ " has finished generating all updates." + "\n").getBytes());
				}
				zk.create(newZnode, update_value.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				output.write(("create " + newZnode + " : " + update_value + "\n").getBytes());
				workerClock.increaseStamp();//Lamport requirement
			}
		}
		catch (KeeperException e) { e.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
	}
}
