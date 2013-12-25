import java.util.RandomAccess;

import org.kordamp.jipsy.ServiceProviderFor;

public class NonStaticTestClass {

	@ServiceProviderFor(RandomAccess.class)
	public class InnerNonStaticTestClass {
		String value();
	}
}
