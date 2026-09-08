package com.discover.app.billing.repository;

import com.discover.app.billing.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DiscountRequestRepository extends JpaRepository<DiscountRequest, Long> {
	boolean existsByInvoiceId(Long invoiceId);

	boolean existsByInvoiceIdAndStatus(Long invoiceId, DiscountRequestStatus status);

	@Query("select distinct r from DiscountRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch r.requestedBy left join fetch r.decidedBy left join fetch r.items where r.status=:status order by r.createdAt asc, r.id asc")
	List<DiscountRequest> findDetailedByStatus(@Param("status") DiscountRequestStatus status);

	@Query("select distinct r from DiscountRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch r.requestedBy left join fetch r.decidedBy left join fetch r.items order by r.createdAt desc, r.id desc")
	List<DiscountRequest> findAllDetailed();

	@Query("select distinct r from DiscountRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch r.requestedBy left join fetch r.decidedBy left join fetch r.items where r.id=:id")
	Optional<DiscountRequest> findDetailedById(@Param("id") Long id);

	@Query("select distinct r from DiscountRequest r join fetch r.requestedBy left join fetch r.decidedBy left join fetch r.items where r.invoice.id=:invoiceId")
	Optional<DiscountRequest> findLatestByInvoiceId(@Param("invoiceId") Long invoiceId);
}
