package id.walt.ssikitexamples

import com.beust.klaxon.Klaxon
import id.walt.auditor.Auditor
import id.walt.auditor.JsonSchemaPolicy
import id.walt.auditor.SignaturePolicy
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.services.vc.JsonLdCredentialService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.vclib.vclist.VerifiableAttestation

fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Define used services
    val signatory = Signatory.getService()
    val credentialService = JsonLdCredentialService.getService()
    val keyService = KeyService.getService()


    /* Use services... */
    // generate key pairs for holder, issuer
    val holderKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)
    val issuerKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

    // create dids, using did:key
    val holderDid = DidService.create(DidMethod.key, holderKey.id)
    val issuerDid = DidService.create(DidMethod.key, issuerKey.id)

    // issue verifiable credential

    // List registered VC templates
    signatory.listTemplates().forEachIndexed { index, templateName ->
        println("$index: $templateName")
    }

    // Prepare VC template
    val vcTemplate = VerifiableAttestation(
        listOf("https://www.w3.org/2018/credentials/v1"),
        "VerifiableAttestation",
        issuerDid,
        credentialSubject = VerifiableAttestation.CredentialSubject(holderDid)
    )
    val signedVC = credentialService.sign(Klaxon().toJsonString(vcTemplate), ProofConfig(issuerDid = issuerDid))

    // verify credential
    val verificationResult = Auditor.verify(signedVC, listOf(SignaturePolicy(), JsonSchemaPolicy()))
    println("VC verified: ${verificationResult.overallStatus}")
    verificationResult.policyResults.forEach { (policy, result) -> println("${policy}: $result") }

    // present credential
    val vp = credentialService.present(
        listOf(signedVC),
        holderDid,
        "https://api.preprod.ebsi.eu",
        "d04442d3-661f-411e-a80f-42f19f594c9d"
    )

    // verify presentation
    val verificationResultVp = Auditor.verify(vp, listOf(SignaturePolicy(), JsonSchemaPolicy()))
    println("VP verified: ${verificationResultVp.overallStatus}")
    verificationResultVp.policyResults.forEach { (policy, result) -> println("${policy}: $result") }
}
