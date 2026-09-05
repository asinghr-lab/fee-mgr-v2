package com.discover.app.school.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="academic_years", uniqueConstraints=@UniqueConstraint(name="uk_academic_year_name",columnNames="name"))
public class AcademicYear {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=20) private String name;
 @Column(nullable=false) private LocalDate startDate;
 @Column(nullable=false) private LocalDate endDate;
 @Column(nullable=false) private boolean active=false;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(nullable=false) private LocalDateTime updatedAt;
 protected AcademicYear() {}
 public AcademicYear(String name,LocalDate startDate,LocalDate endDate){this.name=name;this.startDate=startDate;this.endDate=endDate;}
 @PrePersist void onCreate(){var now=LocalDateTime.now();createdAt=now;updatedAt=now;}
 @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getName(){return name;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;} public boolean isActive(){return active;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
 public void activate(){active=true;} public void deactivate(){active=false;}
}
