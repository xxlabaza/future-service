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

import com.xxlabaza.test.future.service.Action;
import com.xxlabaza.test.future.service.cluster.ResponseContainer.Recipient;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.lang.IgniteFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterActionService {

  @Autowired
  Ignite ignite;

  @Autowired
  IgniteCache<HttpRequest, ResponseContainer> requestsRepository;

  /**
   * Submits the specified action to the computation cluster.
   *
   * @param action the action for execution.
   */
  public void submit (@NonNull Action action) {
    log.info("{}", action);

    val key = new HttpRequest(action);
    val recipient = action.getSendTo()
        .map(Recipient::new)
        .orElse(null);

    requestsRepository
        .invokeAsync(key, new EntryProcessorSubmitReqeust(), recipient)
        .listen(it -> handle(it, action));
  }

  private void handle (IgniteFuture<EntryProcessorResult> future, Action action) {
    EntryProcessorResult result;
    try {
      result = future.get(5, SECONDS);
    } catch (IgniteException ex) {
      log.error("failed to submit {}", action, ex);
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
