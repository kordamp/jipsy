import java.util.RandomAccess;

import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(RandomAccess.class)
class PackagePrivateTestClass {
	String value();
}
