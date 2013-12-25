import java.util.RandomAccess;

import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(RandomAccess.class)
public class NoNoArgsConstructorTestClass implements RandomAccess {
	public NoNoArgsConstructorTestClass(String name) {
	}
}
