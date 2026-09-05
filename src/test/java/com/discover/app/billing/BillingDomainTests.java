package com.discover.app.billing;

import com.discover.app.billing.domain.*;
import com.discover.app.school.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class BillingDomainTests {
    @Test void feeComponentIsActiveAndCanBeDeactivated() {
        var c=new FeeComponent("Tuition Fee");
        assertThat(c.getStatus()).isEqualTo(FeeStatus.ACTIVE);
        c.deactivate();
        assertThat(c.getStatus()).isEqualTo(FeeStatus.INACTIVE);
    }
    @Test void feeStructureItemStoresFrequencyAndAmount() {
        var c=new FeeComponent("Tuition Fee");
        var s=new FeeStructure("STANDARD");
        s.addItem(new FeeStructureItem(c,FeeFrequency.MONTHLY,new BigDecimal("3000.00")));
        assertThat(s.getItems()).hasSize(1);
        assertThat(s.getItems().get(0).getFrequency()).isEqualTo(FeeFrequency.MONTHLY);
        assertThat(s.getItems().get(0).getAmount()).isEqualByComparingTo("3000.00");
    }
    @Test void gradeAssignmentIsActiveForEffectiveDate() {
        var school=new School("Test",null,null,null); var grade=new Grade(school,"Grade1","A","Grade1-A",1);
        var c=new FeeComponent("Tuition"); var s=new FeeStructure("Standard"); s.addItem(new FeeStructureItem(c,FeeFrequency.MONTHLY,new BigDecimal("1000")));
        var a=new GradeFeeStructure(grade,s,LocalDate.of(2026,4,1));
        assertThat(a.isActiveOn(LocalDate.of(2026,4,1))).isTrue();
        assertThat(a.isActiveOn(LocalDate.of(2027,3,31))).isTrue();
    }
}
