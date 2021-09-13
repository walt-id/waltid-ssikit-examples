package id.walt.ssikitexamples

import id.walt.auditor.AuditorService
import id.walt.auditor.JsonSchemaPolicy
import id.walt.auditor.SignaturePolicy
import id.walt.auditor.VerificationPolicy
import id.walt.crypto.KeyAlgorithm
import id.walt.custodian.CustodianService
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableDiploma

class MyCustomPolicy : VerificationPolicy {
    override val description: String
        get() = "A custom verification policy"

    override fun verify(vc: VerifiableCredential): Boolean {
        // TODO: implement custom verification method here
        return true
    }
}

val signatory = Signatory.getService()
val custodian = CustodianService.getService()

fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Create VCs to verify:

    // create dids, using did:key and newly generated keys
    val holderDid = DidService.create(DidMethod.key)
    val issuerDid = DidService.create(DidMethod.key)

    // issue VC in JSON-LD and JWT format (for show-casing both formats)
    val vcJson = signatory.issue(
        "VerifiableDiploma",
        ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.LD_PROOF)
    )
    val vcJwt = signatory.issue(
        "VerifiableDiploma",
        ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.JWT)
    )

    // present VC in JSON-LD and JWT format (for show-casing both formats)
    val vpJson = custodian.createPresentation(vcJson, null, null)
    val vpJwt = custodian.createPresentation(vcJwt, null, null)

    // verify VPs, using Signature, JsonSchema and a custom policy policies
    val resJson = AuditorService.verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
    val resJwt = AuditorService.verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))

    println("JSON verification result: ${resJson.overallStatus}")
    println("JWT verification result: ${resJwt.overallStatus}")
}