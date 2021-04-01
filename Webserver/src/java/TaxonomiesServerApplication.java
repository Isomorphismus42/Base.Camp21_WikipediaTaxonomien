package basecamp.taxonomien.springwebserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class TaxonomiesServerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(TaxonomiesServerApplication.class, args);
	}
}
