package id.walt.ssikitexamples

import com.beust.klaxon.Json
import id.walt.common.prettyPrint
import id.walt.ssikitexamples.CustomCredential.CustomCredentialSubject
import id.walt.vclib.credentials.Europass
import id.walt.vclib.model.*
import id.walt.vclib.registry.VcTypeRegistry
import id.walt.vclib.registry.VerifiableCredentialMetadata


fun checkIfVendor(decodedCredential: VerifiableCredential): List<Any> = when (decodedCredential) {
    is CustomCredential -> {
        val subject: CustomCredential.CustomCredentialSubject = decodedCredential.credentialSubject!!

        listOf(subject.givenName!!, subject.type!!.contains("Vendor"))
    }
    is Europass -> throw Error("Europass isn't supported, please supply an CustomCredential/XYZMarketCredential/...")
    else -> throw Error("Invalid credential was supplied!")
}


fun main() {
    // Registering credential
    VcTypeRegistry.register(CustomCredential.Companion, CustomCredential::class)

    // Creating custom credential
    val myCustomCredential = CustomCredential(
        credentialSubject = CustomCredential.CustomCredentialSubject(
            id = "did:example:123",
            type = listOf(
                "Vendor",
                "Person"
            ),
            givenName = "JOHN",
            birthDate = "1958-08-17"
        ),
        issuer = "did:example:456",
        proof = Proof(
            "Ed25519Signature2018",
            "2020-04-22T10:37:22Z",
            "assertionMethod",
            "did:example:456#key-1",
            "eyJjcml0IjpbImI2NCJdLCJiNjQiOmZhbHNlLCJhbGciOiJFZERTQSJ9..BhWew0x-txcroGjgdtK-yBCqoetg9DD9SgV4245TmXJi-PmqFzux6Cwaph0r-mbqzlE17yLebjfqbRT275U1AA"
        )
    )

    println("This is my custom credential: $myCustomCredential")

    // Encoding a custom credential to JSON
    val ecodedCredential = myCustomCredential.encode()
    println("Encoding credential ...")
    println(ecodedCredential.prettyPrint())

    // Decoding a JSON credential
    println("Decoding credential ...")
    val decodedCredential: VerifiableCredential = ecodedCredential.toCredential()
    println(decodedCredential)

    // Check if Mister John is a Vendor
    val (givenName, isVendor) = checkIfVendor(decodedCredential)

    println("Mister \"$givenName\" ${if (isVendor == true) "is" else "isn't"} a vendor.")

    // Check if Mister James is a Vendor
    myCustomCredential.credentialSubject!!.givenName = "JAMES"
    myCustomCredential.credentialSubject!!.type = listOf("Person")

    val someInput = myCustomCredential.encode()

    val decodedCredential2 = someInput.toCredential()

    val (givenName2, isVendor2) = checkIfVendor(decodedCredential2)

    println("Mister \"$givenName2\" ${if (isVendor2 == true) "is" else "isn't"} a vendor.")
}

// This is our custom credential
data class CustomCredential(
    @Json(name = "@context")
    var context: List<String> = listOf("https://www.w3.org/2018/credentials/v1"),
    override var id: String? = null,
    override var issuer: String?,
    @Json(serializeNull = false) override var issuanceDate: String? = null,
    @Json(serializeNull = false) override var validFrom: String? = null,
    @Json(serializeNull = false) override var expirationDate: String? = null,
    @Json(serializeNull = false) override var credentialSubject: CustomCredentialSubject?,
    @Json(serializeNull = false) override var credentialSchema: CredentialSchema? = null,
    @Json(serializeNull = false) override var proof: Proof? = null,
) : AbstractVerifiableCredential<CustomCredentialSubject>(type) {
    data class CustomCredentialSubject(
        @Json(serializeNull = false) override var id: String? = null, // did:ebsi:00000004321
        @Json(serializeNull = false) var type: List<String>? = null,
        @Json(serializeNull = false) var givenName: String? = null, // JOHN
        @Json(serializeNull = false) var birthDate: String? = null // 1958-08-17
    ) : CredentialSubject()

    companion object : VerifiableCredentialMetadata(
        type = listOf("VerifiableCredential", "CustomCredential"),
        template = {
            CustomCredential(
                credentialSubject = CustomCredentialSubject(
                    id = "did:example:123",
                    type = listOf(
                        "Vendor",
                        "Person"
                    ),
                    givenName = "JOHN",
                    birthDate = "1958-08-17"
                ),
                issuer = "did:example:456",
                proof = Proof(
                    "Ed25519Signature2018",
                    "2020-04-22T10:37:22Z",
                    "assertionMethod",
                    "did:example:456#key-1",
                    "eyJjcml0IjpbImI2NCJdLCJiNjQiOmZhbHNlLCJhbGciOiJFZERTQSJ9..BhWew0x-txcroGjgdtK-yBCqoetg9DD9SgV4245TmXJi-PmqFzux6Cwaph0r-mbqzlE17yLebjfqbRT275U1AA"
                )
            )
        }
    )

}
