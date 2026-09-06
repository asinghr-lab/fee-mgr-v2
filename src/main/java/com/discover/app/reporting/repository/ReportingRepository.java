package com.discover.app.reporting.repository;

import com.discover.app.billing.domain.*;
import com.discover.app.collection.domain.Payment;
import com.discover.app.collection.domain.PaymentStatus;
import com.discover.app.school.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;

public interface ReportingRepository extends JpaRepository<Invoice, Long> {
	@Query("select distinct i from Invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch e.grade g join fetch i.academicYear y left join fetch i.items where y.id=:yearId order by g.displayOrder asc,s.admissionNumber asc,i.generationDate asc")
	List<Invoice> invoices(@Param("yearId") Long yearId);

	@Query("select distinct e from StudentEnrollment e join fetch e.student s join fetch e.grade g join fetch e.academicYear y where y.id=:yearId and e.status=:status order by g.displayOrder asc,s.admissionNumber asc")
	List<StudentEnrollment> enrollments(@Param("yearId") Long yearId, @Param("status") EnrollmentStatus status);

	@Query("select p from Payment p join fetch p.invoice i join fetch i.studentEnrollment e join fetch e.student s join fetch e.grade g where p.status=:status and i.academicYear.id=:yearId")
	List<Payment> payments(@Param("yearId") Long yearId, @Param("status") PaymentStatus status);

	@Query("select distinct r from DiscountRequest r join fetch r.invoice i join fetch i.studentEnrollment e join fetch e.student s left join fetch r.items where i.academicYear.id=:yearId")
	List<DiscountRequest> discountRequests(@Param("yearId") Long yearId);
}