package id.walt.ssikitexamples;

import id.walt.crypto.KeyAlgorithm;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import id.walt.services.essif.EssifClient;
import id.walt.services.essif.didebsi.DidEbsiService;
import id.walt.services.key.KeyService;

public class DidEbsi {

    private final KeyService keyService = KeyService.Companion.getService();
    private final DidEbsiService didEbsiService = DidEbsiService.Companion.getService();

    public static void main(String[] args) {
        new DidEbsi().run();
    }

    public void run() {

        // Load Walt.ID SSI Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        System.out.println("SSI Kit example how to create a DID EBSI");

        ///////////////////////////////////////////////////////////////////////////
        // Create an asymmetric keypair
        ///////////////////////////////////////////////////////////////////////////

        var keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);
        System.out.println("EBSI key generated: " + keyId);

        var ethKeyId = keyService.generate(KeyAlgorithm.ECDSA_Secp256k1);
        System.out.println("ETH key generated: " + ethKeyId);

        ///////////////////////////////////////////////////////////////////////////
        // Create an EBSI compliant Decentralized Identifier
        ///////////////////////////////////////////////////////////////////////////

        var didEbsi = DidService.INSTANCE.create(DidMethod.ebsi, keyId.getId()); // Note, that the DID is stored in /data/did/create

        System.out.println("EBSI DID created : " + didEbsi);

        var didDoc = DidService.INSTANCE.loadDidEbsi(didEbsi);
        System.out.print("\nDID EBSI Document loaded:\n" + didDoc);

        ///////////////////////////////////////////////////////////////////////////
        // Registering the DID on the ledger
        // Pre requisite: put token from https://app.preprod.ebsi.eu/users-onboarding/ in file data/ebsi/bearer-token.txt
        ///////////////////////////////////////////////////////////////////////////

        EssifClient.INSTANCE.onboard(didEbsi, null);

        EssifClient.INSTANCE.authApi(didEbsi);

        didEbsiService.registerDid(didEbsi, ethKeyId.getId());

        System.out.print("\nDID EBSI Document successfully registered:\n" + didEbsi);

        System.out.print("\nCheck EBSI API at https://api.preprod.ebsi.eu/docs/?urls.primaryName=DID%20Registry%20API:\n");


    }
}
