package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.common.response.ResponseDto;
import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.inventory.presentation.dto.InventoryDecreaseRequest;
import com.barofarm.buyer.inventory.presentation.dto.InventoryIncreaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/inventories")
@RequiredArgsConstructor
public class InventoryController implements InventorySwaggerApi{

    private final InventoryService inventoryService;

    @PostMapping("/decrease")
    public ResponseDto<Void> decreaseStock(@Valid @RequestBody InventoryDecreaseRequest request) {
        inventoryService.decreaseStock(request.toCommand());
        return ResponseDto.ok(null);
    }

    @PostMapping("/increase")
    public ResponseDto<Void> increaseStock(@Valid @RequestBody InventoryIncreaseRequest request) {
        inventoryService.increaseStock(request.toCommand());
        return ResponseDto.ok(null);
    }
}
