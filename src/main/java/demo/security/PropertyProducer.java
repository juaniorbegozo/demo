package demo.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

@ApplicationScoped
@Named("propertyProducer")
public class PropertyProducer {
	private Properties properties;

	@Property
	@Produces
	public String produceString(final InjectionPoint ip) {
		return this.properties.getProperty(getKey(ip));
	}

	@Property
	@Produces
	public int produceInt(final InjectionPoint ip) {
		return Integer.valueOf(this.properties.getProperty(getKey(ip)));
	}

	@Property
	@Produces
	public boolean produceBoolean(final InjectionPoint ip) {
		return Boolean.valueOf(this.properties.getProperty(getKey(ip)));
	}

	private synchronized String getKey(final InjectionPoint ip) {
		return (ip.getAnnotated().isAnnotationPresent(Property.class)
				&& !ip.getAnnotated().getAnnotation(Property.class).value().isEmpty())
						? ip.getAnnotated().getAnnotation(Property.class).value() : ip.getMember().getName();
	}

	@PostConstruct
	public void init() throws IOException {
		synchronized (this.getClass()) {
			if(this.properties == null){
				this.properties = new Properties();
				final InputStream stream = PropertyProducer.class.getResourceAsStream("/application.properties");
				if (stream == null) {
					throw new RuntimeException("No properties!!!");
				}
				try {
					this.properties.load(stream);
					stream.close();
				} catch (final IOException e) {
					stream.close();
					throw new RuntimeException("Configuration could not be loaded!");
				}
			}
		}		
	}
}
