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

import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import lombok.Data;
import lombok.val;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.elb.TcpDiscoveryElbIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
class ConfigurationClusterDiscovery {

  @Autowired
  ClusterProperties properties;

  @Bean
  DiscoverySpi awsDiscoverySpi (TcpDiscoveryIpFinder ipFinder) {
    return new TcpDiscoverySpi()
        .setAckTimeout(5_000)
        .setJoinTimeout(0)
        .setNetworkTimeout(5_000)
        .setReconnectCount(10)
        .setSocketTimeout(5_000)
        .setIpFinder(ipFinder);
  }

  @Bean
  @ConditionalOnProperty(name = "cluster.aws.enabled", havingValue = "true")
  TcpDiscoveryIpFinder elbTcpDiscoveryIpFinder () {
    val credentials = new BasicAWSCredentials(
        properties.getAws().getKey().getAccess(),
        properties.getAws().getKey().getSecret()
    );
    val credentialsProvider = new AWSStaticCredentialsProvider(credentials);

    val result = new TcpDiscoveryElbIpFinder();
    result.setLoadBalancerName(properties.getAws().getElb().getName());
    result.setRegion(properties.getAws().getElb().getRegion());
    result.setCredentialsProvider(credentialsProvider);
    return result;
  }

  @Bean
  @ConditionalOnProperty(name = "cluster.tcp.enabled", havingValue = "true")
  TcpDiscoveryIpFinder tcpDiscoverySpi () {
    val result = new TcpDiscoveryMulticastIpFinder();
    val addresses = properties.getTcp().getAddresses();
    result.setAddresses(addresses);
    return result;
  }

  @Bean
  @ConditionalOnMissingBean
  TcpDiscoveryIpFinder defaultTcpDiscoverySpi () {
    return new TcpDiscoveryMulticastIpFinder();
  }

  @Data
  @Component
  @ConfigurationProperties("cluster")
  static class ClusterProperties {

    Aws aws;

    Tcp tcp;

    @Data
    static class Aws {

      boolean enabled;

      Key key;

      Elb elb;

      @Data
      static class Elb {

        String name;

        String region;
      }

      @Data
      static class Key {

        String access;

        String secret;
      }
    }

    @Data
    static class Tcp {

      boolean enabled;

      List<String> addresses;
    }
  }
}
