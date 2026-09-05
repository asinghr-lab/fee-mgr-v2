package com.discover.app.billing.service;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.discover.app.billing.domain.FeeComponent;
import com.discover.app.billing.domain.FeeFrequency;
import com.discover.app.billing.domain.FeeStructure;
import com.discover.app.billing.domain.FeeStructureItem;
import com.discover.app.billing.repository.FeeComponentRepository;
import com.discover.app.billing.repository.FeeStructureRepository;
import com.discover.app.billing.repository.GradeFeeStructureRepository;
import com.discover.app.school.repository.AcademicYearRepository;
import com.discover.app.school.repository.GradeRepository;
import com.discover.app.school.repository.SchoolRepository;

@Configuration
public class BillingDataInitializer {

    @Bean
    CommandLineRunner initializeBilling(FeeComponentRepository componentRepo, FeeStructureRepository structureRepo,
            GradeFeeStructureRepository gradeFeeRepo) {
        return args -> {

            if (componentRepo.findAllByOrderByUpdatedAtDescIdDesc().isEmpty()) {
                var component1 = new FeeComponent("Tuition Fee");
                var component2 = new FeeComponent("Computer Fee");
                var component3 = new FeeComponent("Library Fee");
                var component4 = new FeeComponent("Transport Fee");
                var component5 = new FeeComponent("Annual Fee");
                

                componentRepo.save(component1);
                componentRepo.save(component2);
                componentRepo.save(component3);
                componentRepo.save(component4);
                componentRepo.save(component5);

                var structure1 = new FeeStructure("Grade1-Fees");

                structure1.addItem(new FeeStructureItem(component1, FeeFrequency.MONTHLY, new BigDecimal("2000.00")));
                structure1.addItem(new FeeStructureItem(component2, FeeFrequency.MONTHLY, new BigDecimal("500.00")));
                structure1.addItem(new FeeStructureItem(component3, FeeFrequency.QUARTERLY, new BigDecimal("1000.00")));
                structure1.addItem(new FeeStructureItem(component4, FeeFrequency.MONTHLY, new BigDecimal("1500.00")));
                structure1.addItem(new FeeStructureItem(component5, FeeFrequency.YEARLY, new BigDecimal("5000.00")));

                structureRepo.save(structure1);
            }
        };
    }
}
