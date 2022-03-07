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
    println("${keyHandle1} has been loaded by keyId into KeyService.")

    // adding an key alias
    val keyAlias = "${keyId}Alias"
    keyService.addAlias(keyId, keyAlias)

    // loading key by alias
    val keyHandle2 = keyService.load(keyAlias)
    println("${keyHandle2} has been loaded by alias into KeyService.")

    // exporting public key in JWK format
    val exportedPubKey = keyService.export(keyAlias, KeyFormat.JWK, KeyType.PUBLIC)
    println("${exportedPubKey} public key exported in JWK format.")

    // exporting private key in JWK format. Note that KeyFormat and KeyType are optional parameter.
    val exportedPrivKey = keyService.export(keyAlias, KeyFormat.JWK, KeyType.PRIVATE)
    println("${exportedPrivKey} private key exported in JWK format.")

    // Deleting key
    keyService.delete(keyId.id)

    // Importing key
    val importedKeyId = keyService.importKey(exportedPrivKey)
    println("${importedKeyId} imported into KeyService")
}
