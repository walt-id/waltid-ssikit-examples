package id.walt.ssikitexamples

import id.walt.auditor.AuditorRestAPI
import id.walt.auditor.PolicyRegistry
import id.walt.servicematrix.ServiceMatrix
import id.walt.signatory.DataProviderRegistry
import id.walt.signatory.ProofConfig
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.rest.SignatoryRestAPI
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.registry.VcTypeRegistry
import java.util.*


fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Register custom data provider
    DataProviderRegistry.register(CustomCredential::class, CustomDataProvider())

    // Registering custom verification policy
    PolicyRegistry.register(MyCustomPolicy())

    // Registering a custom Credential Template
    VcTypeRegistry.register(CustomCredential.Companion, CustomCredential::class)

    // Starting REST Services
    val bindAddress = "127.0.0.1"
    SignatoryRestAPI.start(7001, bindAddress)
    AuditorRestAPI.start(7003, bindAddress)

    println(" walt.id Signatory API: http://${bindAddress}:7001")
    println(" walt.id Auditor API:   http://${bindAddress}:7003")


    //TODO: implement the following calls using the ssikit rest client


//    // Issue VC in JSON-LD and JWT format (for show-casing both formats)
//    val vcJson = signatory.issue("CustomCredentialId", ProofConfig(issuerDid = issuer, subjectDid = holder, proofType = ProofType.LD_PROOF))
//    val vcJwt = signatory.issue("VerifiableId", ProofConfig(issuerDid = issuer, subjectDid = holder, proofType = ProofType.JWT))
//
//    // Present VC in JSON-LD and JWT format (for show-casing both formats)
//    val vpJson = custodian.createPresentation(listOf(vcJson), holder)
//    val vpJwt = custodian.createPresentation(listOf(vcJwt), holder)
//
//    // Verify VPs, using Signature, JsonSchema and a custom policy
//    val resJson = AuditorService.verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
//    val resJwt = AuditorService.verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
//
//    println("JSON verification result: ${resJson.overallStatus}")
//    println("JWT verification result: ${resJwt.overallStatus}")
}

class CustomDataProvider : SignatoryDataProvider {
    override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        if (template is CustomCredential) {
            template.apply {
                id = "identity#verifiableID#${UUID.randomUUID()}"
                issuer = proofConfig.issuerDid
                credentialSubject?.apply {
                    givenName = "John"
                    birthDate = "1958-08-17"
                }
            }
            return template
        } else {
            throw IllegalArgumentException("Only VerifiableId is supported by this data provider")
        }
    }
}
