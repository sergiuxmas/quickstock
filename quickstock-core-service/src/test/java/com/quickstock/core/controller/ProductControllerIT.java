package com.quickstock.core.controller;

import com.quickstock.core.security.DatabaseUserDetailsService;
import com.quickstock.core.service.ProductService;
import com.quickstock.core.testconfig.MockMvcITConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@Import(MockMvcITConfig.class)
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    void getProducts_withInexistentName_returnsEmptyPage() throws Exception {
        Pageable defaultPageable = PageRequest.of(0, 20);
        when(productService.listProducts(
                eq(null),
                eq(null),
                eq("inexistent"),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(Page.empty(defaultPageable));

        mockMvc.perform(get("/products").param("name", "inexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }
}
