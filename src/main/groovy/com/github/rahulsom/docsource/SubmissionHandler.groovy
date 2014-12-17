package com.github.rahulsom.docsource

import com.github.rahulsom.cda.POCDMT000040ClinicalDocument
import com.github.rahulsom.docsource.cda.CdaBuilder
import com.github.rahulsom.docsource.cda.CdaHelper
import com.github.rahulsom.docsource.ihe.PnrBuilder
import ihe.iti.xds_b._2007.DocumentRepositoryService
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType
import ratpack.handling.Context
import ratpack.handling.Handler

import javax.xml.bind.JAXBContext
import javax.xml.ws.BindingProvider
import javax.xml.ws.RespectBindingFeature
import javax.xml.ws.Response
import javax.xml.ws.Service
import javax.xml.ws.soap.AddressingFeature

import static ratpack.jackson.Jackson.json

/**
 * Created by rahul on 11/19/14.
 */
class SubmissionHandler implements Handler {

  public static final String RegistryResponse_Success = 'urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success'
  def jaxbContext = JAXBContext.newInstance(POCDMT000040ClinicalDocument)

  @Override
  void handle(Context context) throws Exception {
    context.with {
      Request request = parse(Request)

      def ccd = new CdaBuilder().buildCda(request)

      def ccdJaxb = CdaHelper.jaxb('ClinicalDocument', POCDMT000040ClinicalDocument, ccd)
      def ccdOut = new ByteArrayOutputStream()
      def marshaller = jaxbContext.createMarshaller()
      marshaller.marshal(ccdJaxb, ccdOut)

      def wsdl = this.class.getResource('/iti/wsdl/XDS.b_DocumentRepository.wsdl')
      Service service = new DocumentRepositoryService(wsdl)
      def port = service.getDocumentRepositoryPortSoap12(
          new AddressingFeature(true, true),
          // new MTOMFeature(true),
          new RespectBindingFeature(true)
      )
      BindingProvider bp = port as BindingProvider

      bp.requestContext[BindingProvider.ENDPOINT_ADDRESS_PROPERTY] = request.repositoryUrl

      def pnrRequest = new PnrBuilder().buildPnr(request, ccdOut.toByteArray())

      context.promise { promise ->
        println "Sending pnrRequest..."
        port.documentRepositoryProvideAndRegisterDocumentSetBAsync(pnrRequest) { response ->
          promise.success(response.get())
        }
        println "Done sending pnrRequest"
      }.then { RegistryResponseType registryResponse ->
        println "Received pnrResponse"
        def retval = registryResponse.status == RegistryResponse_Success ?
            [status: 'Success'] :
            [status: 'Error', message: registryResponse.registryErrorList.registryError.first().codeContext]
        render json(retval)

      }
    }
  }

}
