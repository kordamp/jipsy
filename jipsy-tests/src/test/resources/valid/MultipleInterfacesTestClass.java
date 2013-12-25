import java.util.RandomAccess;
import java.io.Serializable;

import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor({RandomAccess.class, Serializable.class})
public class MultipleInterfacesTestClass implements RandomAccess, Serializable {
	private static final long serialVersionUID = 1L;
}
