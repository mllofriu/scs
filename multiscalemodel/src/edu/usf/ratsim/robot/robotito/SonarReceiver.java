package edu.usf.ratsim.robot.robotito;

import java.util.LinkedList;
import java.util.List;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse16;

public class SonarReceiver extends Thread {

	private static final long PERIOD = 10;
	public static final float MAX_READ = 0.4f;
	public static final float MIN_READ = 0.05f;
	public float[] sonarReading;
	public float[] sonarAngles;
	public float[] rawReadings;
	
	private List<Float> intervals;
 	private List<Float> ms;
	private List<Float> ns;
	private XBee xbee;

	public SonarReceiver(XBee xbee, int numSonars) {
		this.xbee = xbee;
		sonarReading = new float[numSonars];
		rawReadings = new float[numSonars];
		sonarAngles = new float[numSonars];
		
		intervals = new LinkedList<Float>();
		ms = new LinkedList<Float>();
		ns = new LinkedList<Float>();
		intervals.add(3.305664f);
		intervals.add(1.8212891f);
		intervals.add(1.2304688f);
		intervals.add(0.96191406f);
		intervals.add(0.7861328f);
		intervals.add(0.6298828f);
		intervals.add(0.56640625f);
		intervals.add(0.390625f);
		ms.add(16.766432f);
		ms.add(18.986177f);
		ms.add(16.912306f);
		ms.add(18.248777f);
		ms.add(24.164885f);
		ms.add(13.678905f);
		ms.add(50.332375f);
		ns.add(0.21222651f);
		ns.add(-8.00848E-4f);
		ns.add(0.13369143f);
		ns.add(0.06824231f);
		ns.add(-0.16449207f);
		ns.add(0.1802147f);
		ns.add(-0.8546095f);
		
		for (int i = 0; i < numSonars; i++) {
			sonarAngles[i] = (float) (2 * Math.PI / numSonars * i);
			System.out.print(sonarAngles[i] + ",");
			sonarReading[i] = .3f;
		}
		System.out.println();
	}

	public void run() {
		while (true) {
			
			try {
				XBeeResponse rsp = xbee.getResponse();
				if (rsp instanceof RxResponse16){
					RxResponse16 msg = (RxResponse16) rsp;
					dataReceived(msg.getData());
				}
//				System.out.println(msg);
				try {
					Thread.sleep(PERIOD);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

	public void dataReceived(int[] data) {
		for (int i = 0; i < data.length; i += 2) {
			int hi = data[i];
			int lo = data[i + 1];
			int val = (hi & 0xff) << 8 | (lo & 0xff);
			float volt = (val / 1024.0f) * 5;
			rawReadings[i/2] = volt;
			sonarReading[i / 2] = convert(volt);
//			System.out.print(sonarReading[i / 2] + "\t");
		}
//		System.out.println();
	}

	private float convert(float volt) {
		if (intervals.isEmpty())
			return MAX_READ;
		
		if (volt > intervals.get(0))
			return MIN_READ;
		
		if (volt < intervals.get(intervals.size()-1))
			return MAX_READ;

		int i = 1;
		while (i < intervals.size() && volt < intervals.get(i))
			i++;
		
		float m = ms.get(i-1);
		float n = ns.get(i-1);
		
		float dist = (float) (m / (volt - n) - 0.42f); 
		
		// Return in m
		return dist / 100f;
		
//		
//		if (volt < .3)
//			return .3f; // FLT_MAX;
//		else if (volt < 1.8f) {
//			// Inverse of distance using eq
//			float distinv = 0.0758f * volt - 0.00265f;
//			float dist = 1 / distinv - 0.42f;
//			return dist / 100f;
//		} else {
//			float distinv = 0.1111f * volt - 0.07831f;
//			float dist = 1 / distinv - 0.42f;
//			return dist / 100f;
//		}
	}

	public void calibrate(List<Float> volts, List<Float> dists) {
		intervals.clear();
		ms.clear();
		ns.clear();
		
		float prevVolt = volts.get(0);
		float prevX = 1f/(dists.get(0) + 0.42f);
		intervals.add(prevVolt);
		for (int i = 1; i < volts.size(); i++){
			float dist = dists.get(i);
			float x = 1f/(dist + 0.42f);
			float volt = volts.get(i);
			
			float m = (volt - prevVolt) / (x - prevX);
			float n = volt - m * x;
			ms.add(m);
			ns.add(n);
			intervals.add(volt);
			
			prevVolt = volt;
			prevX = x;
		}		
		
		System.out.println("Calibration:");
		for (int i = 0; i < intervals.size(); i++)
			System.out.println("intervals.add(" + intervals.get(i) + "f);");
		for (int i = 0; i < ms.size(); i++)
			System.out.println("ms.add(" + ms.get(i) + "f);");
		for (int i = 0; i < ns.size(); i++)
			System.out.println("ns.add(" + ns.get(i) + "f);");
	}

}