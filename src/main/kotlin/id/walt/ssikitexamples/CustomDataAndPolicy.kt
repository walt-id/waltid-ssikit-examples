package id.walt.ssikitexamples

import com.beust.klaxon.Json
import id.walt.auditor.Auditor
import id.walt.auditor.JsonSchemaPolicy
import id.walt.auditor.SignaturePolicy
import id.walt.auditor.VerificationPolicy
import id.walt.custodian.Custodian
import id.walt.servicematrix.ServiceMatrix
import id.walt.signatory.*
import id.walt.vclib.credentials.VerifiableId
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.schema.SchemaService
import java.time.format.DateTimeFormatter
import java.util.*

val signatory = Signatory.getService()
val custodian = Custodian.getService()

fun main() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Create VCs to verify:
    val idIter = MockedIdDatabase.mockedIds.values.iterator()
    val holder = idIter.next()
    val issuer = idIter.next()

    // Register custom data provider
    DataProviderRegistry.register(VerifiableId::class, CustomIdDataProvider())

    // Issue VC in JSON-LD and JWT format
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

    // Present VC in JSON-LD and JWT format
    val vpJson = custodian.createPresentation(listOf(vcJsonLd), holder.did, null, null, null, null)
    println("------------------------------- VP in JSON_LD format -------------------------------")
    println(vpJson)
    val vpJwt = custodian.createPresentation(listOf(vcJwt), holder.did, null, null, null, null)
    println("\n------------------------------- VP in JWT format -------------------------------")
    println(vpJwt)

    // Verify VPs, using Signature, JsonSchema and a custom policy
    val resJson = Auditor.getService().verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
    val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))

    println("JSON verification result: ${resJson.valid}")
    println("JWT verification result: ${resJwt.valid}")
}


// Custom Policy
class MyCustomPolicy : VerificationPolicy() {
    override val description: String
        get() = "A custom verification policy"

    override fun doVerify(vc: VerifiableCredential): Boolean {
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

// Custom Data Provider
class CustomIdDataProvider : SignatoryDataProvider {

    @field:SchemaService.JsonIgnore
    @Json(ignored = true)
    open val dateFormat = DateTimeFormatter.ISO_INSTANT!!

    override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        if (template is VerifiableId) {
            // get ID data for the given subject
            val idData = MockedIdDatabase.get(proofConfig.dataProviderIdentifier!!) ?: throw Exception("No ID data found for the given data-povider identifier")

            template.id = "identity#verifiableID#${UUID.randomUUID()}"
            template.issuer = proofConfig.issuerDid
            if (proofConfig.issueDate != null) template.issuanceDate = dateFormat.format(proofConfig.issueDate)
            if (proofConfig.expirationDate != null) template.expirationDate = dateFormat.format(proofConfig.expirationDate)
            template.validFrom = template.issuanceDate
            template.evidence!![0].verifier = proofConfig.issuerDid
            template.credentialSubject = VerifiableId.VerifiableIdSubject(
                idData.did,
                null,
                idData.familyName,
                idData.firstName,
                idData.dateOfBirth,
                idData.personalIdentifier,
                idData.nameAndFamilyNameAtBirth,
                idData.placeOfBirth,
                listOf(idData.currentAddress),
                idData.gender
            )
            return template
        } else {
            throw IllegalArgumentException("Only VerifiableId is supported by this data provider")
        }
    }
}