/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.commons.api.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.api.serializer.BatchSerializerException;
import org.apache.olingo.server.api.serializer.BatchSerializerException.MessageKeys;
import org.apache.olingo.server.core.deserializer.batch.BatchParserCommon;

public class BatchResponseSerializer {
  private static final int BUFFER_SIZE = 4096;
  private static final String DOUBLE_DASH = "--";
  private static final String COLON = ":";
  private static final String SP = " ";
  private static final String CRLF = "\r\n";

  public InputStream serialize(final List<ODataResponsePart> responses, final String boundary)
      throws BatchSerializerException {
    BodyBuilder builder = createBody(responses, boundary);

    return new ByteArrayInputStream(builder.getContent());
  }

  private BodyBuilder createBody(final List<ODataResponsePart> batchResponses, final String boundary)
      throws BatchSerializerException {
    final BodyBuilder builder = new BodyBuilder();

    for (final ODataResponsePart part : batchResponses) {
      builder.append(getDashBoundary(boundary));

      if (part.isChangeSet()) {
        appendChangeSet(part, builder);
      } else {
        appendBodyPart(part.getResponses().get(0), builder, false);
      }
    }
    builder.append(getCloseDelimiter(boundary));

    return builder;
  }

  private void appendChangeSet(final ODataResponsePart part, final BodyBuilder builder)
      throws BatchSerializerException {
    final String changeSetBoundary = generateBoundary("changeset");

    appendChangeSetHeader(builder, changeSetBoundary);
    builder.append(CRLF);

    for (final ODataResponse response : part.getResponses()) {
      builder.append(getDashBoundary(changeSetBoundary));
      appendBodyPart(response, builder, true);
    }

    builder.append(getCloseDelimiter(changeSetBoundary));
  }

  private void appendBodyPart(final ODataResponse response, final BodyBuilder builder, final boolean isChangeSet)
      throws BatchSerializerException {

    appendBodyPartHeader(response, builder, isChangeSet);
    builder.append(CRLF);

    appendStatusLine(response, builder);
    Body body = new Body(response);
    appendResponseHeader(response, body.getLength(), builder);
    builder.append(CRLF);

    builder.append(body);
    builder.append(CRLF);
  }

  private void appendChangeSetHeader(final BodyBuilder builder, final String changeSetBoundary) {
    appendHeader(HttpHeader.CONTENT_TYPE, HttpContentType.MULTIPART_MIXED + "; boundary="
        + changeSetBoundary, builder);
  }

  private void appendHeader(final String name, final String value, final BodyBuilder builder) {
    builder.append(name)
        .append(COLON)
        .append(SP)
        .append(value)
        .append(CRLF);
  }

  private void appendStatusLine(final ODataResponse response, final BodyBuilder builder) {
    builder.append("HTTP/1.1")
        .append(SP)
        .append(response.getStatusCode())
        .append(SP)
        .append(HttpStatusCode.fromStatusCode(response.getStatusCode()).toString())
        .append(CRLF);
  }

  private void appendResponseHeader(final ODataResponse response, final int contentLength,
      final BodyBuilder builder) {
    final Map<String, String> header = response.getHeaders();

    for (final String key : header.keySet()) {
      // Requests do never has a content id header
      if (!key.equalsIgnoreCase(HttpHeader.CONTENT_ID)) {
        appendHeader(key, header.get(key), builder);
      }
    }

    appendHeader(HttpHeader.CONTENT_LENGTH, "" + contentLength, builder);
  }

  private void appendBodyPartHeader(final ODataResponse response, final BodyBuilder builder,
      final boolean isChangeSet) throws BatchSerializerException {
    appendHeader(HttpHeader.CONTENT_TYPE, HttpContentType.APPLICATION_HTTP, builder);
    appendHeader(BatchParserCommon.CONTENT_TRANSFER_ENCODING, BatchParserCommon.BINARY_ENCODING, builder);

    if (isChangeSet) {
      if (response.getHeaders().get(HttpHeader.CONTENT_ID) != null) {
        appendHeader(HttpHeader.CONTENT_ID, response.getHeaders().get(HttpHeader.CONTENT_ID), builder);
      } else {
        throw new BatchSerializerException("Missing content id", MessageKeys.MISSING_CONTENT_ID);
      }
    }
  }

  private String getDashBoundary(final String boundary) {
    return DOUBLE_DASH + boundary + CRLF;
  }

  private String getCloseDelimiter(final String boundary) {
    return DOUBLE_DASH + boundary + DOUBLE_DASH + CRLF;
  }

  private String generateBoundary(final String value) {
    return value + "_" + UUID.randomUUID().toString();
  }

  /**
   * Builder class to create the body and the header.
   */
  private class BodyBuilder {
    private final Charset CHARSET_ISO_8859_1 = Charset.forName("iso-8859-1");
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private boolean isClosed = false;

    public byte[] getContent() {
      isClosed = true;
      byte[] tmp = new byte[buffer.position()];
      buffer.flip();
      buffer.get(tmp, 0, buffer.limit());
      return tmp;
    }

    public BodyBuilder append(String string) {
      byte [] b = string.getBytes(CHARSET_ISO_8859_1);
      put(b);
      return this;
    }

    private void put(byte[] b) {
      if(isClosed) {
        throw new RuntimeException("BodyBuilder is closed.");
      }
      if(buffer.remaining() < b.length) {
        buffer.flip();
        ByteBuffer tmp = ByteBuffer.allocate(buffer.limit() *2);
        tmp.put(buffer);
        buffer = tmp;
      }
      buffer.put(b);
    }

    public BodyBuilder append(int statusCode) {
      return append(String.valueOf(statusCode));
    }

    public BodyBuilder append(Body body) {
      put(body.getContent());
      return this;
    }

    public String toString() {
      return new String(buffer.array(), 0, buffer.position());
    }
  }

  /**
   * Body part which is read and stored as bytes (no charset conversion).
   */
  private class Body {
    private final byte[] content;

    public Body(ODataResponse response) {
      this.content = getBody(response);
    }

    public int getLength() {
      return content.length;
    }

    public byte[] getContent() {
      return content;
    }

    private byte[] getBody(final ODataResponse response) {
      if (response == null || response.getContent() == null) {
        return new byte[0];
      }

      try {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer inBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ReadableByteChannel ic = Channels.newChannel(response.getContent());
        WritableByteChannel oc = Channels.newChannel(output);
        while (ic.read(inBuffer) > 0) {
          inBuffer.flip();
          oc.write(inBuffer);
          inBuffer.rewind();
        }
        return output.toByteArray();
      } catch (IOException e) {
        throw new ODataRuntimeException("Error on reading request content");
      }
    }
  }
}