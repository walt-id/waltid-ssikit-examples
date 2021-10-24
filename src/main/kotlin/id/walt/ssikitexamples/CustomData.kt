package id.walt.ssikitexamples

import id.walt.auditor.Auditor
import id.walt.auditor.JsonSchemaPolicy
import id.walt.auditor.SignaturePolicy
import id.walt.auditor.VerificationPolicy
import id.walt.custodian.Custodian
import id.walt.servicematrix.ServiceMatrix
import id.walt.signatory.DataProviderRegistry
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableId
import id.walt.vclib.vclist.VerifiablePresentation

class MyCustomPolicy : VerificationPolicy {
    override val description: String
        get() = "A custom verification policy"

    override fun verify(vc: VerifiableCredential): Boolean {
        if (vc is VerifiableId) {
            val idData = MockedIdDatabase.get(vc.credentialSubject!!.personalIdentifier!!)
            if (idData != null) {
                return idData.familyName == vc.credentialSubject?.familyName && idData.firstName == vc.credentialSubject?.firstName
            }
        } else if (vc is VerifiablePresentation) {
            // This custom policy does not verify the VerifiablePresentation
            return true
        }
        return false
    }
}

val signatory = Signatory.getService()
val custodian = Custodian.getService()

fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Create VCs to verify:
    val idIter = MockedIdDatabase.mockedIds.values.iterator()
    val holder = idIter.next()
    val issuer = idIter.next()

    // Register custom data provider
    DataProviderRegistry.register(VerifiableId::class, CustomIdDataProvider())

    // Issue VC in JSON-LD and JWT format (for show-casing both formats)
    val vcJsonLd = signatory.issue(
        "VerifiableId",
        ProofConfig(issuerDid = issuer.did, subjectDid = holder.did, proofType = ProofType.LD_PROOF, dataProviderIdentifier = holder.personalIdentifier)
    )
    println("\n------------------------------- VC in JSON_LD format -------------------------------")
    println(vcJsonLd)
    val vcJwt =
        signatory.issue("VerifiableId", ProofConfig(issuerDid = issuer.did, subjectDid = holder.did, proofType = ProofType.JWT, dataProviderIdentifier = holder.personalIdentifier))
    println("\n------------------------------- VC in JWT format -------------------------------")
    println(vcJwt)

    // Present VC in JSON-LD and JWT format (for show-casing both formats)
    val vpJson = custodian.createPresentation(listOf(vcJsonLd), holder.did)
    println("------------------------------- VP in JSON_LD format -------------------------------")
    println(vpJson)
    val vpJwt = custodian.createPresentation(listOf(vcJwt), holder.did)
    println("\n------------------------------- VP in JWT format -------------------------------")
    println(vpJwt)

    // Verify VPs, using Signature, JsonSchema and a custom policy
    val resJson = Auditor.verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
    val resJwt = Auditor.verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))

    println("JSON verification result: ${resJson.overallStatus}")
    println("JWT verification result: ${resJwt.overallStatus}")
}
