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

import java.util.Arrays;
import java.util.Optional;
import javax.cache.processor.MutableEntry;

import com.xxlabaza.test.future.service.cluster.ResponseContainer.Recipient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.cache.CacheEntryProcessor;

@Slf4j
class EntryProcessorSubmitReqeust implements CacheEntryProcessor<HttpRequest, ResponseContainer, EntryProcessorResult> {

  private static final long serialVersionUID = 4605860000728397801L;

  @Override
  public EntryProcessorResult process (MutableEntry<HttpRequest, ResponseContainer> entry, Object... arguments) {
    val container = ofNullable(entry.getValue())
        .orElseGet(ResponseContainer::new);

    log.info("{}", container);

    if (container.hasResponse()) {
      return getRecipient(arguments)
          .map(it -> new HttpRequest(it, container.getResponse()))
          .map(Arrays::asList)
          .map(IgniteRunnableSendResponses::new)
          .map(EntryProcessorResult::of)
          .orElseGet(EntryProcessorResult::empty);
    }

    getRecipient(arguments)
        .ifPresent(container::addRecipient);

    val containerWasCreated = entry.getValue() == null;
    entry.setValue(container);

    if (containerWasCreated == false) {
      return empty();
    }

    val task = new IgniteRunnableExecuteRequest(entry.getKey());
    return of(task);
  }

  private Optional<Recipient> getRecipient (Object... arguments) {
    return ofNullable(arguments)
        .filter(it -> it.length > 0)
        .map(it -> it[0])
        .filter(it -> it instanceof Recipient)
        .map(it -> (Recipient) it);
  }
}
