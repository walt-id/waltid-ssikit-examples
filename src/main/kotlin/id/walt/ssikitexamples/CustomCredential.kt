package id.walt.ssikitexamples

import com.google.common.io.Resources.getResource
import id.walt.common.prettyPrint
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.W3CCredentialSubject
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.credentials.w3c.toVerifiableCredential
import id.walt.servicematrix.ServiceMatrix

fun main(){
    customCredential()
}

fun customCredential() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Creating custom credential and set the data to be issued
    val myCustomCredential = VerifiableCredential.fromJson(customCredentialData)

    // Registering a custom credential template
    VcTemplateManager.register(customCredentialData::class.java.name, myCustomCredential)

    println("This is my custom credential: $myCustomCredential")

    // Encoding a custom credential to JSON
    val encodedCredential = myCustomCredential.encode()
    println("Encoding credential ...")
    println(encodedCredential.prettyPrint())

    // Decoding a JSON credential
    println("Decoding credential ...")
    val decodedCredential: VerifiableCredential = encodedCredential.toVerifiableCredential()
    println(decodedCredential)

    // Check if Mister John is a Vendor
    val (givenName, isVendor) = checkIfVendor(decodedCredential)

    println("Mister \"$givenName\" ${if (isVendor == true) "is" else "isn't"} a vendor.")

    // Check if Mister James is a Vendor
    myCustomCredential.credentialSubject = W3CCredentialSubject(
        id = myCustomCredential.credentialSubject?.id,
        properties = mapOf("givenName" to "James", "type" to listOf("Person"))
    )

    val someInput = myCustomCredential.encode()

    val decodedCredential2 = someInput.toVerifiableCredential()

    val (givenName2, isVendor2) = checkIfVendor(decodedCredential2)

    println("Mister \"$givenName2\" ${if (isVendor2 == true) "is" else "isn't"} a vendor.")
}

    // This is our custom credential
    val customCredentialData = getResource("customCredential.json").readText()

fun checkIfVendor(decodedCredential: VerifiableCredential): List<Any> = decodedCredential.credentialSubject?.takeIf {
    it.properties["givenName"] != null && it.properties["type"] != null
}?.let {
    listOf(it.properties["type"]!!, (it.properties["type"] as? List<String>)?.contains("Vendor") ?: false)
} ?: throw Error("Invalid credential was supplied!")