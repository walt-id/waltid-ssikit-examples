import id.walt.auditor.Auditor
import id.walt.auditor.VerificationPolicy
import id.walt.auditor.VerificationPolicyResult
import id.walt.auditor.policies.SignaturePolicy
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.W3CIssuer
import id.walt.credentials.w3c.builder.W3CCredentialBuilder
import id.walt.crypto.KeyAlgorithm
import id.walt.crypto.decBase64
import id.walt.crypto.decBase64Str
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.jwt.JwtService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import java.time.Instant
import java.util.UUID
import kotlin.io.encoding.Base64


class JwtHelper(val credential: String) {
    val header get() = credential.substringBefore(".")
    val payload get() = credential.substringAfter(".").substringBefore(".")
    val signature get() = credential.substringAfterLast(".")
    val jwsSignaturePart get() = mapOf(
        "protected" to header,
        "signature" to signature
    )

    companion object {
        fun fromJWS(payload: String, sig: Map<String, String>): JwtHelper {
            val h = sig["protected"] ?: throw Exception("No header found")
            val s = sig["signature"] ?: throw Exception("No sig found")
            val jwt = "$h.$payload.$s"
            println("JWT: $jwt")
            return JwtHelper(jwt)
        }
    }
}

fun getSignature(signerVM: String, holderDid: String, payload: Map<String, Any>, issuer: W3CIssuer, credentialId: String, issuedAt: Instant): JwtHelper {
    val signedVC = Signatory.getService().issue(
        "UniversityDegree",
        ProofConfig(
            subjectDid = holderDid,
            issuerDid = issuer.id,
            proofType = ProofType.JWT,
            issuerVerificationMethod = signerVM,
            credentialId = credentialId,
            issueDate = issuedAt
        ),
        dataProvider = MergingDataProvider(payload),
        issuer,
        false
    )

    println("Signature from: $signerVM\n")
    return JwtHelper(signedVC).apply {
        println("\tHeader: ${this.header} => ${decBase64Str(header)}\n")
        println("\tPayload: ${this.payload} =>  ${decBase64Str(this.payload)}\n")
        println("\tSignature: ${signature}\n")
    }
}

fun main() {

    // issue JWT Joint Diploma Credential with multiple signatures
    ServiceMatrix("service-matrix.properties")

    // generate key pairs and DIDs for holder, issuer
    val holderKey = KeyService.getService().generate(KeyAlgorithm.RSA)
    val issuerKey = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)

    val holderDid = DidService.create(DidMethod.key, holderKey.id)
    val mainIssuerDid = DidService.create(DidMethod.key, issuerKey.id)


    val payload = mapOf(
        "credentialSubject" to mapOf(
            "degree" to listOf(
                mapOf(
                    "name" to "Bachelor of Science and Arts",
                )
            )
        )
    )

    val credentialId = UUID.randomUUID().toString()
    val issuedAt = Instant.now()
    val signerOneDid = DidService.create(DidMethod.key)
    val signerTwoDid = DidService.create(DidMethod.key)
    val subIssuers = listOf(signerOneDid, signerTwoDid)
    val issuer = W3CIssuer(mainIssuerDid, properties = mapOf(
        "sub_issuers" to subIssuers
    ))

    val signatures = subIssuers.map { getSignature(it, holderDid, payload, issuer, credentialId, issuedAt) }

    if (!signatures.all { it.payload == signatures.first().payload }) {
       throw java.lang.IllegalStateException("The payloads of each signers must match")
    }

    val signedVC = Signatory.getService().issue(
        W3CCredentialBuilder(listOf("VerifiableCredential", "JWSMultiSigCredential")).buildSubject {
            setId(holderDid)
            setProperty("payload", signatures.first().payload)
            setProperty("signatures", signatures.map { it.jwsSignaturePart })
        },
        ProofConfig(
            subjectDid = holderDid,
            issuerDid = issuer.id,
            proofType = ProofType.JWT,
            credentialId = credentialId,
            issueDate = issuedAt
        ),
        issuer,
        false
    )


    println("Credential: $signedVC\n")

    val verificationResult = Auditor.getService().verify(signedVC, listOf(SignaturePolicy(), MultiSignaturePolicy()))

    println("Result: ${verificationResult}")


}

class MultiSignaturePolicy: VerificationPolicy() {
    override val description: String
        get() = "JWS Multi Signature Verification Policy"

    override fun doVerify(vc: VerifiableCredential): VerificationPolicyResult {
        val payload = (vc.credentialSubject?.properties?.get("payload") as? String) ?: return VerificationPolicyResult.failure()
        val signatures = (vc.credentialSubject?.properties?.get("signatures") as? List<Map<String, String>>) ?: return VerificationPolicyResult.failure()
        val credentials = signatures.map { JwtHelper.fromJWS(payload, it).credential }
        return if(credentials.all { SignaturePolicy().verify(VerifiableCredential.fromString(it)).isSuccess }) {
            VerificationPolicyResult.success()
        } else VerificationPolicyResult.failure()
    }
}