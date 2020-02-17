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

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xxlabaza.test.future.service.Action;
import com.xxlabaza.test.future.service.cluster.ResponseContainer.HttpResponse;
import com.xxlabaza.test.future.service.cluster.ResponseContainer.Recipient;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.val;

@Value
@ToString(exclude = "body")
class HttpRequest implements Serializable {

  private static final long serialVersionUID = -7606825749140975726L;

  URI uri;

  String method;

  Map<String, List<String>> headers;

  byte[] body;

  HttpRequest (@NonNull Action action) {
    val request = action.getRequest();
    uri = request.getUri();
    method = request.getMethod().name();
    headers = request.getHeaders().orElse(null);
    body = request.getBody().orElse(null);
  }

  HttpRequest (@NonNull Recipient recipient, @NonNull HttpResponse response) {
    uri = recipient.getUri();
    method = recipient.getMethod();
    headers = new HashMap<>();

    ofNullable(response.getHeaders())
        .filter(v -> recipient.isIncludeResponseHeaders())
        .ifPresent(headers::putAll);

    ofNullable(recipient.getHeaders())
        .ifPresent(headers::putAll);

    body = response.getBody();
  }
}
