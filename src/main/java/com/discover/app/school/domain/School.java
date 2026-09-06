package com.discover.app.school.domain;

import jakarta.persistence.*;

@Entity
@Table(name="schools")
public class School {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String name;
 @Column(length=255) private String address;
 @Column(length=30) private String phoneNumber;
 @Column(length=150) private String email;
 protected School() {}
 public School(String name,String address,String phoneNumber,String email){this.name=name;this.address=address;this.phoneNumber=phoneNumber;this.email=email;}
 public Long getId(){return id;} public String getName(){return name;} public String getAddress(){return address;} public String getPhoneNumber(){return phoneNumber;} public String getEmail(){return email;}
 public void update(String name,String address,String phoneNumber,String email){this.name=name;this.address=address;this.phoneNumber=phoneNumber;this.email=email;}
}
