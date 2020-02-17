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

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.PRAGMA;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.security.MessageDigest;

import io.appulse.utils.HexUtil;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proto")
@SuppressWarnings("PMD.UnusedPrivateMethod")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ProtoController {

  private static final String PROTO_FILE_NAME = "messages.proto";

  private static final String PROTO_FILE_HASH_HEADER_NAME = "X-Proto-File-SHA1";

  ResponseEntity<Resource> getResponse;

  ResponseEntity<Void> headResponse;

  @Autowired
  ProtoController (ResourceLoader loader) {
    val resource = loader.getResource("classpath:" + PROTO_FILE_NAME);
    val sha1 = evaluateSha1(resource);

    getResponse = createGetResponse(resource, sha1);
    headResponse = createHeadResponse(sha1);
  }

  @GetMapping
  ResponseEntity<Resource> get () {
    return getResponse;
  }

  @RequestMapping(method = HEAD)
  ResponseEntity<Void> head () {
    return headResponse;
  }

  @SneakyThrows
  private ResponseEntity<Resource> createGetResponse (Resource resource, String sha1) {
    val headers = new HttpHeaders();
    headers.add(PROTO_FILE_HASH_HEADER_NAME, sha1);
    headers.add(CONTENT_TYPE, TEXT_PLAIN_VALUE);
    headers.add(CONTENT_LENGTH, Long.toString(resource.contentLength()));
    headers.add(CONTENT_DISPOSITION, "attachment; filename=" + PROTO_FILE_NAME);
    headers.add(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
    headers.add(PRAGMA, "no-cache");
    headers.add(EXPIRES, "0");

    return ResponseEntity.ok()
        .headers(headers)
        .body(resource);
  }

  private ResponseEntity<Void> createHeadResponse (String sha1) {
    val headers = new HttpHeaders();
    headers.add(PROTO_FILE_HASH_HEADER_NAME, sha1);

    return ResponseEntity.noContent()
        .headers(headers)
        .build();
  }

  @SneakyThrows
  private String evaluateSha1 (Resource resource) {
    val digest = MessageDigest.getInstance("SHA-1");
    try (val inputStream = resource.getInputStream()) {
      val buffer = new byte[8192];
      int readed = inputStream.read(buffer);
      while (readed != -1) {
        digest.update(buffer, 0, readed);
        readed = inputStream.read(buffer);
      }
      val  bytes = digest.digest();
      return HexUtil.toHexString(bytes);
    }
  }
}
