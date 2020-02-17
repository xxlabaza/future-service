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

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.xxlabaza.test.future.service.cluster.ResponseContainer.HttpResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.SpringResource;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;

@Slf4j
abstract class IgniteRunnableAbstractHttpRequest implements IgniteRunnable {

  private static final long serialVersionUID = 4789018103252063120L;

  @SpringResource(resourceName = "asyncHttpClient")
  transient AsyncHttpClient http;

  protected CompletableFuture<HttpResponse> make (HttpRequest request) {
    val builder = new RequestBuilder()
        .setUri(toUri(request.getUri()))
        .setMethod(request.getMethod());

    val headers = request.getHeaders();
    if (headers != null) {
      builder.setHeaders(headers);
    }

    val body = request.getBody();
    if (body != null) {
      builder.setBody(body);
    }

    return http.executeRequest(builder)
        .toCompletableFuture()
        .exceptionally(this::log)
        .thenApply(HttpResponse::new);
  }

  private Uri toUri (URI uri) {
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

  private Response log (Throwable exception) {
    log.error("reqeust error", exception);
    return null;
  }
}
