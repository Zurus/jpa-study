package com.example.jpalab.service;

import com.example.jpalab.dto.ProductDto;
import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream()
                .map(ProductDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(ProductDto::fromEntity);
    }

    @Transactional
    public ProductDto save(ProductDto dto) {
        Product saved = productRepository.save(dto.toEntity());
        return ProductDto.fromEntity(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductNamePrice> findNamePricePairs() {
        return productRepository.findAll().stream()
                .map(p -> new ProductNamePrice(p.getName(), p.getPrice()))
                .toList();
    }
}
