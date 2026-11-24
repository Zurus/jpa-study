package com.example.jpalab.dto;

import com.example.jpalab.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;

    @NotBlank
    private String name;

    @PositiveOrZero
    private BigDecimal price;

    @PositiveOrZero
    private Integer stockQuantity;

    private Long categoryId;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        return dto;
    }

    public Product toEntity() {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price != null ? price : BigDecimal.ZERO);
        product.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
        return product;
    }
}
