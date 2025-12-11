package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.common.response.ResponseDto;
import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.inventory.presentation.dto.InventoryDecreaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PatchMapping("/decrease")
    public ResponseDto<Void> decreaseStock(@Valid @RequestBody InventoryDecreaseRequest request) {
        inventoryService.decreaseStock(request.toCommand());
        return ResponseDto.ok(null);
    }
}
