package org.springboot.service.junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springboot.service.ProductServiceImpl;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static org.mockito.Mockito.*;
@Profile("test")
public class ProductServiceImplTest {

    private ProductServiceImpl productService;
    private ProductServiceImpl productServiceSpy;

    @BeforeEach
    public void setUp() {
        productService = new ProductServiceImpl(null, null,null,null);
        productServiceSpy = Mockito.spy(productService);
    }

    @Test
    public void testUpdateProductsAfterOrderDeletion_ShouldUpdateProductsCorrectly() throws Exception {
        // Arrange
        Product product = new Product();
        product.setEan("1234567890123");
        product.setAvailable(2);
        product.setSold(5);

        List<Product> products = List.of(product);

        doReturn(product).when(productServiceSpy).updateProduct(anyString(), any(Product.class));

        // Act
        productServiceSpy.updateProductsAfterOrderDeletion(products);

        // Assert
        assert product.getAvailable() == 3;
        assert product.getSold() == 4;

        verify(productServiceSpy, times(1)).updateProduct("1234567890123", product);
    }

    @Test
    public void testUpdateProductsAfterOrderDeletion_WhenProductNotFound_ShouldThrowException() throws Exception {
        // Arrange
        Product product = new Product();
        product.setEan("9999999999999");
        product.setAvailable(1);
        product.setSold(1);

        List<Product> products = List.of(product);

        // Simuluj výnimku
        doThrow(new ProductNotFoundException("Not found")).when(productServiceSpy).updateProduct(anyString(), any(Product.class));

        // Act & Assert
        try {
            productServiceSpy.updateProductsAfterOrderDeletion(products);
            assert false : "Expected RuntimeException was not thrown";
        } catch (RuntimeException e) {
            assert e.getMessage().contains("Product with EAN 9999999999999 not found");
        }
    }
}
