package com.windsome.repository.product;

import com.windsome.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Long> {
    boolean existsByName(String sizeName);
}
