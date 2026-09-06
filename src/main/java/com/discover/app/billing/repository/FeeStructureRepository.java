package com.discover.app.billing.repository;
import com.discover.app.billing.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface FeeStructureRepository extends JpaRepository<FeeStructure,Long> {
    Optional<FeeStructure> findByNameIgnoreCase(String name);
    @EntityGraph(attributePaths = {
            "items",
            "items.feeComponent"
    })
    List<FeeStructure> findAllByOrderByUpdatedAtDescIdDesc();
    @EntityGraph(attributePaths = {
            "items",
            "items.feeComponent"
    })
    List<FeeStructure> findByStatusOrderByNameAsc(FeeStatus status);
}
