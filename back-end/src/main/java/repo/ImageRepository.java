package repo;

import org.springframework.data.jpa.repository.JpaRepository;

import entity.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByCustomerId(Long customerId);
}

