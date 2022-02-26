package id.walt.ssikitexamples

import id.walt.common.prettyPrint
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import id.walt.vclib.templates.VcTemplateManager

fun main() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Define used services
    val signatory = Signatory.getService()
    val keyService = KeyService.getService()

    /* Use services... */
    // generate key pairs for holder, issuer
    val holderKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)
    val issuerKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

    // create dids, using did:key
    val holderDid = DidService.create(DidMethod.key, holderKey.id)
    val issuerDid = DidService.create(DidMethod.key, issuerKey.id)

    // List registered VC templates
    signatory.listTemplates().forEachIndexed { index, templateName ->
        println("$index: $templateName")
    }

    // Create VC template
    val defaultVerifiableDiploma = VcTemplateManager.loadTemplate("VerifiableDiploma")
    println("Default Verifiable Diploma - " + defaultVerifiableDiploma.prettyPrint())

    // Prepare desired custom data that should replace the default template data
    val data = mapOf(
        credentialSubjectEntry(
            Pair("givenNames", "Yves"),
            Pair("familyName", "SMITH"),
            Pair("dateOfBirth", "2000-02-04")
        )
    )

    // Custom VC template
    val customVerifiableDiploma = MergingDataProvider(data).populate(
        defaultVerifiableDiploma,
        ProofConfig(subjectDid = holderDid, issuerDid = issuerDid, proofType = ProofType.LD_PROOF)
    )
    println("Custom Verifiable Diploma - " + customVerifiableDiploma.prettyPrint())

}

fun credentialSubjectEntry(vararg pairs: Pair<String, String>): Pair<String, Map<String, String>> {
    return Pair("credentialSubject", pairs.toMap())
}