package kitty.research.maxlifetime.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import kitty.research.maxlifetime.basics.Geometry;
import kitty.research.maxlifetime.basics.Point;
import kitty.research.maxlifetime.basics.Segment;

/**
 * The sensor network
 * 
 * @author MeryKitty
 *
 */
public class SensorNetwork {
	private final double width, height;
	private final Segment leftEdge, rightEdge;
	private final List<Sensor> sensorList;
	
	private SensorNetwork(double width, double height) {
		this.width = width;
		this.height = height;
		this.sensorList = new ArrayList<Sensor>();
		this.leftEdge = new Segment(new Point(0, 0), new Point(0, this.height));
		this.rightEdge = new Segment(new Point(this.width, 0), new Point(this.width, this.height));
	}
	
	/**
	 * The width of the sensor network
	 * 
	 * @return
	 */
	public double width() {
		return this.width;
	}
	
	/**
	 * The height of the sensor network
	 * 
	 * @return
	 */
	public double height() {
		return this.height;
	}
	
	/**
	 * The segment represents the left edge of the sensing field
	 * 
	 * @return
	 */
	public Segment leftEdge() {
		return this.leftEdge;
	}
	
	/**
	 * The segment represents the right edge of the sensing field
	 * 
	 * @return
	 */
	public Segment rightEdge() {
		return this.rightEdge;
	}
	
	/**
	 * The list of sensor in the sensor network
	 * 
	 * @return
	 */
	public List<Sensor> sensorList() {
		return this.sensorList;
	}
	
	/**
	 * Read the data and return the sensor network according to the read data
	 * 
	 * @param file the file to read the input data
	 * @return
	 * @throws IOException
	 */
	public static SensorNetwork readData(String file) throws IOException {
		var input = Files.readAllLines(Paths.get(file));
		var temp = input.get(0).split(" ");
		var result = new SensorNetwork(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
		for (int i = 0; i < input.size() / 2; i++) {
			temp = input.get(i * 2 + 1).split(" ");
			double x = Double.parseDouble(temp[0]);
			double y = Double.parseDouble(temp[1]);
			double r = Double.parseDouble(temp[2]);
			double alpha = Math.toRadians(Double.parseDouble(temp[3]));
			double lt = Double.parseDouble(temp[4]);
			Sensor cur = new Sensor(x, y, r, alpha, lt);
			result.sensorList.add(cur);
			temp = input.get(i * 2 + 2).split(" ");
			for (String phiString : temp) {
				double phi = Math.toRadians(Double.parseDouble(phiString));
				cur.sectorList().add(new Sector(cur, phi));
			}
		}
		return result;
	}
	
	/**
	 * Check the possible intersections of sensors in the network
	 * 
	 * @param diff
	 */
	public void initialiseSectors(double diff) {
		for (int i = 0; i < this.sensorList().size(); i++) {
			Sensor s1 = this.sensorList().get(i);
			for (int j = 0; j < i; j++) {
				Sensor s2 = this.sensorList().get(j);
				if (s1.checkIntersection(s2)) {
					s1.intersectedSensor().add(s2);
					s2.intersectedSensor().add(s1);
				}
			}
		}
//		for (Sensor s : this.sensorList()) {
//			var temp = s.sectorList();
//			for (double i = 0; i < Math.PI * 2 - 0.0001; i += diff) {
//				temp.add(new Sector(s, i));
//			}
//		}
	}
	
	/**
	 * Update the intersections of the sectors in the sensor network
	 */
	public void updateSectors() {
		for (Sensor S : this.sensorList()) {
			for (Sector sec : S.sectorList()) {
				for (Sensor S2 : S.intersectedSensor()) {
					for (Sector sec2 : S2.sectorList()) {
						if (Geometry.circularSectorIntersection(sec, sec2)) {
							sec.intersectedSectors().add(sec2);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Forget it!
	 */
	public void reduceSectors() {
		for (Sensor S : this.sensorList()) {
			if (S.sectorList().size() == 0) {
				continue;
			}
			var iter = S.sectorList().iterator();
			var current = iter.next();
			boolean passAll = false;
			while(true) {
				var temp = iter.next();
				if (current.compareIntersect(temp)) {
					iter.remove();
				} else {
					if (passAll) {
						break;
					}
					current = temp;
				}
				if (!iter.hasNext()) {
					iter = S.sectorList().iterator();
					passAll = true;
				}
			}
		}
		for (Sensor S : this.sensorList()) {
			for (Sector sec : S.sectorList()) {
				for (var iter = sec.intersectedSectors().iterator(); iter.hasNext();) {
					var sec2 = iter.next();
					if (!sec2.root().sectorList().contains(sec2)) {
						iter.remove();
					}
				}
			}
		}
	}
	
	/**
	 * Count the number of sectors in the network
	 * 
	 * @return
	 */
	public int countSectors() {
		int result = 0;
		for (Sensor S : this.sensorList()) {
			result += S.sectorList().size();
		}
		return result;
	}
	
	/**
	 * Count the number of intersections in the sensor network
	 * 
	 * @return
	 */
	public int countOverlaps() {
		int result = 0;
		for (Sensor S : this.sensorList()) {
			for (Sector sec : S.sectorList()) {
				result += sec.intersectedSectors().size();
			}
		}
		return result;
	}
	
	/**
	 * Iterate through all the sectors in the network
	 * 
	 * @param action
	 */
	public void iterateSectors(Consumer<? super Sector> action) {
		for (Sensor S : this.sensorList()) {
			for (Sector sec : S.sectorList()) {
				action.accept(sec);
			}
		}
	}
	
	/**
	 * Get the list of sectors overlap the left edge of the sensing field
	 * 
	 * @return
	 */
	public List<Sector> sectorsOverlapLeftEdge() {
		var result = new ArrayList<Sector>();
		this.iterateSectors((Sector s) -> {
			if (Geometry.segmentCircularSectorIntersection(this.leftEdge(), s)) {
				result.add(s);
			}
		});
		return result;
	}
	
	/**
	 * Get the list of sectors overlap the right edge of the sensing field
	 * 
	 * @return
	 */
	public List<Sector> sectorsOverlapRightEdge() {
		var result = new ArrayList<Sector>();
		this.iterateSectors((Sector s) -> {
			if (Geometry.segmentCircularSectorIntersection(this.rightEdge(), s)) {
				result.add(s);
			}
		});
		return result;
	}
}
