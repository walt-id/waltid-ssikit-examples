import id.walt.auditor.Auditor
import id.walt.auditor.policies.SignaturePolicy
import id.walt.credentials.w3c.W3CIssuer
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import java.time.Instant
import java.util.UUID


class JwtHelper(val credential: String) {
    val header get() = credential.substringBefore(".")
    val payload get() = credential.substringAfter(".").substringBefore(".")
    val signature get() = credential.substringAfterLast(".")
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

    println("Signature from: $signerVM")
    println(signedVC)

    return JwtHelper(signedVC)
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
    val issuer = W3CIssuer(mainIssuerDid, properties = mapOf(
        "signer1" to signerOneDid,
        "signer2" to signerTwoDid
    ))

    val signerOne = getSignature(signerOneDid, holderDid, payload, issuer, credentialId, issuedAt)
    val signerTwo = getSignature(signerTwoDid, holderDid, payload, issuer, credentialId, issuedAt)

    if (signerOne.payload != signerTwo.payload) {
       throw java.lang.IllegalStateException("The payloads of each signers must match")
    }

    // prepare payload of credential
    val data = mapOf(
        "credentialSubject" to mapOf(
            "id" to holderDid,
            "payload" to signerOne.payload,
            "signatures" to listOf(
                mapOf(
                    "protected" to signerOne.header,
                    "signature" to signerOne.signature
                ), mapOf(
                    "protected" to signerTwo.header,
                    "signature" to signerTwo.signature
                )
            )
        )
    )

    val signedVC = Signatory.getService().issue(
        "VerifiableDiploma",
        ProofConfig(
            subjectDid = holderDid,
            issuerDid = issuer.id,
            proofType = ProofType.LD_PROOF,
            credentialId = credentialId,
            issueDate = issuedAt
        ),
        dataProvider = MergingDataProvider(data),
        issuer,
        false
    )


    println(signedVC)

    // TODO: Add policy that verifies the signatures of the payload
    val verificationResult = Auditor.getService().verify(signedVC, listOf(SignaturePolicy()))

    println(verificationResult)


}