package controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import entity.Customer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import repo.CustomerRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private CustomerRepository customerRepo;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
		String email = body.get("email"), password = body.get("password");
		if (customerRepo.findByEmail(email).isPresent()) {
			return ResponseEntity.status(400).body(Map.of("message", "email already registered"));
		}
		String hash = new BCryptPasswordEncoder().encode(password);
		Customer c = new Customer();
		c.setEmail(email);
		c.setPassword(hash);
		customerRepo.save(c);
		return ResponseEntity.ok(Map.of("message", "created"));
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody Map<String, String> body) {
		String email = body.get("email"), password = body.get("password");
		Optional<Customer> oc = customerRepo.findByEmail(email);
		if (oc.isEmpty()) {
			return ResponseEntity.status(400).body(Map.of("message", "invalid credentials"));
		}
		Customer c = oc.get();
		if (!new BCryptPasswordEncoder().matches(password, c.getPassword())) {
			return ResponseEntity.status(400).body(Map.of("message", "invalid credentials"));
		}
		String token = Jwts.builder().setSubject(c.getId().toString()).claim("email", c.getEmail())
				.setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
				.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).compact();
		return ResponseEntity.ok(Map.of("token", token));
	}
}
