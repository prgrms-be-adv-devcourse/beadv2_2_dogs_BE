package com.barofarm.support.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 설정
 *
 * <p>Spring Data Elasticsearch 6.0.0 + Spring Boot 4.0.0 조합 - ElasticsearchConfiguration을 확장하면 다음
 * Bean들이 자동 생성됨: - Rest5Client (저수준 REST 클라이언트) - ElasticsearchClient (Elasticsearch Java API
 * Client) - ElasticsearchOperations (= ElasticsearchTemplate)
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.barofarm.support.search")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.uris:http://localhost:9200}")
  private String elasticsearchUri;

  @Override
  public ClientConfiguration clientConfiguration() {
    // "http://localhost:9200" -> "localhost:9200"
    String hostAndPort = elasticsearchUri.replace("http://", "").replace("https://", "");

    return ClientConfiguration.builder()
        .connectedTo(hostAndPort)
        .withConnectTimeout(java.time.Duration.ofSeconds(10))
        .withSocketTimeout(java.time.Duration.ofSeconds(30))
        .build();
  }
}
