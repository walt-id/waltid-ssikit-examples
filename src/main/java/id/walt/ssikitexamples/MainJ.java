package id.walt.ssikitexamples;

import com.beust.klaxon.Klaxon;
import id.walt.crypto.KeyAlgorithm;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import id.walt.services.key.KeyService;
import id.walt.services.vc.VCService;
import id.walt.signatory.Signatory;
import id.walt.vclib.model.CredentialStatus;
import id.walt.vclib.vclist.VerifiableAttestation;

import java.util.List;

public class MainJ {
    private Signatory signatory = Signatory.Companion.getService();
    private VCService credentialService = VCService.Companion.getService();
    private KeyService keyService = KeyService.Companion.getService();

    public void run() {
// Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        /* Use services... */
        // generate key pairs for holder, issuer
        var holderKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);
        var issuerKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);

        // create dids, using did:key
        var holderDid = DidService.INSTANCE.create(DidMethod.key, holderKey.getId());
        String issuerDid = DidService.INSTANCE.create(DidMethod.key, issuerKey.getId());

        // issue verifiable credential

        // List registered VC templates
        signatory.listTemplates().forEach(templateName -> System.out.println(templateName));

        // Get default VC template from VCService
        System.out.println("Default VC template: " + credentialService.defaultVcTemplate().toString());

        var vcTemplate = new VerifiableAttestation(List.of("https://www.w3.org/2018/credentials/v1"), "VerifiableAttestation", issuerDid, null,null, null, null, null, null, null);
        vcTemplate.setCredentialSubject(new VerifiableAttestation.CredentialSubject(holderDid));

        var signedVC = credentialService.sign(issuerDid, new Klaxon().toJsonString(vcTemplate, null), null, null, null, null);

        // verify credential
        var verified = credentialService.verifyVc(issuerDid, signedVC);
        System.out.println("Verified: " + Boolean.toString(verified));
    }

    public static void main(String[] args) {
        new MainJ().run();
    }
}
