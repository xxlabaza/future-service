/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xxlabaza.test.future.service.cluster;

import static org.apache.ignite.cache.CacheAtomicityMode.ATOMIC;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;
import static org.apache.ignite.cache.CacheWriteSynchronizationMode.PRIMARY_SYNC;

import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSpring;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ConfigurationCluster {

  @Autowired
  DiscoverySpi discoverySpi;

  @Bean(destroyMethod = "close")
  Ignite ignite (ApplicationContext applicationContext) throws IgniteCheckedException {
    val configuration = igniteConfiguration();
    val ignite = IgniteSpring.start(configuration, applicationContext);

    val cluster = ignite.cluster();
    cluster.active(true);
    cluster.setBaselineTopology(cluster.forServers().nodes());

    return ignite;
  }

  @Bean
  IgniteConfiguration igniteConfiguration () {
    return new IgniteConfiguration()
        .setIgniteInstanceName("future-service")
        .setDiscoverySpi(discoverySpi)
        .setCacheConfiguration(requestsRepositoryConfiguration())
        .setNetworkTimeout(5_000)
        .setClientMode(false)
        .setMetricsLogFrequency(0)
        .setPeerClassLoadingEnabled(false)
        .setFailureDetectionTimeout(10_000)
        .setClientFailureDetectionTimeout(30_000);
  }

  @Bean
  CacheConfiguration<HttpRequest, ResponseContainer> requestsRepositoryConfiguration () {
    return new CacheConfiguration<HttpRequest, ResponseContainer>()
        .setName("requestsRepository")
        .setCacheMode(PARTITIONED)
        .setBackups(1)
        .setAtomicityMode(ATOMIC)
        .setWriteSynchronizationMode(PRIMARY_SYNC);
  }

  @Bean
  IgniteCache<HttpRequest, ResponseContainer> requestsRepository (Ignite ignite) {
    val configuration = requestsRepositoryConfiguration();
    return ignite.getOrCreateCache(configuration);
  }
}
