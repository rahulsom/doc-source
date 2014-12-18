package com.github.rahulsom.docsource.cda

import com.github.rahulsom.cda.*
import com.github.rahulsom.docsource.Request
import com.github.rahulsom.docsource.util.Pair

import static com.github.rahulsom.docsource.cda.CdaHelper.*

/**
 * Created by rahul on 12/8/14.
 */
class CdaBuilder {

  String TimestampFormat = "yyyyMMddHHmmssZ"
  def    currentTime     = new Date()

  POCDMT000040ClinicalDocument addHeader(POCDMT000040ClinicalDocument clinicalDocument) {
    clinicalDocument.
        withRealmCode(new CS().withCode('US')).
        withTypeId(new POCDMT000040InfrastructureRootTypeId().
            withRoot('2.16.840.1.113883.1.3').
            withExtension('POCD_HD000040')
        ).
        withTemplateId(
            new II().withRoot('2.16.840.1.113883.3.27.1776').withAssigningAuthorityName('CDA/R2'),
            new II().withRoot('2.16.840.1.113883.10.20.3').withAssigningAuthorityName('HL7/CDT Header'),
            new II().withRoot('1.3.6.1.4.1.19376.1.5.3.1.1.1').withAssigningAuthorityName('IHE/PCC'),
            new II().withRoot('2.16.840.1.113883.3.88.11.32.1').withAssigningAuthorityName('HITSP/C32')
        ).
        withId(new II().withRoot(UUID.randomUUID().toString())).
        withCode(new CE().
            withCode('34133-9').
            withDisplayName('Summarization of episode note').
            withCodeSystem('2.16.840.1.113883.6.1').withCodeSystemName('LOINC')
        ).
        withTitle(new ST().withContent('Continuity of Care Document')).
        withEffectiveTime(new TS().withValue(currentTime.format(TimestampFormat))).
        withConfidentialityCode(new CE().
            withCode('N').
            withDisplayName('Normal').
            withCodeSystem('2.16.840.1.113883.5.25')
        ).
        withLanguageCode(new CS().withCode('en-US'))
  }

  POCDMT000040ClinicalDocument buildCda(Request request) {

    new POCDMT000040ClinicalDocument().
        with { addHeader(it) }.
        withRecordTarget(buildRecordTarget(request)).
        withAuthor(buildAuthor(request)).
        withComponent(new POCDMT000040Component2().
            withStructuredBody(
                new POCDMT000040StructuredBody().
                    withComponent(
                        buildVitalSigns(request.vitalSigns),
                        buildLabResults(request.labResults),
                        /* TODO More Sections*/
                    )
            )
        )
  }

  private POCDMT000040Author buildAuthor(Request request) {
    new POCDMT000040Author().
        withTypeCode('AUT').
        withContextControlCode('OP').
        withTime(ts(currentTime.format(TimestampFormat))).
        withAssignedAuthor(
            new POCDMT000040AssignedAuthor().
                withId(ii(request.identifier.root)).
                withRepresentedOrganization(new POCDMT000040Organization().
                    withId(ii(request.identifier.root)).
                    withName(new ON().withContent(request.identifier.name))
                )

        )
  }

  private POCDMT000040RecordTarget buildRecordTarget(Request request) {
    def dob = request.patient.dob.format('yyyyMMdd')
    new POCDMT000040RecordTarget().withPatientRole(
        new POCDMT000040PatientRole().
            withId(
                ii(request.identifier.root, request.identifier.extension)
            ).
            withPatient(
                new POCDMT000040Patient().
                    withName(En.from(request.patient.firstName, request.patient.lastName)).
                    withAdministrativeGenderCode(new CE().withCode(request.patient.gender)).
                    withBirthTime(ts(dob))
            ).
            withAddr(
                Ad.from(
                    request.patient.address.street,
                    request.patient.address.city,
                    request.patient.address.state,
                    request.patient.address.country,
                    request.patient.address.zipCode,
                )
            )
    )
  }

  private POCDMT000040Component3 buildVitalSigns(List<Request.VitalSign> vitalSigns) {
    def vitalSignElements = vitalSigns.collect { vs -> buildVitalSign(vs) }
    def je = new StrucDocTable().
        withThead(new StrucDocThead().
            withTr(new StrucDocTr().
                withThOrTd(
                    new StrucDocTh().withContent('VitalSign'),
                    new StrucDocTh().withContent('Date'),
                    new StrucDocTh().withContent('Value'),
                    new StrucDocTh().withContent('Unit'),
                )
            )
        ).
        withTbody(new StrucDocTbody().
            withTr(vitalSignElements*.right)
        )

    new POCDMT000040Component3().
        withSection(
            new POCDMT000040Section().
                withTemplateId(ii('2.16.840.1.113883.10.20.1.16')).
                withCode(new CE().withCode('8716-3').withCodeSystem('2.16.840.1.113883.6.1')).
                withTitle(st('Vital Signs')).
                withText(new StrucDocText().withContent(jaxb('table', StrucDocTable, je))).
                withEntry(new POCDMT000040Entry().
                    withTypeCode(XActRelationshipEntry.DRIV).
                    withOrganizer(new POCDMT000040Organizer().
                        withClassCode(XActClassDocumentEntryOrganizer.CLUSTER).
                        withMoodCode('EVN').
                        withTemplateId(ii('2.16.840.1.113883.10.20.1.35')).
                        withId(ii('c6f88320-67ad-11db-bd13-0800200c9a66')).
                        withCode(cd('46680005', '2.16.840.1.113883.6.96')).
                        withStatusCode(cs('completed')).
                        withEffectiveTime(
                            new IVLTS().withValue(currentTime.format(TimestampFormat))
                        ).
                        withComponent(vitalSignElements*.left)

                    )
                )
        )
  }


  private Pair<POCDMT000040Component4, StrucDocTr> buildVitalSign(Request.VitalSign vs) {
    new Pair<POCDMT000040Component4, StrucDocTr>(
        new POCDMT000040Component4().
            withObservation(new POCDMT000040Observation().
                withClassCode('OBS').
                withMoodCode(XActMoodDocumentObservation.EVN).
                withTemplateId(ii('2.16.840.1.113883.10.20.1.31')).
                withId(ii('c6f88322-67ad-11db-bd13-0800200c9a67')).
                withCode(cd(vs.code, '2.16.840.1.113883.6.1').
                    withDisplayName(vs.text)
                ).
                withStatusCode(cs('completed')).
                withEffectiveTime(new IVLTS().
                    withValue(currentTime.format(TimestampFormat))
                ).
                withValue(new PQ().
                    withValue(vs.value).
                    withUnit(vs.unit)
                )
            ),
        new StrucDocTr().
            withThOrTd(
                new StrucDocTd().withContent(vs.text),
                new StrucDocTd().withContent(currentTime.format('yyyy-MM-dd HH:mm z')),
                new StrucDocTd().withContent(vs.value),
                new StrucDocTd().withContent(vs.unit),
            )
    )

  }

  private POCDMT000040Component3 buildLabResults(List<Request.LabResult> labResults) {
    def labResultElements = labResults.collect { buildLabResult(it) }
    def je = new StrucDocTable().
        withThead(new StrucDocThead().
            withTr(new StrucDocTr().
                withThOrTd(
                    new StrucDocTh().withContent('VitalSign'),
                    new StrucDocTh().withContent('Date'),
                    new StrucDocTh().withContent('Value'),
                    new StrucDocTh().withContent('Unit'),
                )
            )
        ).
        withTbody(new StrucDocTbody().
            withTr(labResultElements*.right)
        )

    new POCDMT000040Component3().
        withSection(
            new POCDMT000040Section().
                withTemplateId(ii('2.16.840.1.113883.10.20.1.16')).
                withCode(new CE().withCode('30954-2').withCodeSystem('2.16.840.1.113883.6.1')).
                withTitle(st('Lab Results')).
                withText(new StrucDocText().withContent(jaxb('table', StrucDocTable, je))).
                withEntry(new POCDMT000040Entry().
                    withTypeCode(XActRelationshipEntry.DRIV).
                    withOrganizer(new POCDMT000040Organizer().
                        withClassCode(XActClassDocumentEntryOrganizer.CLUSTER).
                        withMoodCode('EVN').
                        withTemplateId(ii('2.16.840.1.113883.10.20.1.35')).
                        withId(ii('c6f88320-67ad-11db-bd13-0800200c9a66')).
                        withCode(cd('46680005', '2.16.840.1.113883.6.96')).
                        withStatusCode(cs('completed')).
                        withEffectiveTime(
                            new IVLTS().withValue(currentTime.format(TimestampFormat))
                        ).
                        withComponent(labResultElements*.left)

                    )
                )
        )
  }


  private Pair<POCDMT000040Component4, StrucDocTr> buildLabResult(Request.LabResult labResult) {
    new Pair(
        new POCDMT000040Component4().
            withObservation(new POCDMT000040Observation().
                withClassCode('OBS').
                withMoodCode(XActMoodDocumentObservation.EVN).
                withTemplateId(ii('2.16.840.1.113883.10.20.1.31')).
                withId(ii('c6f88322-67ad-11db-bd13-0800200c9a67')).
                withCode(cd(labResult.code, '2.16.840.1.113883.6.1').
                    withDisplayName(labResult.text)
                ).
                withStatusCode(cs('completed')).
                withEffectiveTime(new IVLTS().
                    withValue(currentTime.format(TimestampFormat))
                ).
                withValue(new PQ().
                    withValue(labResult.value).
                    withUnit(labResult.unit)
                )
            ),
        new StrucDocTr().
            withThOrTd(
                new StrucDocTd().withContent(labResult.text),
                new StrucDocTd().withContent(currentTime.format('yyyy-MM-dd HH:mm z')),
                new StrucDocTd().withContent(labResult.value),
                new StrucDocTd().withContent(labResult.unit),
            )

    )

  }
}
