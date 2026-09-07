package com.discover.app.billing.repository;

import com.discover.app.billing.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface FeeComponentRepository extends JpaRepository<FeeComponent, Long> {
	Optional<FeeComponent> findByNameIgnoreCase(String name);

	List<FeeComponent> findAllByOrderByUpdatedAtDescIdDesc();

	List<FeeComponent> findByStatusOrderByNameAsc(FeeStatus status);
}
