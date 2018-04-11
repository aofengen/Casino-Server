package blackjack;

//import java.util.Arrays;

//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);
		System.out.println("Server started.");
    }
    
//	@Bean
    public class corsConfigurer implements WebMvcConfigurer {
//        return new corsConfigurer() {
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/shuffle").allowedOrigins("*");
//            }
//        };
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//
//        };
//    }

}