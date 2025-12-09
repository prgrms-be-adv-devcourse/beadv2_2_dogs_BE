package com.barofarm.order.order.application;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.dto.request.DirectOrderCreateRequest;
import com.barofarm.order.order.domain.OrderItemRepository;
import com.barofarm.order.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ResponseDto<> createDirectOrder(UUID mockUserId, DirectOrderCreateRequest request){

    }

}
