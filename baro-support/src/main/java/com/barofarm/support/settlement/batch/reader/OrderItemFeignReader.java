package com.barofarm.support.settlement.batch.reader;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.settlement.client.OrderItemSettlementResponse;
import com.barofarm.support.settlement.client.OrderSettlementClient;
import java.time.LocalDate;
import java.util.Iterator;
import org.springframework.batch.item.ItemReader;

public class OrderItemFeignReader implements ItemReader<OrderItemSettlementResponse> {

    private final OrderSettlementClient client;
    private final LocalDate start;
    private final LocalDate end;

    private Iterator<OrderItemSettlementResponse> iterator;
    private int page = 0;
    private final int size = 200;

    public OrderItemFeignReader(OrderSettlementClient client, LocalDate start, LocalDate end) {
        this.client = client;
        this.start = start;
        this.end = end;
    }

    @Override
    public OrderItemSettlementResponse read() {

        while (iterator == null || !iterator.hasNext()) {

            CustomPage<OrderItemSettlementResponse> response =
                client.getOrderItems(start, end, page, size);

            if (response.content().isEmpty()) {
                return null; // batch 종료 (no more data)
            }

            iterator = response.content().iterator();

            if (!response.hasNext()) {
                page = Integer.MAX_VALUE; // 마지막 페이지 처리
            } else {
                page++;
            }
        }

        return iterator.next();
    }
}
