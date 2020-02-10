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

import static java.util.concurrent.TimeUnit.SECONDS;

import com.xxlabaza.test.future.service.cluster.ResponseContainer.HttpResponse;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.SpringResource;

@Slf4j
@ToString(of = "request")
class IgniteRunnableExecuteRequest extends IgniteRunnableAbstractHttpRequest {

  private static final long serialVersionUID = 7605360035728307801L;

  @IgniteInstanceResource
  transient Ignite ignite;

  @SpringResource(resourceName = "requestsRepository")
  transient IgniteCache<HttpRequest, ResponseContainer> requestsRepository;

  HttpRequest request;

  IgniteRunnableExecuteRequest (HttpRequest request) {
    super();
    this.request = request;
  }

  @Override
  public void run () {
    log.info("{}", request);
    make(request).thenAccept(this::handle);
  }

  private void handle (HttpResponse response) {
    requestsRepository
        .invokeAsync(request, new EntryProcessorCompleteRequest(), response)
        .listen(this::handle);
  }

  private void handle (IgniteFuture<EntryProcessorResult> future) {
    EntryProcessorResult result;
    try {
      result = future.get(5, SECONDS);
    } catch (IgniteException ex) {
      log.error("failed to complete {}", request, ex);
      return;
    }

    if (result.isEmpty()) {
      return;
    }

    val task = result.getTask();
    val executor = ignite.executorService();
    executor.execute(task);
  }
}
