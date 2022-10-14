package id.walt.ssikitexamples;

import id.walt.auditor.Auditor;
import id.walt.auditor.JsonSchemaPolicy;
import id.walt.auditor.SignaturePolicy;
import id.walt.custodian.Custodian;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import id.walt.signatory.Ecosystem;
import id.walt.signatory.ProofConfig;
import id.walt.signatory.ProofType;
import id.walt.signatory.Signatory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Credentials {

    public static void main(String[] args) {
        new Credentials().run();
    }

    public void run() {
        // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        // creating dids
        var issuerDid = DidService.INSTANCE.create(DidMethod.ebsi, null, null);
        var holderDid = DidService.INSTANCE.create(DidMethod.key, null, null);

        var expiration = Instant.now().plus(30, ChronoUnit.DAYS);

        // Issue VC in JSON-LD and JWT format (for show-casing both formats)
        var vcJsonLd = Signatory.Companion.getService().issue("VerifiableId", createProofConfig(issuerDid, holderDid, ProofType.LD_PROOF, expiration), null);
        var vcJwt = Signatory.Companion.getService().issue("VerifiableId", createProofConfig(issuerDid, holderDid, ProofType.JWT, expiration), null);

        // Present VC in JSON-LD and JWT format (for show-casing both formats)
        // expiration date is not needed when JSON-LD format
        var vpJsonLd = Custodian.Companion.getService().createPresentation(List.of(vcJsonLd), holderDid, null, null, null, null);
        var vpJwt = Custodian.Companion.getService().createPresentation(List.of(vcJwt), holderDid, null, null, null, expiration);

        // Verify VPs, using Signature, JsonSchema and a custom policy
        var resJsonLd = Auditor.Companion.getService().verify(vpJsonLd, List.of(new SignaturePolicy(), new JsonSchemaPolicy()));
        var resJwt = Auditor.Companion.getService().verify(vpJwt, List.of(new SignaturePolicy(), new JsonSchemaPolicy()));

        System.out.println("JSON-LD verification result: " + resJsonLd.getValid());
        System.out.println("JWT verification result: " + resJwt.getValid());
    }

    public ProofConfig createProofConfig(String issuerDid, String subjectDid, ProofType proofType, Instant expiration) {
        return new ProofConfig(issuerDid, subjectDid, null, null, proofType, null, null,
                null, null, null, null, expiration, null, null, null, Ecosystem.DEFAULT);
    }
}
