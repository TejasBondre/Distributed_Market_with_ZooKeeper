public class Currency {
	private int buyRate;
	private int sellRate;
	public void setBuyRate(int i) {
		this.buyRate = i + buyRate;
	}
	public void setSellRate(int i) {
		this.sellRate = i + sellRate;
	}
	public int getBuyRate() {
		return this.buyRate;
	}
	public int getSellRate() {
		return this.sellRate;
	}
}

class Clock {
	public int clockRate;
	public int counter;
	public void setClockRate(int i) {
		this.clockRate = i;
	}
	public synchronized void setCounter (int i) {
		this.counter = i;
	}
	public synchronized void increaseStamp() {
		this.counter++;
	}
}


class LocalClock implements Runnable {
	public Clock localClock;
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				int currentCounter = localClock.counter;
				currentCounter += localClock.clockRate;
				localClock.setCounter(currentCounter);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
