package com.jkc.microservices.api.composite.product;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

@SwaggerDefinition(info = @Info(
        title = "Api contains methods to get the product by productID",
        version = "0.0.1",
        description = "Rest Api for product-composite information",
        contact = @Contact(
                email = "anxtr@gmail.com",
                name = "Absolute Chemist",
                url = "https://github.com/JatinderKumarChaurasia/Product-Manager/tree/master/api"
        ),
        termsOfService = "Open Source",
        license = @License(
                name = "Apache 2.0 Open Source",
                url = "http://www.apache.org/licenses/LICENSE-2.0"
        )
))
public interface ProductCompositeService {

    /**
     * usage : curl $HOST:$PORT/product-composite/1
     *
     * @param productID "productID: int required"
     * @return composite productInfo , if found else null
     */
    @ApiOperation(
            value = "${api.product-composite.get-composite-product.description}",
            nickname = "get product by id",
            tags = "product composite",
            notes = "${api.product-composite.get-composite-product.notes}"
    )

    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Ok"),
                    @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
                    @ApiResponse(code = 404, message = "Not found, product with this id does not exist."),
                    @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
            }
    )
    @GetMapping(value = "/product-composite/{productID}", produces = "application/json")
    ProductAggregate getProductComposite(@PathVariable int productID);

    /**
     * usage: curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param productAggregate description: productAggregate:ProductAggregate
     */
    @ApiOperation(
            value = "${api.product-composite.create-composite-product.description}",
            nickname = "get product by id",
            tags = "product composite",
            notes = "${api.product-composite.create-composite-product.notes}"
    )

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    }
    )
    @PostMapping(value = {"/product-composite"}, consumes = {"application/json"})
    void createProductComposite(@RequestBody ProductAggregate productAggregate);

    /**
     * usage: curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productID description:"delete product-composite by id"
     */
    @ApiOperation(
            value = "${api.product-composite.delete-composite-product.description}",
            nickname = "delete product by id",
            tags = "product composite",
            notes = "${api.product-composite.delete-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    }
    )
    @DeleteMapping(value = "/product-composite/{productID}")
    void deleteProductComposite(@PathVariable int productID);
}
