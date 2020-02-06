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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.atomic.LongAdder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

  static final LongAdder POST_COUNTER = new LongAdder();

  static final LongAdder PUT_COUNTER = new LongAdder();

  String response = "{\"text\":\"Hello world\"}";

  @PostMapping("/post")
  String action () throws Exception {
    POST_COUNTER.increment();
    SECONDS.sleep(1);
    return response;
  }

  @PutMapping("/put")
  void put (@RequestBody String string) {
    if (response.equals(string)) {
      PUT_COUNTER.increment();
    }
  }
}
