package id.walt.ssikitexamples

import id.walt.auditor.Auditor
import id.walt.auditor.policies.JsonSchemaPolicy
import id.walt.auditor.policies.SignaturePolicy
import id.walt.credentials.w3c.PresentableCredential
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import java.time.Instant
import java.time.temporal.ChronoUnit


fun main()
{
    credentials()
}

fun credentials() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    val issuerDid = DidService.create(DidMethod.ebsi)
    val holderDid = DidService.create(DidMethod.key)

    val expiration = Instant.now().plus(30, ChronoUnit.DAYS)

    // Issue VC in JSON-LD and JWT format (for show-casing both formats)
    val vcJsonLd = Signatory.getService().issue(
        "VerifiableId", ProofConfig(
            issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.LD_PROOF, expirationDate = expiration
        )
    )
    val vcJwt = Signatory.getService().issue(
        "VerifiableId", ProofConfig(
            issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.JWT, expirationDate = expiration
        )
    )

    // Present VC in JSON-LD and JWT format (for show-casing both formats)
    // expiration date is not needed when JSON-LD format
    val vpJsonLd = Custodian.getService().createPresentation(
        listOf(PresentableCredential(VerifiableCredential.fromString(vcJsonLd))), holderDid, expirationDate = null
    )
    val vpJwt = Custodian.getService().createPresentation(
        listOf(PresentableCredential(VerifiableCredential.fromString(vcJwt))), holderDid, expirationDate = expiration
    )

    // Verify VPs, using Signature, JsonSchema and a custom policy
    val resJsonLd = Auditor.getService().verify(vpJsonLd, listOf(SignaturePolicy(), JsonSchemaPolicy()))
    val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy()))

    println("JSON-LD verification result: ${resJsonLd.result}")
    println("JWT verification result: ${resJwt.result}")
}


