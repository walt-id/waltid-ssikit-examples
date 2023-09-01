import id.walt.auditor.Auditor
import id.walt.auditor.policies.SignaturePolicy
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.signatory.JwtPayloadUpdate
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider


class JwtHelper(credential: String) {
    val header: String
    val payload: String
    val signature: String

    init {
        credential.split(".").let {
            header = it[0]
            payload = it[1]
            signature = it[2]
        }
    }
}

fun getSignature(holderDid: String, payload: Map<String, Any>): JwtHelper {

    val issuerKey = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)
    val issuerDid = DidService.create(DidMethod.key, issuerKey.id)

    val signedVC = Signatory.getService().issue(
        "UniversityDegree",
        ProofConfig(subjectDid = holderDid, issuerDid = issuerDid, proofType = ProofType.JWT, jwtPayloadUpdate = JwtPayloadUpdate.NO),
        dataProvider = MergingDataProvider(payload),
        null,
        false
    )

    println("Signature from: $issuerDid")
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
    val issuerDid = DidService.create(DidMethod.key, issuerKey.id)


    val payload = mapOf(
        "credentialSubject" to mapOf(
            "degree" to listOf(
                mapOf(
                    "name" to "Bachelor of Science and Arts",
                )
            )
        )
    )

    val signerOne = getSignature(holderDid, payload)
    val signerTwo = getSignature(holderDid, payload)

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
        ProofConfig(subjectDid = holderDid, issuerDid = issuerDid, proofType = ProofType.LD_PROOF),
        dataProvider = MergingDataProvider(data),
        null,
        false
    )


    println(signedVC)

    // TODO: Add policy that verifies the signatures of the payload
    val verificationResult = Auditor.getService().verify(signedVC, listOf(SignaturePolicy()))

    println(verificationResult)


}