package kitty.research.maxlifetime.model;

import java.util.HashSet;
import java.util.Set;

import kitty.research.maxlifetime.basics.CircularSector;
import kitty.research.maxlifetime.basics.Geometry;

/**
 * A sector represents a cover possibility of a sensor, the cover region
 * is a circular sector, so this class extends Circular sector for convenience
 * 
 * @author MeryKitty
 *
 */
public class Sector extends CircularSector{
	private final Sensor root;
	private final double position;
	private final Set<Sector> intersectedSectors;
	
	public Sector(Sensor root, double position) {
		super(root, root.r(), Geometry.standardiseAngle(position - root.alpha() / 2), Geometry.standardiseAngle(position + root.alpha() / 2));
		this.root = root;
		this.position = position;
		this.intersectedSectors = new HashSet<>();
	}
	
	public final Sensor root() {
		return this.root;
	}

	public final double position() {
		return this.position;
	}
	
	public final Set<Sector> intersectedSectors() {
		return this.intersectedSectors;
	}
	
	/**
	 * Check if 2 sectors have the same set of intersected sectors
	 * 
	 * @param other
	 * @return
	 */
	public boolean compareIntersect(Sector other) {
		if (this.intersectedSectors().size() != other.intersectedSectors().size()) {
			return false;
		}
		for (Sector s : this.intersectedSectors()) {
			if (!other.intersectedSectors().contains(s)) {
				return false;
			}
		}
		return true;
	}
}
