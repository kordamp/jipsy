import java.util.RandomAccess;

import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(RandomAccess.class)
public class NoPublicConstructorTestClass implements RandomAccess {
	private NoPublicConstructorTestClass() {
	}
}
