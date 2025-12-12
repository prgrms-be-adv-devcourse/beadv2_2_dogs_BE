package com.barofarm.seller.config;

import com.barofarm.seller.farm.application.FarmService;
import com.barofarm.seller.farm.domain.FarmRepository;
import com.barofarm.seller.seller.domain.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public abstract class BaseServiceTest {

    @Autowired
    protected FarmRepository farmRepository;

    @Autowired
    protected SellerRepository sellerRepository;

    @Autowired
    protected FarmService farmService;
}
