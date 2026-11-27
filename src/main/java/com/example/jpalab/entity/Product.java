package com.example.jpalab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @PositiveOrZero
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Тема 15 (@ElementCollection): коллекции простых значений (стартовый код, не решение).
    // Методы-артефакты студента, работающие с ними, — в ProductElementCollectionService.

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attr_key")
    @Column(name = "attr_value")
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    // Тема 16 (@ManyToMany): связь со сущностью Tag. Product — владелец
    // (FK в промежуточной таблице product_tag_link), Tag.products — mappedBy.
    // Имя поля productTags (а не tags) — имя tags уже занято строковой
    // коллекцией темы 15. Методы-артефакты студента — в ProductTagService.

    @ManyToMany
    @JoinTable(
            name = "product_tag_link",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> productTags = new HashSet<>();

    // Тема 18 (embedded-типы и конвертеры): статус товара. Стартовое состояние —
    // явный EnumType.STRING; задания темы 18 показывают альтернативные маппинги.

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
