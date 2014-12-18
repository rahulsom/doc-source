package com.github.rahulsom.docsource.util

import groovy.transform.Immutable

/**
 * Created by rahul on 12/17/14.
 */
class Pair<L,R> {
  final L left
  final R right

  Pair(L left, R right) {
    this.left = left
    this.right = right
  }

}
