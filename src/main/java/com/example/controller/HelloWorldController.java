package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Hello World", description = "Hello World API endpoints")
public class HelloWorldController {

    @Operation(
        summary = "Get hello message",
        description = "Returns a simple hello world message",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved hello message"
            )
        }
    )
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @Operation(
        summary = "Post a message",
        description = "Send a message and get a response back",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully processed the message"
            )
        }
    )
    @PostMapping("/hello")
    public String postHello(
        @Parameter(description = "Message to be processed")
        @RequestBody(required = false) String message
    ) {
        return "Received message: " + (message != null ? message : "No message provided");
    }
} 