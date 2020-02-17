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
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.GET;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.xxlabaza.test.future.service.proto.Proto;

import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.springframework.http.HttpMethod;

@Value
@Builder
public class Action {

  static Action from (Proto.Action proto) {
    return Action.builder()
        .request(Request.from(proto.getRequest()))
        .sendTo(ofNullable(proto.getSendTo())
            .map(SendTo::from))
        .build();
  }

  @NonNull
  Request request;

  @NonNull
  Optional<SendTo> sendTo;

  @Value
  @Builder
  public static class Request {

    @SneakyThrows
    static Request from (@NonNull Proto.Action.Request proto) {
      String stringUri = proto.getUri();
      if (stringUri.startsWith("http") == false) {
        stringUri = "http://".concat(stringUri);
      }
      val uri = new URI(stringUri);

      val method = ofNullable(proto.getMethod())
          .map(Proto.Action.HttpMethod::name)
          .map(HttpMethod::resolve)
          .filter(Objects::nonNull)
          .orElse(GET);

      val headers = ofNullable(proto.getHeadersList())
          .map(it -> it.stream()
              .collect(groupingBy(Proto.Action.Header::getName,
                                  mapping(Proto.Action.Header::getValue, toList()))));

      val body = ofNullable(proto.getBody())
          .map(String::getBytes);

      return Request.builder()
          .uri(uri)
          .method(method)
          .headers(headers)
          .body(body)
          .build();
    }

    @NonNull
    URI uri;

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
  public static class SendTo {

    @SneakyThrows
    static SendTo from (@NonNull Proto.Action.SendTo proto) {
      String stringUri = proto.getUri();
      if (stringUri.startsWith("http") == false) {
        stringUri = "http://".concat(stringUri);
      }
      val uri = new URI(stringUri);

      val method = ofNullable(proto.getMethod())
          .map(Proto.Action.HttpMethod::name)
          .map(HttpMethod::resolve)
          .filter(Objects::nonNull)
          .orElse(GET);

      val headers = ofNullable(proto.getHeadersList())
          .map(it -> it.stream()
              .collect(groupingBy(Proto.Action.Header::getName,
                                  mapping(Proto.Action.Header::getValue, toList()))));

      return SendTo.builder()
          .uri(uri)
          .method(method)
          .headers(headers)
          .includeResponseHeaders(proto.getIncludeResponseHeaders())
          .build();
    }

    @NonNull
    URI uri;

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
