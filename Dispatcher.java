import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.util.List;
import java.lang.InterruptedException;
import java.lang.Integer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

public class Dispatcher implements Runnable {
	public ZooKeeper zk;
	public String processID;
	public Currency currency;
	public Clock dis_clock;
	public String root;
	public void run() {
		try {
			File log = new File("log"+processID+".txt");
			OutputStream output = new FileOutputStream (log, true);
			int end_counter = 0;
			int update_counter = 0;
			while (true) {
				if(zk.exists(root+"/"+"update"+update_counter, false)!=null) {
					String update_value = new String(zk.getData(root+"/"+"update"+update_counter, false, null));
					update_counter++;
					if(update_value.equals("End of updates")) {
						end_counter++;
					} else {
						String[] deltas = update_value.split(",");
						currency.setSellRate(Integer.parseInt(deltas[0]));
						currency.setBuyRate(Integer.parseInt(deltas[1]));
						try {
						output.write(("currency is set to sell_rate="+currency.getSellRate()+" and buy_rate="+currency.getBuyRate() +" after updates deltaSell="+deltas[0]+" and deltaBuy="+deltas[1]+"\n").getBytes());
						} catch (Exception e) {}
					}
					if(end_counter == 3) {
						System.out.println("exiting.");
						System.exit(0);
					}
				}
			}
		}
		catch (KeeperException e) { e.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
	}
}
