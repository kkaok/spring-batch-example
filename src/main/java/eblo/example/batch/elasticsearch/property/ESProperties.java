package eblo.example.batch.elasticsearch.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@PropertySource({ "classpath:es-default.properties" })
@ConfigurationProperties(prefix = "es.connection")
@Setter
@Getter
@ToString
public class ESProperties {

  private String host;
  private int port;
  private String scheme;

}
