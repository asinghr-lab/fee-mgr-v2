package com.discover.app.billing.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="fee_structures", uniqueConstraints=@UniqueConstraint(name="uk_fee_structure_name", columnNames="name"), indexes=@Index(name="idx_fee_structure_status", columnList="status"))
public class FeeStructure {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private FeeStatus status=FeeStatus.ACTIVE;
    @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime updatedAt;
    @OneToMany(mappedBy="feeStructure",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("id ASC") private List<FeeStructureItem> items=new ArrayList<>();
    protected FeeStructure() {}
    public FeeStructure(String name){this.name=name;}
    @PrePersist void onCreate(){var now=LocalDateTime.now();createdAt=now;updatedAt=now;}
    @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
    public void addItem(FeeStructureItem item){items.add(item);item.attachTo(this);}
    public void deactivate(){if(status==FeeStatus.INACTIVE) throw new IllegalStateException("Fee structure is already inactive"); status=FeeStatus.INACTIVE;}
    public Long getId(){return id;} public String getName(){return name;} public FeeStatus getStatus(){return status;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;} public List<FeeStructureItem> getItems(){return Collections.unmodifiableList(items);}
}
