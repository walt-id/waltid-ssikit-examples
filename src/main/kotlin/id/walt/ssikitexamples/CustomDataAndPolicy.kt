package id.walt.ssikitexamples

import com.beust.klaxon.Json
import id.walt.auditor.Auditor
import id.walt.auditor.SimpleVerificationPolicy
import id.walt.auditor.VerificationPolicyResult
import id.walt.auditor.policies.JsonSchemaPolicy
import id.walt.auditor.policies.SignaturePolicy
import id.walt.credentials.w3c.PresentableCredential
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.builder.W3CCredentialBuilder
import id.walt.custodian.Custodian
import id.walt.servicematrix.ServiceMatrix
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.SignatoryDataProvider
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

val signatory = Signatory.getService()
val custodian = Custodian.getService()

fun main(){
    customDataAndPolicy()
}

fun customDataAndPolicy() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Create VCs to verify:
    val idIter = MockedIdDatabase.mockedIds.values.iterator()
    val holder = idIter.next()
    val issuer = idIter.next()

    // Issue VC in JSON-LD and JWT format
    val vcJsonLd = signatory.issue(
        "VerifiableId",
        ProofConfig(issuerDid = issuer.did, subjectDid = holder.did, proofType = ProofType.LD_PROOF, dataProviderIdentifier = holder.personalIdentifier),
        dataProvider = CustomIdDataProvider()
    )
    println("\n------------------------------- VC in JSON_LD format -------------------------------")
    println(vcJsonLd)
    val vcJwt = signatory.issue(
        "VerifiableId", ProofConfig(
            issuerDid = issuer.did,
            subjectDid = holder.did,
            proofType = ProofType.JWT,
            dataProviderIdentifier = holder.personalIdentifier
        ), dataProvider = CustomIdDataProvider()
    )
    println("\n------------------------------- VC in JWT format -------------------------------")
    println(vcJwt)

    // Verify VPs, using Signature, JsonSchema and a custom policy
    val resVcJson = Auditor.getService().verify(vcJsonLd, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
    val resVcJwt = Auditor.getService().verify(vcJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))

    println("JSON verification result: ${resVcJson.result}")
    println("JWT verification result: ${resVcJwt.result}")

    // Present VC in JSON-LD and JWT format
    val vpJson = custodian.createPresentation(
        listOf(PresentableCredential(VerifiableCredential.fromString(vcJsonLd))), holder.did, null, null, null, null
    )
    println("------------------------------- VP in JSON_LD format -------------------------------")
    println(vpJson)
    val vpJwt = custodian.createPresentation(
        listOf(PresentableCredential(VerifiableCredential.fromString(vcJwt))), holder.did, null, null, null, null
    )
    println("\n------------------------------- VP in JWT format -------------------------------")
    println(vpJwt)

    // Verify VPs, using Signature, JsonSchema and a custom policy
    val resJson = Auditor.getService().verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))
    val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy(), MyCustomPolicy()))

    println("JSON verification result: ${resJson.result}")
    println("JWT verification result: ${resJwt.result}")
}


// Custom Policy
class MyCustomPolicy : SimpleVerificationPolicy() {
    override val description: String
        get() = "A custom verification policy"

    override fun doVerify(vc: VerifiableCredential): VerificationPolicyResult {
        return when (vc.type.last()) {
            "VerifiableId" -> MockedIdDatabase.get(vc.credentialSubject!!.properties["personalIdentifier"] as String)
                ?.takeIf {
                    it.familyName == vc.credentialSubject!!.properties["familyName"] && it.firstName == vc.credentialSubject!!.properties["firstName"]
                }?.let { VerificationPolicyResult.success() } ?: VerificationPolicyResult.failure(Exception(""))

            "VerifiablePresentation" -> {
                // This custom policy does not verify the VerifiablePresentation
                VerificationPolicyResult.success()
            }
            else -> VerificationPolicyResult.failure(IllegalArgumentException(""))
        }
    }
}

// Custom Data Provider
class CustomIdDataProvider : SignatoryDataProvider {

    @Json(ignored = true)
    val dateFormat = DateTimeFormatter.ISO_INSTANT!!

    override fun populate(credentialBuilder: W3CCredentialBuilder, proofConfig: ProofConfig): W3CCredentialBuilder {
        return when(credentialBuilder.type.last()) {
            "VerifiableId" -> {
                credentialBuilder.setId("identity#verifiableID#${UUID.randomUUID()}")
                    .setIssuer(proofConfig.issuerDid)
                    .setIssuanceDate(proofConfig.issueDate?.let { Instant.parse(dateFormat.format(it)) } ?: Instant.now())
                    .setExpirationDate(proofConfig.expirationDate?.let { Instant.parse(dateFormat.format(it)) } ?: Instant.now())
                    .setValidFrom(proofConfig.issueDate?.let { Instant.parse(dateFormat.format(it)) } ?: Instant.now())
                    .setProperty("evidence", buildList {
                        add(mapOf("verifier" to proofConfig.issuerDid))
                    }).buildSubject {
                        // get ID data for the given subject
                        val idData = MockedIdDatabase.get(proofConfig.dataProviderIdentifier!!)
                            ?: throw Exception("No ID data found for the given data-povider identifier")
                        setProperty("familyName", idData.familyName)
                        setProperty("firstName", idData.firstName)
                        setProperty("dateOfBirth", idData.dateOfBirth)
                        setProperty("personalIdentifier", idData.personalIdentifier)
                        setProperty("nameAndFamilyNameAtBirth", idData.nameAndFamilyNameAtBirth)
                        setProperty("placeOfBirth", idData.placeOfBirth)
                        setProperty("currentAddress", listOf(idData.currentAddress))
                        setProperty("gender", idData.gender)
                    }
            }

            else -> throw IllegalArgumentException("Only VerifiableId is supported by this data provider");
        }
    }
}