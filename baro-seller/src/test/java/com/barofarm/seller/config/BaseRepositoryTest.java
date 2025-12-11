package com.barofarm.seller.config;

import com.barofarm.seller.farm.domain.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected FarmRepository farmRepository;
}
