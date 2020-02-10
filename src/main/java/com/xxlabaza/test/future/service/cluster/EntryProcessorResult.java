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

import java.io.Serializable;

import lombok.NonNull;
import lombok.Value;
import org.apache.ignite.lang.IgniteRunnable;

@Value
class EntryProcessorResult implements Serializable {

  private static final long serialVersionUID = 718810330728397809L;

  static EntryProcessorResult of (@NonNull IgniteRunnable task) {
    return new EntryProcessorResult(task);
  }

  static EntryProcessorResult empty () {
    return new EntryProcessorResult(null);
  }

  IgniteRunnable task;

  boolean isEmpty () {
    return task == null;
  }
}
