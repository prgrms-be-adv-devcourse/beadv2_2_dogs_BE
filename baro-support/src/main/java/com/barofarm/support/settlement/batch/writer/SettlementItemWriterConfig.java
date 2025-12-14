package com.barofarm.support.settlement.batch.writer;

import com.barofarm.support.settlement.domain.SettlementItem;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementItemWriterConfig {

    @Bean
    public JpaItemWriter<SettlementItem> settlementItemWriter(EntityManagerFactory emf) {
        JpaItemWriter<SettlementItem> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
