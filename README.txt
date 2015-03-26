HOW TO RUN:

java -cp .:zookeeper-3.4.6/zookeeper-3.4.6.jar:zookeeper-3.4.6/lib/* TotalOrderZK <PROCESS_ID> <NUM_OPERATIONS> <CLOCK_SPEED>




IMPORTANT CLASSES:

1. TotalOrderZK = this class has the main() method that starts the dispatcher and worker threads, keeps track of the message queue using SyncPrimitive and maintains (cleaning up old nodes, etc) the zookeeper instance.

2. Currency = this class maintains the Currency values, updates them, and the file also hosts the logical clocks.

3. Dispatcher = this class dequeues the updates from zookeeper and applies them to Currency. Keeps track of number of updates.

4. Worker = this class generates new updates to currency and enqueues them into zookeeper.

5. SyncPrimitive = this is the standard class from Apache website that is used to maintain queue's producer and consumer functionality.
