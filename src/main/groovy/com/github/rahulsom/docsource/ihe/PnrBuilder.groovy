package com.github.rahulsom.docsource.ihe

import com.github.rahulsom.cda.POCDMT000040ClinicalDocument
import com.github.rahulsom.docsource.MimeDataSource
import com.github.rahulsom.docsource.Request
import groovy.time.TimeCategory
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.SubmitObjectsRequest
import oasis.names.tc.ebxml_regrep.xsd.rim._3.AssociationType1
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.InternationalStringType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.LocalizedStringType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryObjectListType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryPackageType
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType

import javax.activation.DataHandler
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import java.security.SecureRandom

/**
 * Created by rahul on 12/17/14.
 */
class PnrBuilder {
  public static final String AssociationType_HasMember                = 'HasMember'
  public static final String SlotName_SubmissionSetStatus             = 'SubmissionSetStatus'
  public static final String SubmissionSetStatus_Original             = 'Original'
  public static final String ISO8601Format                            = "yyyy-MM-dd'T'HH:mm:ssZ"
  public static final String SlotName_CodingScheme                    = 'codingScheme'
  public static final String IdentifierType_SubmissionSetUniqueId     = 'XDSSubmissionSet.uniqueId'
  public static final String IdentifierType_SubmissionSetSourceId     = 'XDSSubmissionSet.sourceId'
  public static final String IdentifierType_SubmissionSetPatientId    = 'XDSSubmissionSet.patientId'
  public static final String IdentifierType_DocumentPatientId         = 'XDSDocumentEntry.patientId'
  public static final String IdentifierType_DocumentUniqueId          = 'XDSDocumentEntry.uniqueId'
  public static final String ClassificationScheme_ClassCode           = "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a"
  public static final String ClassificationScheme_ConfidentialityCode = "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f"
  public static final String ClassificationScheme_FormatCode          = "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d"
  public static final String ClassificationScheme_FacilityTypeCode    = "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1"
  public static final String ClassificationScheme_PracticeSettingCode = "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead"
  public static final String PhotoId                                  = 'PHOTO'
  public static final String DocumentId                               = 'DOCUMENT'
  public static final String SubmissionSetId                          = 'SubmissionSet'


  public static final uuidMap = [
      'XDSSubmissionSet.uniqueId' : 'urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8',
      'XDSSubmissionSet.sourceId' : 'urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832',
      'XDSSubmissionSet.patientId': 'urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446',

      'XDSDocumentEntry.patientId': 'urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427',
      'XDSDocumentEntry.uniqueId' : "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab"
  ]

  ProvideAndRegisterDocumentSetRequestType buildPnr(Request request, byte[] ccdOut){

    def photoRegex = 'data:(.*.);(.*),(.*)'
    def photoMatch = request.patient.photo =~ photoRegex

    String photoFormat = photoMatch[0][1]
    String photoEncoding = photoMatch[0][2]
    String photoData = photoMatch[0][3]
    def photoBinary = photoEncoding == 'base64' ? photoData.decodeBase64() : ''.bytes

    def currentTime = new Date()
    def serviceBegan = use(TimeCategory) {
      currentTime - 21.minutes
    }
    def serviceEnded = use(TimeCategory) {
      currentTime - 1.minute
    }

    new ProvideAndRegisterDocumentSetRequestType().
        withDocument(
            document(PhotoId, photoFormat, photoBinary),
            document(DocumentId, 'application/xml', ccdOut),
        ).
        withSubmitObjectsRequest(new SubmitObjectsRequest().
            withId(UUID.randomUUID().toString()).
            withRegistryObjectList(new RegistryObjectListType().
                withIdentifiable(
                    jaxb('ExtrinsicObject', ExtrinsicObjectType, new ExtrinsicObjectType().
                        withId(PhotoId).
                        withMimeType(photoFormat).
                        withObjectType('urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1').
                        withSlot(
                            slot('creationTime', currentTime.format(ISO8601Format)),
                            slot('serviceStartTime', serviceBegan.format(ISO8601Format)),
                            slot('serviceStopTime', serviceEnded.format(ISO8601Format)),
                            slot('sourcePatientId', request.identifier.toString()),
                            slot('sourcePatientInfo',
                                "PID-3|${request.identifier}",
                                "PID-5|${request.patient.lastName}^${request.patient.firstName}^^^",
                                "PID-7|${request.patient.dob.format('yyyyMMdd')}",
                                "PID-8|${request.patient.gender}",
                                "PID-11|${request.patient.address}",
                            ),
                        ).
                        withName(ist(PhotoId)).
                        withDescription(null).
                        withClassification(
                            new ClassificationType().
                                withId("PHOTOClassification01").
                                withClassificationScheme('urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d').
                                withClassifiedObject(PhotoId).
                                withSlot(
                                    slot('authorPerson', request.author.name),
                                    slot('authorInstitution', request.author.institution),
                                    slot('authorRole', request.author.role),
                                    slot('authorSpecialty', request.author.specialty),
                                ),
                            classify(PhotoId, 'Connect-a-thon classCodes',
                                ClassificationScheme_ClassCode, 'History and Physical',
                                'History and Physical'
                            ),
                            classify(PhotoId, 'Connect-a-thon confidentialityCodes',
                                ClassificationScheme_ConfidentialityCode, 'Clinical Workers Only',
                                "1.3.6.1.4.1.21367.2006.7.101"
                            ),
                            classify(PhotoId, 'Connect-a-thon formatCodes',
                                ClassificationScheme_FormatCode, photoFormat, photoFormat
                            ),
                            classify(PhotoId, 'Connect-a-thon healthcareFacilityTypeCodes',
                                ClassificationScheme_FacilityTypeCode, 'Outpatient', 'Outpatient'
                            ),
                            classify(PhotoId, 'Connect-a-thon practiceSettingCodes',
                                ClassificationScheme_PracticeSettingCode, 'General Medicine',
                                'General Medicine'
                            ),
                            classify(PhotoId, 'LOINC', "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                                'Outpatient Evaluation And Management', "34108-1"
                            ),
                        ).
                        withExternalIdentifier(
                            identifier(PhotoId, IdentifierType_DocumentPatientId, request.identifier.toString()),
                            identifier(PhotoId, IdentifierType_DocumentUniqueId,
                                "${request.identifier.root}.${identifierCounter++}"
                            )
                        )
                    ),
                    jaxb('ExtrinsicObject', ExtrinsicObjectType, new ExtrinsicObjectType().
                        withId(DocumentId).
                        withMimeType('application/xml').
                        withObjectType('urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1').
                        withSlot(
                            slot('languageCode', 'en-us'),
                            slot('creationTime', currentTime.format(ISO8601Format)),
                            slot('serviceStartTime', serviceBegan.format(ISO8601Format)),
                            slot('serviceStopTime', serviceEnded.format(ISO8601Format)),
                            slot('sourcePatientId', request.identifier.toString()),
                            slot('sourcePatientInfo',
                                "PID-3|${request.identifier}",
                                "PID-5|${request.patient.lastName}^${request.patient.firstName}^^^",
                                "PID-7|${request.patient.dob.format('yyyyMMdd')}",
                                "PID-8|${request.patient.gender}",
                                "PID-11|${request.patient.address}",
                            ),
                        ).
                        withName(ist(DocumentId)).
                        withDescription(null).
                        withClassification(
                            new ClassificationType().
                                withId("DOCUMENTClassification01").
                                withClassificationScheme('urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d').
                                withClassifiedObject(DocumentId).
                                withSlot(
                                    slot('authorPerson', request.author.name),
                                    slot('authorInstitution', request.author.institution),
                                    slot('authorRole', request.author.role),
                                    slot('authorSpecialty', request.author.specialty),
                                ),
                            classify(DocumentId, 'Connect-a-thon classCodes', ClassificationScheme_ClassCode,
                                'History and Physical', 'History and Physical'
                            ),
                            classify(DocumentId, 'Connect-a-thon confidentialityCodes',
                                ClassificationScheme_ConfidentialityCode, 'Clinical Workers Only',
                                "1.3.6.1.4.1.21367.2006.7.101"
                            ),
                            classify(DocumentId, 'Connect-a-thon formatCodes',
                                ClassificationScheme_FormatCode, 'CDAR2/IHE 1.0', 'CDAR2/IHE 1.0'
                            ),
                            classify(DocumentId, 'Connect-a-thon healthcareFacilityTypeCodes',
                                ClassificationScheme_FacilityTypeCode, 'Outpatient', 'Outpatient'
                            ),
                            classify(DocumentId, 'Connect-a-thon practiceSettingCodes',
                                ClassificationScheme_PracticeSettingCode, 'General Medicine',
                                'General Medicine'
                            ),
                            classify(DocumentId, 'LOINC', "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                                'Outpatient Evaluation And Management', "34108-1"
                            ),
                        ).
                        withExternalIdentifier(
                            identifier(DocumentId, IdentifierType_DocumentPatientId, request.identifier.toString()
                            ),
                            identifier(DocumentId, IdentifierType_DocumentUniqueId,
                                "${request.identifier.root}.${identifierCounter++}"
                            )
                        )
                    ),
                    jaxb('RegistryPackage', RegistryPackageType, new RegistryPackageType().
                        withId(SubmissionSetId).
                        withSlot(
                            slot('submissionTime', currentTime.format(ISO8601Format)),
                        ).
                        withName(ist('Physical')).
                        withDescription(ist('Annual Physical')).
                        withClassification(
                            new ClassificationType().
                                withId('SubmissionSet-Classification-08').
                                withClassificationScheme("urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d").
                                withClassifiedObject(SubmissionSetId).
                                withSlot(
                                    slot('authorPerson', request.author.name),
                                    slot('authorInstitution', request.author.institution),
                                    slot('authorRole', request.author.role),
                                    slot('authorSpecialty', request.author.specialty),
                                ),
                            classify(SubmissionSetId, 'Connect-a-thon contentTypeCodes',
                                "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500", "History and Physical",
                                "History and Physical"
                            )
                        ).
                        withExternalIdentifier(
                            identifier(SubmissionSetId, IdentifierType_SubmissionSetUniqueId,
                                "${request.identifier.root}.${identifierCounter++}"
                            ),
                            identifier(SubmissionSetId, IdentifierType_SubmissionSetSourceId,
                                "${identifierCounter++}"
                            ),
                            identifier(SubmissionSetId, IdentifierType_SubmissionSetPatientId,
                                request.identifier.toString()
                            )
                        )
                    ),
                    jaxb('Classification', ClassificationType, new ClassificationType().
                        withId('CL10').
                        withClassifiedObject(SubmissionSetId).
                        withClassificationNode('urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd')
                    ),
                    jaxb('Association', AssociationType1, associate(SubmissionSetId, PhotoId)),
                    jaxb('Association', AssociationType1, associate(SubmissionSetId, DocumentId))
                )
            )
        )
  }

  private ClassificationType classify(
      String object, String codingScheme, String classificationScheme, String name, String code) {
    new ClassificationType().
        withId("${object}-Classification-${identifierCounter++}").
        withClassificationScheme(classificationScheme).
        withClassifiedObject(object).
        withNodeRepresentation(code).
        withSlot(slot(SlotName_CodingScheme, codingScheme)).
        withName(ist(name))
  }

  long identifierCounter = Math.abs(new SecureRandom().nextLong())

  private ExternalIdentifierType identifier(String object, String scheme, String value) {
    new ExternalIdentifierType().
        withId("${object}-Identifier-${identifierCounter++}").
        withRegistryObject(object).
        withIdentificationScheme(uuidMap[scheme]).
        withValue(value).
        withName(ist(scheme))
  }

  private ProvideAndRegisterDocumentSetRequestType.Document document(String id, String mimeType, byte[] bytes) {
    new ProvideAndRegisterDocumentSetRequestType.Document().
        withId(id).
        withValue(new DataHandler(new MimeDataSource("${id}.${mimeType.split('/').last()}", mimeType, bytes)))
  }

  private AssociationType1 associate(String source, String destination) {
    new AssociationType1().
        withId("${source}-${destination}-Association-${identifierCounter++}").
        withAssociationType(AssociationType_HasMember).
        withSourceObject(source).
        withTargetObject(destination).
        withSlot(
            slot(SlotName_SubmissionSetStatus, SubmissionSetStatus_Original)
        )
  }

  private SlotType1 slot(String name, String... values) {
    new SlotType1().
        withName(name).
        withValueList(
            new ValueListType().withValue(values)
        )
  }

  private InternationalStringType ist(String value) {
    new InternationalStringType().
        withLocalizedString(
            new LocalizedStringType().
                withValue(value)
        )
  }

  private static <T> JAXBElement<T> jaxb(String tagname, Class<T> clazz, T value) {
    return new JAXBElement<T>(new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", tagname), clazz, value);
  }

}
