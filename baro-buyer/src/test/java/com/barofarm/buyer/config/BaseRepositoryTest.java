package com.barofarm.buyer.config;

import com.barofarm.buyer.product.domain.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected ProductRepository productRepository;
}
