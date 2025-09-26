package controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Image;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import repo.CustomerRepository;
import repo.ImageRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RestController
@RequestMapping("/api/images")
public class ImageController {

	@Autowired
	private ImageRepository imageRepo;

	@Autowired
	private CustomerRepository customerRepo;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.region}")
	private String region;

	@Value("${jwt.secret}")
	private String jwtSecret;

	private final S3Client s3 = S3Client.create(); // uses instance role credentials

	private Long customerIdFromToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new RuntimeException("Unauthorized");
		}
		String token = authHeader.substring(7);
		Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build()
				.parseClaimsJws(token).getBody();
		return Long.valueOf(claims.getSubject());
	}

	@GetMapping
	public List<Map<String, String>> list(@RequestHeader("Authorization") String auth) {
		Long cid = customerIdFromToken(auth);
		List<Image> imgs = imageRepo.findByCustomerId(cid);

		S3Presigner presigner = S3Presigner.create();
		return imgs.stream().map(img -> {
			GetObjectRequest gor = GetObjectRequest.builder().bucket(bucket).key(img.getS3Key()).build();
			GetObjectPresignRequest pop = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(15))
					.getObjectRequest(gor).build();
			String url = presigner.presignGetObject(pop).url().toString();
			return Map.of("filename", img.getFilename(), "url", url);
		}).collect(Collectors.toList());
	}

	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestHeader("Authorization") String auth,
			@RequestParam("file") MultipartFile file) throws IOException {
		Long cid = customerIdFromToken(auth);

		if (!"image/png".equals(file.getContentType())) {
			return ResponseEntity.status(400).body(Map.of("message", "only PNG allowed"));
		}

		String key = "customer-" + cid + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
		PutObjectRequest por = PutObjectRequest.builder().bucket(bucket).key(key).contentType(file.getContentType())
				.build();
		s3.putObject(por, RequestBody.fromBytes(file.getBytes()));

		Image img = new Image();
		img.setCustomerId(cid);
		img.setFilename(file.getOriginalFilename());
		img.setS3Key(key);
		img.setUploadedAt(Instant.now());
		imageRepo.save(img);

		return ResponseEntity.ok(Map.of("message", "uploaded"));
	}
}
