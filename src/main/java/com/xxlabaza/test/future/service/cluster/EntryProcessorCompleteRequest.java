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

import static com.xxlabaza.test.future.service.cluster.EntryProcessorResult.empty;
import static com.xxlabaza.test.future.service.cluster.EntryProcessorResult.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.Optional;
import javax.cache.processor.MutableEntry;

import com.xxlabaza.test.future.service.cluster.ResponseContainer.HttpResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.resources.IgniteInstanceResource;

@Slf4j
class EntryProcessorCompleteRequest implements CacheEntryProcessor<HttpRequest, ResponseContainer, EntryProcessorResult> {

  private static final long serialVersionUID = 3608860000721117801L;

  @IgniteInstanceResource
  transient Ignite ignite;

  @Override
  public EntryProcessorResult process (MutableEntry<HttpRequest, ResponseContainer> entry, Object... arguments) {
    val container = ofNullable(entry.getValue())
        .orElseGet(ResponseContainer::new);

    val response = getHttpResponse(arguments).orElse(null);
    if (response == null) {
      log.warn("{} doesn't have response argument", entry.getKey());
      return empty();
    }

    val recipients = container.complete(response);
    entry.setValue(container);

    log.info("{}", container);
    if (recipients == null || recipients.isEmpty()) {
      return empty();
    }

    val requests = recipients.stream()
        .map(it -> new HttpRequest(it, response))
        .collect(toList());

    val task = new IgniteRunnableSendResponses(requests);
    return of(task);
  }

  private Optional<HttpResponse> getHttpResponse (Object... arguments) {
    return ofNullable(arguments)
        .filter(it -> it.length > 0)
        .map(it -> it[0])
        .filter(it -> it instanceof HttpResponse)
        .map(it -> (HttpResponse) it);
  }
}
