package com.barofarm.buyer.product.presentation;

import com.barofarm.buyer.product.application.CategoryQueryService;
import com.barofarm.buyer.product.application.dto.CategoryListItem;
import com.barofarm.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1}/categories")
public class CategoryQueryController {

  private final CategoryQueryService categoryQueryService;

  @Operation(
      summary = "카테고리 목록 조회 API",
      description = "parentId가 없으면 1차, 있으면 해당 하위 카테고리를 조회합니다.")
  @GetMapping
  public ResponseDto<List<CategoryListItem>> getCategories(
      @RequestParam(required = false) UUID parentId) {
    return ResponseDto.ok(categoryQueryService.getCategories(parentId));
  }
}
