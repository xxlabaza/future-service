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

import static java.util.Optional.empty;
import static org.springframework.http.HttpMethod.GET;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.HttpMethod;

@Value
@Builder
@JsonDeserialize(using = ActionDeserializer.class)
class Action {

  @NonNull
  Request request;

  @NonNull
  Optional<SendTo> sendTo;

  @Value
  @Builder
  static class Request {

    @NonNull
    URI url;

    @NonNull
    @Builder.Default
    HttpMethod method = GET;

    @NonNull
    @Builder.Default
    Optional<Map<String, List<String>>> headers = empty();

    @NonNull
    @Builder.Default
    Optional<byte[]> body = empty();
  }

  @Value
  @Builder
  static class SendTo {

    @NonNull
    URI url;

    @NonNull
    @Builder.Default
    HttpMethod method = GET;

    @NonNull
    @Builder.Default
    Optional<Map<String, List<String>>> headers = empty();

    @Builder.Default
    boolean includeResponseHeaders = true;
  }
}
