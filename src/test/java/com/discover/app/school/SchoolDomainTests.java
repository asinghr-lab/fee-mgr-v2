package com.discover.app.school;

import com.discover.app.school.domain.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchoolDomainTests {
 @Test void enrollmentStartsRequestedAndCanBeApproved(){
   var school=new School("Test School","A","1","a@b.com");
   var grade=new Grade(school,"Grade 1",1);
   var year=new AcademicYear("2026-2027",LocalDate.of(2026,4,1),LocalDate.of(2027,3,31));
   var student=new Student("A001","Test","Student",LocalDate.now(),null,null,null,null);
   var user=new com.discover.app.identity.domain.User("staff","x",true); user.addRole(com.discover.app.identity.domain.Role.STAFF);
   var enrollment=new StudentEnrollment(student,year,grade,user,LocalDateTime.now());
   assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.REQUESTED);
   var admin=new com.discover.app.identity.domain.User("admin","x",true); admin.addRole(com.discover.app.identity.domain.Role.ADMIN);
   enrollment.approve(admin,LocalDateTime.now());
   assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.APPROVED);
   assertThat(enrollment.getApprovedBy()).isSameAs(admin);
 }
 @Test void cancelledRequestCannotBeApproved(){
   var school=new School("Test School",null,null,null); var grade=new Grade(school,"G1",1); var year=new AcademicYear("2026-2027",LocalDate.of(2026,4,1),LocalDate.of(2027,3,31)); var student=new Student("A1","A",null,LocalDate.now(),null,null,null,null); var u=new com.discover.app.identity.domain.User("staff","x",true); var e=new StudentEnrollment(student,year,grade,u,LocalDateTime.now()); e.cancel();
   assertThatThrownBy(() -> e.approve(u,LocalDateTime.now())).isInstanceOf(IllegalStateException.class);
 }
}
