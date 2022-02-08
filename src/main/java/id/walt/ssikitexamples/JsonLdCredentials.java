package id.walt.ssikitexamples;

import com.beust.klaxon.Klaxon;
import id.walt.crypto.KeyAlgorithm;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import id.walt.services.key.KeyService;
import id.walt.services.vc.JsonLdCredentialService;
import id.walt.signatory.ProofConfig;
import id.walt.signatory.ProofType;
import id.walt.signatory.Signatory;
import id.walt.vclib.credentials.VerifiableAttestation;


import java.util.List;

public class JsonLdCredentials {
    private final Signatory signatory = Signatory.Companion.getService();
    private final JsonLdCredentialService credentialService = JsonLdCredentialService.Companion.getService();
    private final KeyService keyService = KeyService.Companion.getService();

    public static void main(String[] args) {
        new JsonLdCredentials().run();
    }

    public void run() {
        // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        /* Use services... */
        // generate key pairs for holder, issuer
        var holderKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);
        var issuerKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);

        // create dids, using did:key
        var holderDid = DidService.INSTANCE.create(DidMethod.key, holderKey.getId(), null);
        String issuerDid = DidService.INSTANCE.create(DidMethod.key, issuerKey.getId(), null);

        // issue verifiable credential

        // List registered VC templates
        signatory.listTemplates().forEach(templateName -> System.out.println(templateName));

        // Prepare VC template
        var vcTemplate = new VerifiableAttestation(List.of("https://www.w3.org/2018/credentials/v1"), "VerifiableAttestation", issuerDid, null, null, null, null, null, null, null, null);
        vcTemplate.setCredentialSubject(new VerifiableAttestation.VerifiableAttestationSubject(holderDid));

        var credentialJson = new Klaxon().toJsonString(vcTemplate, null);
        var proofConfig = new ProofConfig(issuerDid, holderDid, null, null, ProofType.LD_PROOF, null, null, null, null, null, null, null, null);
        var signedVC = credentialService.sign(credentialJson, proofConfig);

        // verify credential
        var verified = credentialService.verifyVc(issuerDid, signedVC);
        System.out.println("Verified: " + verified);
    }
}
