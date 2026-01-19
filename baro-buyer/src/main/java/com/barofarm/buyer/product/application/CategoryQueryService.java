package com.barofarm.buyer.product.application;

import com.barofarm.buyer.product.application.dto.CategoryListItem;
import com.barofarm.buyer.product.domain.Category;
import com.barofarm.buyer.product.domain.CategoryRepository;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import com.barofarm.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<CategoryListItem> getCategories(UUID parentId) {
    List<Category> categories;
    if (parentId == null) {
      categories = categoryRepository.findRoots();
    } else {
      ensureCategoryExists(parentId);
      categories = categoryRepository.findChildren(parentId);
    }
    return categories.stream().map(CategoryListItem::from).toList();
  }

  private void ensureCategoryExists(UUID categoryId) {
    if (categoryRepository.findById(categoryId).isEmpty()) {
      throw new CustomException(ProductErrorCode.CATEGORY_NOT_FOUND);
    }
  }
}
