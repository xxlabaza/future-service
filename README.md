# Overview

This is a simple service for playing in the AWS ecosystem.

The service provides functionality for submitting an HTTP request to a remote service, which invokes once (in spite of the submits maybe several times), its response caches, and resend to a specified destination(s) (optional).

<dl>
  <dt>POST /</dt>
  <dd>
    Submits a new HTTP request. JSON payload explanation:
    <table>
      <thead>
        <th>key</th><th>description</th><th>default</th><th>&nbsp;</th>
      </thead>
      <tr>
        <td><b>reqeust</b></td>
        <td>an object, which describes an HTTP request</td>
        <td>-</td>
        <td>required</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;url</td>
        <td>request's URL</td>
        <td>-</td>
        <td>required</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;method</td>
        <td>HTTP method</td>
        <td>GET</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;headers</td>
        <td>the headers of the reqeust</td>
        <td>-</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;body</td>
        <td>request's payload</td>
        <td>-</td>
        <td>optional</td>
      </tr>
      <tr>
        <td><b>sendTo</b></td>
        <td>an object describes where to send request's response</td>
        <td>-</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;url</td>
        <td>URL where send the response</td>
        <td>-</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;method</td>
        <td>HTTP method</td>
        <td>GET</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;headers</td>
        <td>the headers of the reqeust</td>
        <td>-</td>
        <td>optional</td>
      </tr>
      <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;includeResponseHeaders</td>
        <td>a flag, which indecates - <code>true</code> if original response headers should be copied, <code>false</code> otherwise</td>
        <td>true</td>
        <td>optional</td>
      </tr>
    </table>
    A request body example:
    <pre>
{
  "request": {
    "url": "http://localhost:8989",
    "method": "delete",
    "headers": {
      "Key1": "Value1",
      "Key2": ["Value2", "Value3"]
    },
    "body": "Hello world"
  },
  "sendTo": {
    "url": "localhost:9999/api",
    "includeResponseHeaders": false
  }
}</pre>
  </dd>
</dl>

## Build docker image

```bash
$> ./mvnw clean package docker:build
```

## Package, verify and run

```bash
$> ./mvnw clean package verify; and java -jar target/future-service-1.1.0.jar
```
