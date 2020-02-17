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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.xxlabaza.test.future.service.Action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.asynchttpclient.Response;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
class ResponseContainer implements Serializable {

  private static final long serialVersionUID = 1107509815160689273L;

  List<Recipient> recipients;

  @Getter
  HttpResponse response;

  @SuppressWarnings("PMD.NullAssignment")
  List<Recipient> complete (@NonNull HttpResponse result) {
    response = result;
    val temp = recipients;
    recipients = null;
    return temp;
  }

  void addRecipient (@NonNull Recipient recipient) {
    if (recipients == null) {
      recipients = new ArrayList<>();
    }
    recipients.add(recipient);
  }

  boolean hasResponse () {
    return response != null;
  }

  @Value
  static class Recipient implements Serializable {

    private static final long serialVersionUID = -8070048633834849617L;

    URI uri;

    String method;

    Map<String, List<String>> headers;

    boolean includeResponseHeaders;

    Recipient (@NonNull Action.SendTo sendTo) {
      uri = sendTo.getUri();
      method = sendTo.getMethod().toString();
      headers = sendTo.getHeaders().orElse(null);
      includeResponseHeaders = sendTo.isIncludeResponseHeaders();
    }
  }

  @Value
  @ToString(exclude = "body")
  static class HttpResponse implements Serializable {

    private static final long serialVersionUID = -5929834528614671958L;

    byte[] body;

    Map<String, List<String>> headers;

    HttpResponse (@NonNull Response response) {
      body = response.getResponseBodyAsBytes();
      headers = response.getHeaders()
          .entries()
          .stream()
          .collect(groupingBy(Entry::getKey,
                              mapping(Entry::getValue, toList())));
    }
  }
}
