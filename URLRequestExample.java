package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class URLRequestExample {
    @Autowired
    RestTemplate restTemplate;
    Map<String, List<String>> urlMap = Map.of("c1",
            List.of("https://my-json-server.typicode.com/typicode/demo/posts",
                    "https://my-json-server.typicode.com/typicode/demo/comments",
                    "https://my-json-server.typicode.com/typicode/demo/posts"),
            "c2",
            List.of("https://my-json-server.typicode.com/typicode/demo/profile",
                    "https://my-json-server.typicode.com/typicode/demo/posts",
                    "https://my-json-server.typicode.com/typicode/demo/posts"),
            "c3",
            List.of("https://my-json-server.typicode.com/typicode/demo/posts",
                    "https://my-json-server.typicode.com/typicode/demo/profile",
                    "https://my-json-server.typicode.com/typicode/demo/comments"));

    @GetMapping("/http-status-codes")
    public Map<String, List<Map<String, String>>> getHttpStatusCodes() throws ExecutionException, InterruptedException {
        System.out.println(java.lang.Thread.activeCount());

        // Use a CompletableFuture to execute the HTTP requests in parallel
        CompletableFuture[] futures = urlMap.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> {
                    String category = entry.getKey();
                    List<String> urls = entry.getValue();

                    // Create a list to store the results for this category
                    List<Map<String, String>> results = new ArrayList<>();

                    // Iterate over the URLs in the list and get the HttpStatusCode
                    for (String url : urls) {
                        try {
                            System.out.println("URL: " + url);
                            results.add(Map.of(url, Integer.toString(restTemplate.getForEntity(url, String.class).getStatusCode().value())));
                        } catch (Exception e) {
                            // If there is an error, add a pair with the URL and an error message
                            results.add(Map.of(url, "Error getting HttpStatusCode: " + e.getMessage()));
                        }
                    }

                    // Return the results for this category
                    return Map.of(category, results);
                })).toArray(CompletableFuture[]::new);

        // Wait for all the CompletableFutures to complete
        CompletableFuture.allOf(futures).join();

        // Create a Map to store the results
        Map<String, List<Map<String, String>>> resultMap = new HashMap<>();

        // Iterate over the CompletableFutures and add the results to the result map
        for (CompletableFuture<Map<String, List<Map<String, String>>>> future : futures) {
            Map<String, List<Map<String, String>>> pair = future.get();
            System.out.println("Futures: " + pair);
            resultMap.putAll(pair);
        }

        return resultMap;
    }

}
