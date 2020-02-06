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

import static com.xxlabaza.test.future.service.Action.Request;
import static com.xxlabaza.test.future.service.Action.SendTo;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.Spliterator.NONNULL;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.springframework.http.HttpMethod;

@SuppressWarnings({
    "PMD.UnusedPrivateMethod",
    "PMD.UseDiamondOperator"
})
class ActionDeserializer extends JsonDeserializer<Action> {

  @Override
  public Action deserialize (JsonParser parser, DeserializationContext context) throws IOException {
    val codec = parser.getCodec();
    val node = (JsonNode) codec.readTree(parser);
    return Action.builder()
        .request(parseRequest(node))
        .sendTo(parseSendTo(node))
        .build();
  }

  private Request parseRequest (JsonNode node) {
    val object = node.get("request");
    if (object == null || object.isObject() == false) {
      throw new ParseException("request object must be set as an object");
    }
    val builder = Request.builder()
        .url(parseUrl(object.get("url")));

    parseMethod(object.get("method"))
        .ifPresent(builder::method);

    val headers = parseHeaders(object.get("headers"));
    if (headers.isPresent()) {
      builder.headers(headers);
    }
    val body = parsePayload(object.get("body"));
    if (body.isPresent()) {
      builder.body(body);
    }

    return builder.build();
  }

  private Optional<SendTo> parseSendTo (JsonNode node) {
    val object = node.get("sendTo");
    if (object == null) {
      return empty();
    }
    if (object.isObject() == false) {
      throw new ParseException("response must be an object");
    }
    val builder = SendTo.builder()
        .url(parseUrl(object.get("url")));

    parseMethod(object.get("method"))
        .ifPresent(builder::method);

    val headers = parseHeaders(object.get("headers"));
    if (headers.isPresent()) {
      builder.headers(headers);
    }

    parseBoolean(object.get("includeResponseHeaders"))
        .ifPresent(builder::includeResponseHeaders);

    return of(builder.build());
  }

  private URI parseUrl (JsonNode node) {
    return ofNullable(node)
        .map(JsonNode::asText)
        .filter(not(String::isEmpty))
        .map(it -> it.startsWith("http://")
                   ? it
                   : "http://" + it
        )
        .map(it -> {
          try {
            return new URI(it);
          } catch (URISyntaxException ex) {
            throw new ParseException(ex);
          }
        })
        .filter(Objects::nonNull)
        .orElseThrow(() -> new ParseException("'url' field must be set"));
  }

  private Optional<HttpMethod> parseMethod (JsonNode node) {
    if (node == null) {
      return empty();
    }
    return of(node)
        .map(JsonNode::asText)
        .filter(not(String::isEmpty))
        .map(String::toUpperCase)
        .map(HttpMethod::valueOf);
  }

  private Optional<Map<String, List<String>>> parseHeaders (JsonNode node) {
    if (node == null) {
      return empty();
    }
    if (node.isObject() == false) {
      throw new ParseException("field 'headers' must be an object");
    }

    val result = new HashMap<String, List<String>>();

    val iterator = node.fields();
    while (iterator.hasNext()) {
      val entry = iterator.next();

      val key = entry.getKey();
      val value = entry.getValue();
      if (value.isArray()) {
        val spliterator = Spliterators.spliteratorUnknownSize(value.elements(), NONNULL);
        val values = StreamSupport.stream(spliterator, false)
            .map(JsonNode::asText)
            .collect(toList());

        result.put(key, values);
      } else {
        val values = asList(value.asText());
        result.put(key, values);
      }
    }
    return of(result);
  }

  private Optional<byte[]> parsePayload (JsonNode node) {
    return ofNullable(node)
        .map(JsonNode::asText)
        .filter(not(String::isEmpty))
        .map(String::getBytes);
  }

  private Optional<Boolean> parseBoolean (JsonNode node) {
    if (node == null) {
      return empty();
    }
    if (node.isBoolean() == false) {
      throw new ParseException("invalid boolean field");
    }
    return of(node.asBoolean());
  }

  static class ParseException extends RuntimeException {

    private static final long serialVersionUID = -5299345461591323209L;

    ParseException(String message) {
      super(message);
    }

    ParseException (Throwable throwable) {
      super(throwable);
    }
  }
}
