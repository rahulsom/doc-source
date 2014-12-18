package com.github.rahulsom.docsource

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.CompileStatic

/**
 * Created by rahul on 12/8/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CompileStatic
class Request {
  class Patient {
    String firstName
    String lastName
    String gender
    Date   dob
    String race
    String photo

    static class Address {
      String street
      String city
      String state
      String country
      String zipCode

      @Override
      String toString() {
        "${street}^^${city}^${state}^${zipCode}^${country}"
      }
    }
    Address address
  }

  Patient patient = new Patient()

  class Author {
    String name
    String institution
    String specialty
    String role
  }
  Author author = new Author()

  String repositoryUrl

  static class Identifier {
    String root      = '1.2.3.4'
    String extension = '123'
    String name      = 'Good Health Clinic'

    @Override
    String toString() {
      "${extension}^^^&${root}&ISO"
    }
  }
  Identifier identifier = new Identifier()


  @JsonIgnoreProperties(ignoreUnknown = true)
  static class VitalSign {
    String text
    String code
    String value
    String unit
  }
  List<VitalSign> vitalSigns

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class LabResult {
    String text
    String code
    String value
    String unit
  }
  List<LabResult> labResults


  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Medication {
    String text
    String code
  }
  List<Medication> medications

}
