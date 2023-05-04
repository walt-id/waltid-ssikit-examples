package id.walt.ssikitexamples

import id.walt.servicematrix.ServiceMatrix
import id.walt.crypto.KeyAlgorithm
import id.walt.services.key.KeyService
import id.walt.services.did.DidService
import id.walt.model.DidMethod
import id.walt.services.ecosystems.essif.EssifClient
import id.walt.services.ecosystems.essif.didebsi.DidEbsiService
import mu.KotlinLogging

fun main(){
    ebsiWorkflow()
}

fun ebsiWorkflow(){
    // Load SSI-Kit services
    val log = KotlinLogging.logger {}
    ServiceMatrix("service-matrix.properties")

    val keyService = KeyService.getService()
    val didEbsiService = DidEbsiService.getService()

    ///////////////////////////////////////////////////////////////////////////
    // PREREQUISITE
    // Place token from https://app.preprod.ebsi.eu/users-onboarding/ in file data/ebsi/bearer-token.txt
    ///////////////////////////////////////////////////////////////////////////

    // 1. Create key
    val keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)
    val ethKeyId = keyService.generate( KeyAlgorithm.ECDSA_Secp256k1 )

    // 2. Create did
    val didEbsi = DidService.create( DidMethod.ebsi, keyId.id )
    log.debug{ "${didEbsi}"}

    val didDoc = DidService.loadDidEbsi( didEbsi )
    log.debug { didDoc.encodePretty() }

    // 3. Onboard
    val onboardDid = EssifClient.onboard( didEbsi, null )
    log.debug{ onboardDid }

    // 4. Authorize
    val authorizeDid = EssifClient.authApi( didEbsi )
    log.debug{ authorizeDid }

    // 5. Register
    val registerDid = didEbsiService.registerDid( didEbsi, ethKeyId.id )
    log.debug{ registerDid }

    // 6. Resolve
    val resolveDid = DidService.resolve( didEbsi )
    log.debug{ resolveDid }

}