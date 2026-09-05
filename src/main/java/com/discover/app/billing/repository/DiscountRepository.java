package com.discover.app.billing.repository;
import com.discover.app.billing.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DiscountRepository extends JpaRepository<Discount,Long> {}
