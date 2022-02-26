package id.walt.ssikitexamples;

import id.walt.crypto.KeyAlgorithm;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import id.walt.services.key.KeyService;
import id.walt.signatory.ProofConfig;
import id.walt.signatory.ProofType;
import id.walt.signatory.Signatory;
import id.walt.signatory.dataproviders.MergingDataProvider;
import id.walt.vclib.templates.VcTemplateManager;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

public class CustomVcTemplate {

    public static void main(String[] args) {
        new CustomVcTemplate().run();
    }

    public void run() {
        // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        // Define used services
        var signatory = Signatory.Companion.getService();
        var keyService = KeyService.Companion.getService();

        /* Use services... */
        // generate key pairs for holder, issuer
        var holderKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);
        var issuerKey = keyService.generate(KeyAlgorithm.EdDSA_Ed25519);

        // create dids, using did:key
        var holderDid = DidService.INSTANCE.create(DidMethod.key, holderKey.getId(), null);
        var issuerDid = DidService.INSTANCE.create(DidMethod.key, issuerKey.getId(), null);

        // List registered VC templates
        List<String> vcTemplates = signatory.listTemplates();
        vcTemplates.forEach(template -> {
            System.out.println(vcTemplates.indexOf(template) + ": " + template);
        });

        // Create VC template
        var defaultVerifiableDiploma = VcTemplateManager.INSTANCE.loadTemplate("VerifiableDiploma");
        System.out.println("Default Verifiable Diploma - " + defaultVerifiableDiploma.encodePretty());

        // Prepare desired custom data that should replace the default template data
        var data = Map.ofEntries(
                credentialSubjectEntry(new SimpleEntry("givenNames", "Yves"), new SimpleEntry("familyName", "SMITH"), new SimpleEntry("dateOfBirth", "2000-02-04"))
        );

        // Custom VC template
        var customVerifiableDiploma = new MergingDataProvider(data).populate(
                defaultVerifiableDiploma,
                new ProofConfig(issuerDid, holderDid, null, null, ProofType.LD_PROOF, null, null, null, null, null, null, null, null)
        );
        System.out.println("Custom Verifiable Diploma - " + customVerifiableDiploma.encodePretty());

    }

    private SimpleEntry<String, Map<String, String>> credentialSubjectEntry(SimpleEntry<String, String>... pairs) {
        return new SimpleEntry("credentialSubject", Map.ofEntries(pairs));
    }
}
