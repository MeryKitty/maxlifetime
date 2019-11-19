package kitty.research.maxlifetime.basics;

/**
 * A pair match zip 2 object in a single one
 * 
 * @author MeryKitty
 *
 * @param <E1>
 * @param <E2>
 */
public class Pair<E1, E2> {
	private final E1 first;
	private final E2 second;
	
	public Pair(E1 first, E2 second) {
		this.first = first;
		this.second = second;
	}
	
	public E1 first() {
		return this.first;
	}
	
	public E2 second() {
		return this.second;
	}
	
	@Override
	public String toString() {
		return "<" + this.first.toString() + ", " + this.second.toString() + ">";
	}
	
	@Override
	public int hashCode() {
		return 10000 * this.first.hashCode() + this.second.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			@SuppressWarnings("rawtypes")
			var p = (Pair)o;
			return this.first.equals(p.first) && this.second.equals(p.second);
		} else {
			return false;
		}
	}
}