package com.github.rahulsom.docsource

import groovy.transform.Immutable

import javax.activation.DataSource

/**
 * Created by rahul on 12/15/14.
 */
@Immutable
class MimeDataSource implements DataSource {

  String name
  String contentType
  byte[] byteArray

  @Override
  InputStream getInputStream() throws IOException {
    new ByteArrayInputStream(byteArray)
  }

  @Override
  OutputStream getOutputStream() throws IOException {
    null
  }
}
