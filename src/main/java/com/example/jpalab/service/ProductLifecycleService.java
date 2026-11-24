package com.example.jpalab.service;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductLifecycleService {

    private final ProductRepository productRepository;

    public ProductLifecycleService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product restock(Long productId, int newStockQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setStockQuantity(newStockQuantity);
        return product;
    }

    @Transactional
    public Product updatePrice(Long productId, java.math.BigDecimal newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setPrice(newPrice);
        return product;
    }
}
