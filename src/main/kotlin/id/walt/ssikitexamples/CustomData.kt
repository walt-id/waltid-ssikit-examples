package id.walt.ssikitexamples

import id.walt.common.prettyPrint
import id.walt.credentials.w3c.builder.W3CCredentialBuilder
import id.walt.credentials.w3c.templates.VcTemplateService
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider


fun main(){
    customData()
}

fun customData() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Define used services
    val signatory = Signatory.getService()
    val keyService = KeyService.getService()

    /* Use services... */
    // generate key pairs for holder, issuer
    val holderKey = keyService.generate(KeyAlgorithm.RSA)
    val issuerKey = keyService.generate(KeyAlgorithm.ECDSA_Secp256k1)

    // create dids, using did:key
    val holderDid = DidService.create(DidMethod.key, holderKey.id)
    val issuerDid = DidService.create(DidMethod.key, issuerKey.id)

    // List registered VC templates
    signatory.listTemplates().forEachIndexed { index, templateName ->
        println("$index: $templateName")
    }

    // Load a VC template
    val verifiableDiplomaTemplate = VcTemplateService.getService().getTemplate("VerifiableDiploma").template!!
    println("Default Verifiable Diploma - " + verifiableDiplomaTemplate.prettyPrint())

    // Prepare desired custom data that should replace the default template data
    val data = mapOf(
        credentialSubjectEntry(
            Pair("givenNames", "Yves"),
            Pair("familyName", "SMITH"),
            Pair("dateOfBirth", "2000-02-04")
        )
    )

    // Populate VC template with custom data
    val verifiableDiploma = MergingDataProvider(data).populate(
        W3CCredentialBuilder.fromPartial(verifiableDiplomaTemplate),
        ProofConfig(subjectDid = holderDid, issuerDid = issuerDid, proofType = ProofType.LD_PROOF)
    ).build()
    println("Verifiable Diploma with custom data - " + verifiableDiploma.prettyPrint())

}

fun credentialSubjectEntry(vararg pairs: Pair<String, String>): Pair<String, Map<String, String>> {
    return Pair("credentialSubject", pairs.toMap())
}

