package com.discover.app.billing.repository;

import com.discover.app.billing.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface CancellationRequestRepository extends JpaRepository<InvoiceCancellationRequest, Long> {
	boolean existsByInvoiceIdAndStatus(Long invoiceId, CancellationRequestStatus status);

	@Query("select r from InvoiceCancellationRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch r.requestedBy left join fetch r.decidedBy where r.status=:status order by r.createdAt asc, r.id asc")
	List<InvoiceCancellationRequest> findDetailedByStatus(@Param("status") CancellationRequestStatus status);

	@Query("select r from InvoiceCancellationRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch r.requestedBy left join fetch r.decidedBy order by r.createdAt desc, r.id desc")
	List<InvoiceCancellationRequest> findAllDetailed();

	@Query("select r from InvoiceCancellationRequest r join fetch r.invoice i join fetch r.requestedBy left join fetch r.decidedBy where r.id=:id")
	Optional<InvoiceCancellationRequest> findDetailedById(@Param("id") Long id);

	@Query("select r from InvoiceCancellationRequest r join fetch r.requestedBy left join fetch r.decidedBy where r.invoice.id=:invoiceId order by r.createdAt desc, r.id desc")
	List<InvoiceCancellationRequest> findLatestByInvoiceId(@Param("invoiceId") Long invoiceId);
}
