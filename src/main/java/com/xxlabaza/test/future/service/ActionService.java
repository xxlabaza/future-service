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

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ActionService {

  static Set<String> ignoreHeaders = new HashSet<>(asList(
      "content-length",
      "connection"
  ));

  static Uri toUri (URI uri) {
    return new Uri(
        uri.getScheme(),
        uri.getUserInfo(),
        uri.getHost(),
        uri.getPort(),
        uri.getPath(),
        uri.getQuery(),
        uri.getFragment()
    );
  }

  @Autowired
  AsyncHttpClient http;

  Map<Action.Request, ResponseContainer> repository = new ConcurrentHashMap<>();

  void invoke (@NonNull Action action) {
    val container = repository
        .computeIfAbsent(action.getRequest(), ResponseContainer::new);

    action.getSendTo()
        .ifPresent(container::subscribe);
  }

  void forward (Response response, Action.SendTo to) {
    val builder = new RequestBuilder()
        .setUri(toUri(to.getUrl()))
        .setMethod(to.getMethod().name());

    if (to.isIncludeResponseHeaders()) {
      response.getHeaders()
          .entries()
          .stream()
          .filter(it -> {
            val name = it.getKey().toLowerCase(ENGLISH);
            return ignoreHeaders.contains(name) == false;
          })
          .forEach(it -> builder.addHeader(it.getKey(), it.getValue()));
    }

    to.getHeaders()
        .ifPresent(builder::setHeaders);

    builder.setBody(response.getResponseBodyAsBytes());

    http.executeRequest(builder);
  }

  @Value
  private class ResponseContainer {

    ListenableFuture<Response> future;

    ResponseContainer(Action.Request action) {
      val request = toRequest(action);
      future = http.executeRequest(request);
    }

    void subscribe (Action.SendTo to) {
      future.toCompletableFuture().thenAcceptAsync(response -> {
        forward(response, to);
      });
    }

    private Request toRequest (Action.Request request) {
      val builder = new RequestBuilder()
          .setUri(toUri(request.getUrl()))
          .setMethod(request.getMethod().name());

      request.getHeaders()
          .ifPresent(builder::setHeaders);

      request.getBody()
          .ifPresent(builder::setBody);

      return builder.build();
    }
  }
}
