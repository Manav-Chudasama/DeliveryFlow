package com.deliveryflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("DeliveryFlow API")
                .version("1.0")
                .description("""
                        Delivery management API for customers, drivers and delivery orders.

                        Order lifecycle: CREATED -> ASSIGNED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED.
                        Any non-terminal order may be CANCELLED.

                        Drivers must be AVAILABLE to be assigned; they become BUSY on assignment and
                        return to AVAILABLE when the order is delivered or cancelled.
                        """));
    }
}
