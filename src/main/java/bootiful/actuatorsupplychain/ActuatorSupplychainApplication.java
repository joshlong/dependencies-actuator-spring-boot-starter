package bootiful.actuatorsupplychain;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class ActuatorSupplychainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActuatorSupplychainApplication.class, args);
	}

	@Bean
	ApplicationRunner runner (){
		return args -> {
		  var classpath = new ClassPathResource("/META-INF/classpath");
		  try (var in = classpath.getInputStream()) {
		    var bytes = in.readAllBytes();
		    System.out.println(new String(bytes));
		  }
		};
	}
}
