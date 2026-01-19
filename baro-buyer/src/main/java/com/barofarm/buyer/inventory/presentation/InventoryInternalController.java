package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.inventory.application.InventoryFacadeService;
import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.inventory.presentation.dto.InventoryCancelRequest;
import com.barofarm.buyer.inventory.presentation.dto.InventoryReserveRequest;
import com.barofarm.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryInternalController {

    private final InventoryFacadeService inventoryFacadeService;
    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseDto<Void> reserveInventory(@Valid @RequestBody InventoryReserveRequest request) {
        log.info("createOrder called: {}", request);
        inventoryFacadeService.reserveInventory(request.toCommand());
        return ResponseDto.ok(null);
    }

    @PostMapping("/cancel")
    public ResponseDto<Void> cancelInventory(@Valid @RequestBody InventoryCancelRequest request) {
        inventoryFacadeService.cancelInventory(request.toCommand());
        return ResponseDto.ok(null);
    }
}
