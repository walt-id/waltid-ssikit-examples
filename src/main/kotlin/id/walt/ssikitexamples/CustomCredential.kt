package id.walt.ssikitexamples

import com.beust.klaxon.Json
import id.walt.vclib.Helpers.encode
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.VcLibManager
import id.walt.vclib.model.Proof
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.registry.VerifiableCredentialMetadata
import id.walt.vclib.vclist.Europass

fun checkIfVendor(decodedCredential: VerifiableCredential): List<Any> = when (decodedCredential) {
    is DataDaoCredential -> {
        val subject: DataDaoCredential.DataDaoCredentialSubject = decodedCredential.credentialSubject!!

        listOf(subject.givenName!!, subject.type!!.contains("Vendor"))
    }
    is Europass -> throw Error("Europass isn't supported, please supply an DataDaoCredential/XYZMarketCredential/...")
    else -> throw Error("Invalid credential was supplied!")
}


fun main() {
    // Registering credential
    VcLibManager.register(DataDaoCredential.Companion, DataDaoCredential::class)

    // Creating custom credential
    val myCustomCredential = DataDaoCredential(
        credentialSubject = DataDaoCredential.DataDaoCredentialSubject(
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

    // Encoding a custom credential to JSON
    val customCredentialJson = myCustomCredential.encode()
    println("This is my custom credential: $myCustomCredential")

    // Decoding a JSON credential
    println("Decoding json...")
    val decodedCredential: VerifiableCredential = customCredentialJson.toCredential()

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
data class DataDaoCredential(
    @Json(name = "@context")
    var context: List<String> = listOf("https://www.w3.org/2018/credentials/v1"),
    @Json(serializeNull = false) var credentialSubject: DataDaoCredentialSubject? = null,
    @Json(serializeNull = false) var issuer: String? = null,
    @Json(serializeNull = false) var proof: Proof?,
) : VerifiableCredential(type) {
    data class DataDaoCredentialSubject(
        @Json(serializeNull = false) var id: String? = null, // did:ebsi:00000004321
        @Json(serializeNull = false) var type: List<String>? = null,
        @Json(serializeNull = false) var givenName: String? = null, // JOHN
        @Json(serializeNull = false) var birthDate: String? = null // 1958-08-17
    )

    companion object : VerifiableCredentialMetadata(
        type = listOf("VerifiableCredential", "DataDaoCredential"),
        template = {
            DataDaoCredential(
                credentialSubject = DataDaoCredentialSubject(
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

    @Json(serializeNull = false)
    override var id: String? = null
}
