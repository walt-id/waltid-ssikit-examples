package id.walt.ssikitexamples;

import id.walt.crypto.KeyAlgorithm;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.key.KeyFormat;
import id.walt.services.key.KeyService;
import id.walt.services.keystore.KeyType;

public class KeyManagement {

    public static void main(String[] args) {
        new KeyManagement().run();
    }

    public void run() {
        // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        // Define used services
        var keyService = KeyService.Companion.getService();

        // generate an asymmetric key of type EdDSA ED25519
        var keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);

        // loading key by ID
        var keyHandle1 = keyService.load(keyId.getId());
        System.out.println(keyHandle1 + " has been loaded by keyId into KeyService.");

        // adding an key alias
        var keyAlias = keyId + "Alias";
        keyService.addAlias(keyId, keyAlias);

        // loading key by alias
        var keyHandle2 = keyService.load(keyAlias);
        System.out.println(keyHandle2 + " has been loaded by alias into KeyService.");

        // exporting public key in JWK format
        var exportedPubKey = keyService.export(keyAlias, KeyFormat.JWK, KeyType.PUBLIC);
        System.out.println(exportedPubKey + " public key exported in JWK format.");

        // exporting private key in JWK format. Note that KeyFormat and KeyType are optional parameter.
        var exportedPrivKey = keyService.export(keyAlias, KeyFormat.JWK, KeyType.PRIVATE);
        System.out.println(exportedPrivKey + " private key exported in JWK format.");

        // Deleting key
        keyService.delete(keyId.getId());

        // Importing key
        var importedKeyId = keyService.importKey(exportedPrivKey);
        System.out.println(importedKeyId + " imported into KeyService");
    }
}
