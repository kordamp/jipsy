import java.util.RandomAccess;

import org.kordamp.jipsy.ServiceProviderFor;

public class StaticInnerTestClass {
	@ServiceProviderFor(RandomAccess.class)
	public static class Inner implements RandomAccess {
		
	}
}
