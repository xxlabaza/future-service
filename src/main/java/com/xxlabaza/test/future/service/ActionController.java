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

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.xxlabaza.test.future.service.cluster.ClusterActionService;
import com.xxlabaza.test.future.service.proto.Proto;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
class ActionController {

  @Autowired
  ClusterActionService service;

  @PostMapping("/action")
  @ResponseStatus(ACCEPTED)
  void post (@RequestBody Proto.Action proto) {
    val action = Action.from(proto);
    service.submit(action);
  }

  @ExceptionHandler
  @ResponseStatus(BAD_REQUEST)
  void handleException (Throwable throwable) {
    log.warn("bad reqeust", throwable);
  }
}
