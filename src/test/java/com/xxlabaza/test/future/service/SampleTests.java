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

package com.xxlabaza.test.future.service;

import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.net.URI;

import com.xxlabaza.test.future.service.cluster.ClusterActionService;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = DEFINED_PORT,
    properties = "cluster.tcp.enabled=true"
)
class SampleTests {

  @Autowired
  ClusterActionService actionService;

  @Value("http://localhost:${server.port}")
  String host;

  @Test
  void test () throws Exception {
    val action = Action.builder()
        .request(Action.Request.builder()
            .method(POST)
            .url(new URI(host + "/post"))
            .build())
        .sendTo(of(Action.SendTo.builder()
            .method(PUT)
            .url(new URI(host + "/put"))
            .build()))
        .build();

    actionService.submit(action);
    actionService.submit(action);
    actionService.submit(action);
    actionService.submit(action);
    actionService.submit(action);

    SECONDS.sleep(10);

    assertThat(TestController.POST_COUNTER.sum()).isEqualTo(1);
    assertThat(TestController.PUT_COUNTER.sum()).isEqualTo(5);
  }
}
