//package com.triply.tripapp.config;
//
//import com.azure.ai.openai.OpenAIClient;
//import com.azure.ai.openai.OpenAIClientBuilder;
//import com.azure.core.credential.AzureKeyCredential;
//import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
//import io.micrometer.observation.ObservationRegistry;
//import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
//import org.springframework.ai.document.MetadataMode;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class EmbeddingConfig {
//
//    @Value("${spring.ai.azure.openai.api-key}")
//    private String apiKey;
//
//    @Value("${spring.ai.azure.openai.endpoint}")
//    private String endpoint;
//
//    @Value("${spring.ai.azure.openai.embedding.options.deployment-name:text-embedding-3-small}")
//    private String deploymentName;
//
//    @Value("${spring.ai.azure.openai.embedding.options.model-name:text-embedding-3-small}")
//    private String modelName;
//
//    @Value("${spring.ai.azure.openai.embedding.options.dimension:1536}")
//    private Integer dimension;
//
//    @Bean
//    public EmbeddingModel azureOpenAiEmbeddingModel() {
//        // Tạo Azure OpenAI Client
//        OpenAIClient openAIClient = new OpenAIClientBuilder()
//            .credential(new AzureKeyCredential(apiKey))
//            .endpoint(endpoint)
//            .buildClient();
//
//        AzureOpenAiEmbeddingOptions defaultOptions = AzureOpenAiEmbeddingOptions.builder()
//            .deploymentName(deploymentName)
//            .user("tripapp-system")
//            .dimensions(dimension)
//            .build();
//
//        return new AzureOpenAiEmbeddingModel(
//            openAIClient,
//            MetadataMode.EMBED,
//            defaultOptions,
//            ObservationRegistry.create()
//        );
//    }
//}
//
