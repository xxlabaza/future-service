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

import java.util.Map;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@SpringBootApplication
@SuppressWarnings("checkstyle:DesignForExtension")
public class Main {

  public static void main (String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter () {
    return new ProtobufHttpMessageConverter();
  }

  @Bean
  AsyncHttpClient asyncHttpClient () {
    return Dsl.asyncHttpClient();
  }

  @Bean
  DefaultErrorAttributes errorAttributes () {
    return new NoErrorAttributes();
  }

  private static class NoErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes (WebRequest webRequest, boolean includeStackTrace) {
      return null;
    }
  }
}
