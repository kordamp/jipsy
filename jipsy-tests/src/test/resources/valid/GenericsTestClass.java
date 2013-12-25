import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(Comparable.class)
public class GenericsTestClass implements Comparable<Integer> {

	@Override
	public int compareTo(Integer o) {
		return 0;
	}
}
