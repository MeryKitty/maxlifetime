package kitty.research.maxlifetime.model;

import java.util.ArrayList;
import java.util.List;
import kitty.research.maxlifetime.basics.Point;

/**
 * A sensor in the sensor network
 * 
 * @author MeryKitty
 *
 */
public class Sensor extends Point {
	private final double r;
	private final double alpha;
	private final double lifetime;
	private final List<Sensor> intersectedSensors;
	private List<Sector> sectorList;
	
	public Sensor(double x, double y, double r, double alpha, double lifetime) {
		super(x, y);
		this.r = r;
		this.alpha = alpha;
		this.lifetime = lifetime;
		this.intersectedSensors = new ArrayList<>();
		this.sectorList = new ArrayList<>();
	}
	
	public final double r() {
		return this.r;
	}
	
	public final double alpha() {
		return this.alpha;
	}
	
	public final double lifetime() {
		return this.lifetime;
	}
	
	public final List<Sensor> intersectedSensor() {
		return this.intersectedSensors;
	}
	
	public final List<Sector> sectorList() {
		return this.sectorList;
	}
	
	public boolean checkIntersection(Sensor other) {
		return this.distance(other) < this.r() + other.r();
	}
}
