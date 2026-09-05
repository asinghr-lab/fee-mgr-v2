package com.discover.app.billing.domain;
import com.discover.app.identity.domain.User; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
@Entity @Table(name="discount_requests",indexes={@Index(name="idx_discount_request_invoice_status",columnList="invoice_id,status"),@Index(name="idx_discount_request_created",columnList="created_at")})
public class DiscountRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id",nullable=false) private Invoice invoice;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private DiscountRequestStatus status=DiscountRequestStatus.DRAFT;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="requested_by_user_id",nullable=false) private User requestedBy;
 @Column(nullable=false) private LocalDateTime createdAt;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="decided_by_user_id") private User decidedBy; private LocalDateTime approvedAt;
 @OneToMany(mappedBy="discountRequest",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("id ASC") private List<DiscountRequestItem> items=new ArrayList<>();
 @Column(length=500) private String remarks;
 protected DiscountRequest() {}
 public DiscountRequest(Invoice invoice,User user,String remarks){this.invoice=invoice;this.requestedBy=user;this.createdAt=LocalDateTime.now();this.remarks=remarks;}
 public void addItem(InvoiceItem item,BigDecimal amount,String reason){items.add(new DiscountRequestItem(this,item,amount,reason));}
 public void approve(User admin){if(status!=DiscountRequestStatus.DRAFT) throw new IllegalStateException("Only draft discount requests can be approved."); items.forEach(i->{i.approve(); i.getInvoiceItem().applyDiscount(i.getRequestedAmount());}); status=DiscountRequestStatus.APPROVED; decidedBy=admin; approvedAt=LocalDateTime.now();}
 public void reject(User admin){if(status!=DiscountRequestStatus.DRAFT) throw new IllegalStateException("Only draft discount requests can be rejected."); items.forEach(DiscountRequestItem::reject); status=DiscountRequestStatus.REJECTED; decidedBy=admin; approvedAt=LocalDateTime.now();}
 public Long getId(){return id;} public Invoice getInvoice(){return invoice;} public DiscountRequestStatus getStatus(){return status;} public User getRequestedBy(){return requestedBy;} public LocalDateTime getCreatedAt(){return createdAt;} public User getDecidedBy(){return decidedBy;} public LocalDateTime getApprovedAt(){return approvedAt;} public List<DiscountRequestItem> getItems(){return Collections.unmodifiableList(items);} public String getRemarks(){return remarks;}
 @Transient public BigDecimal getTotalRequested(){return items.stream().map(DiscountRequestItem::getRequestedAmount).reduce(BigDecimal.ZERO,BigDecimal::add);}
}
