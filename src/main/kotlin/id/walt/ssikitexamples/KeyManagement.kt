package id.walt.ssikitexamples

import id.walt.crypto.KeyAlgorithm
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.key.KeyFormat
import id.walt.services.key.KeyService
import id.walt.services.keystore.KeyType

fun main() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Define used services
    val keyService = KeyService.getService()

    // generate an asymmetric key of type EdDSA ED25519
    val keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

    // loading key by ID
    val keyHandle1 = keyService.load(keyId.id)

    // adding an key alias
    val keyAlias = "${keyId}Alias"
    keyService.addAlias(keyId, keyAlias)

    // loading key by alias
    val keyHandle2 = keyService.load(keyAlias)

    // exporting public key in JWK format
    val exportedPubKey = keyService.export(keyAlias)

    println(exportedPubKey)

    // exporting private key in JWK format. Note that KeyFormat and KeyType are optional parameter.
    val exportedPrivKey = keyService.export(keyAlias, KeyFormat.JWK, KeyType.PRIVATE)

    println(exportedPrivKey)

    // Deleting key
    keyService.delete(keyId.id)

    // Importing key
    keyService.importKey(exportedPrivKey)


    val keyHandle3 = keyService.load(keyId.id)
}
