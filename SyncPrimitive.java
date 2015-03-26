import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

public class SyncPrimitive implements Watcher {
	static ZooKeeper zk = null;
	static Integer mutex;
	String root;
	SyncPrimitive(String address) {
		if(zk == null){
			try {
				System.out.println("Starting ZK:");
				zk = new ZooKeeper(address, 3000, this);
				mutex = new Integer(-1);
				System.out.println("Finished starting ZK: " + zk);
			} catch (IOException e) {
				System.out.println(e.toString());
				zk = null;
			}
		}
	}

	synchronized public void process(WatchedEvent event) {
		synchronized (mutex) {
			mutex.notify();
		}
	}

	static public class Queue extends SyncPrimitive {
		Queue(String address, String name) {
			super(address);
			this.root = name;
			if (zk != null) {
				try {
					Stat s = zk.exists(root, false);
					if (s == null) {
						zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
				} catch (KeeperException e) {
					System.out.println("Keeper exception when instantiating queue: "+ e.toString());
				} catch (InterruptedException e) {
					System.out.println("Interrupted exception");
				}
			}
		}

		boolean isEmpty() throws KeeperException, InterruptedException{
			List<String> list = zk.getChildren(root, true);
			if (list.size()==0) return true;
			else return false;    
		}

		boolean produce(String s) throws KeeperException, InterruptedException{
			byte[] value = s.getBytes();
			zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			return true;
		}

		String consume() throws KeeperException, InterruptedException{
			String retvalue = "";
			Stat stat = null;
			while (true) {
				synchronized (mutex) {
					List<String> list = zk.getChildren(root, true);
					if (list.size() == 0) {
						System.out.println("Going to wait");
						mutex.wait();
					} else {
						Integer min = new Integer(list.get(0).substring(7));
						for(String s : list){
							Integer tempValue = new Integer(s.substring(7));
							if(tempValue < min) min = tempValue;
						}
						System.out.println("Temporary value: " + root + "/element" + min);
						retvalue = new String(zk.getData(root + "/element" + min, false, stat));
						zk.delete(root + "/element" + min, 0);
						return retvalue;
					}
				}
			}
		}
	}
}
