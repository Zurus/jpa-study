package com.example.jpalab.repository;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ==== Тема 1: derived query (фильтрация, сортировка, LIMIT) ====

    /** Фильтрация по имени и цене + сортировка, переданная вызывающим кодом. */
    List<Product> findByNameContainingIgnoreCaseAndPriceLessThanEqual(String name, BigDecimal price, Sort sort);

    /** LIMIT: не более 3 позиций (Top-3), отсортированных по имени. */
    List<Product> findTop3ByPriceGreaterThanOrderByNameAsc(BigDecimal price);

    /** Обратная сторона associations: все товары категории (для OneToManyExampleTest). */
    List<Product> findByCategory(Category category);

    // ==== Тема 2: JPQL @Query (параметры, LIKE, сортировка, проекция поля) ====

    /** Именованный параметр, LIKE, явная сортировка. */
    @Query("""
            SELECT p FROM Product p
            WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :fragment, '%'))
            ORDER BY p.price DESC, p.id ASC
            """)
    List<Product> searchByNameFragment(@Param("fragment") String fragment);

    /** Проекция в record: выбираем только имя и цену. */
    @Query("""
            SELECT new com.example.jpalab.dto.ProductNamePrice(p.name, p.price)
            FROM Product p
            WHERE p.price >= :minPrice
            ORDER BY p.name ASC, p.id ASC
            """)
    List<ProductNamePrice> namePricesAtLeast(@Param("minPrice") BigDecimal minPrice);

    // ==== Тема 3: native-запрос ====

    /** SQL против реальной таблицы products (порядок колонок важен: id, name, price). */
    @Query(value = """
            SELECT p.id, p.name, p.price
            FROM products p
            WHERE p.price > :minPrice
            ORDER BY p.name ASC, p.id ASC
            """, nativeQuery = true)
    List<Object[]> nativeSearchByPriceAbove(@Param("minPrice") BigDecimal minPrice);

    // ==== Тема 5: "eager" загрузка через fetch join (контраст с LAZY) ====

    /** Одна SELECT с JOIN: категория загружается вместе с товаром. */
    @Query("SELECT p FROM Product p JOIN FETCH p.category")
    List<Product> findAllWithCategoryFetched();

    // ==== Тема 7: N+1 против @EntityGraph ====

    /** EntityGraph заставляет Hibernate добавить fetch join по пути category. */
    /**
     * Slice-запрос: возвращается только список (плюс одна лишняя строка для hasNext),
     * count-запрос НЕ выполняется — всего 1 SQL.
     */
    Slice<Product> findAllByOrderByPriceAsc(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByPriceAsc();

    // ==== Тема 9: @Modifying UPDATE/DELETE ====

    /**
     * Bulk UPDATE с flushAutomatically/clearAutomatically:
     * persistence context сбрасывается после запроса, дальнейшие
     * SELECT видят уже обновленные данные.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = :stock WHERE p.price >= :minPrice")
    int updateStockForExpensive(@Param("stock") int stock, @Param("minPrice") BigDecimal minPrice);

    /** Bulk DELETE с теми же флагами. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Product p WHERE p.stockQuantity = 0")
    int deleteOutOfStock();

    /**
     * Bulk UPDATE БЕЗ флагов: persistence context не очищается,
     * и немедленный SELECT может вернуть устаревшие сущности из PC.
     */
    @Modifying
    @Query("UPDATE Product p SET p.price = p.price + :delta")
    int bumpAllPricesBy(@Param("delta") BigDecimal delta);

    // ==== Тема 10: interface-based projection ====

    /** Open projection: getters маппятся на поля Product. */
    interface ProductSummary {
        String getName();
        BigDecimal getPrice();
        int getStockQuantity();
    }

    List<ProductSummary> findSummariesByPriceGreaterThanOrderByNameAsc(BigDecimal minPrice);
}
