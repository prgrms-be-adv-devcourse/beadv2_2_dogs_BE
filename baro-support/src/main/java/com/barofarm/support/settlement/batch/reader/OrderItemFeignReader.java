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

        // iterator가 없거나 다 소비했으면 다음 페이지 로드
        if (iterator == null || !iterator.hasNext()) {

            CustomPage<OrderItemSettlementResponse> response =
                client.getOrderItems(start, end, page, size);

            // 더 이상 읽을 데이터 없음 → 배치 종료
            if (response.content().isEmpty()) {
                return null;
            }

            iterator = response.content().iterator();
            page++;
        }

        return iterator.next();
    }
}
