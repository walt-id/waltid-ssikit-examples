package id.walt.ssikitexamples

import com.beust.klaxon.Klaxon
import id.walt.crypto.KeyAlgorithm
import id.walt.custodian.CustodianService
import id.walt.model.DidMethod
import id.walt.model.VerificationMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.services.vc.VCService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.vclib.model.Proof
import id.walt.vclib.vclist.VerifiableAttestation

// Define used services
val signatory = Signatory.getService()
val credentialService = VCService.getService()
val keyService = KeyService.getService()

fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

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

    // Get default VC template from VCService
    println("Default VC template: ${credentialService.defaultVcTemplate()}")

    var vcTemplate = VerifiableAttestation(listOf("https://www.w3.org/2018/credentials/v1"), "VerifiableAttestation", issuerDid, credentialSubject = VerifiableAttestation.CredentialSubject(holderDid))
    val signedVC = credentialService.sign(issuerDid, Klaxon().toJsonString(vcTemplate))

    // verify credential
    println("VC verified: ${credentialService.verifyVc(issuerDid, signedVC)}")
}
