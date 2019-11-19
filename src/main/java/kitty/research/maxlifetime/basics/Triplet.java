package kitty.research.maxlifetime.basics;

/**
 * A triplet zip 3 objects in a single one
 * 
 * @author MeryKitty
 */
public class Triplet<E1, E2, E3> {
	private final E1 first;
	private final E2 second;
	private final E3 third;
	
	public Triplet(E1 first, E2 second, E3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public E1 first() {
		return this.first;
	}
	
	public E2 second() {
		return this.second;
	}
	
	public E3 third() {
		return this.third;
	}
}
