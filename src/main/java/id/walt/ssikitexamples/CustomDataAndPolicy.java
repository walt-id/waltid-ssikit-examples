package id.walt.ssikitexamples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.walt.auditor.Auditor;
import id.walt.auditor.JsonSchemaPolicy;
import id.walt.auditor.SignaturePolicy;
import id.walt.auditor.VerificationPolicy;
import id.walt.custodian.Custodian;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.signatory.*;
import id.walt.vclib.credentials.VerifiableId;
import id.walt.vclib.credentials.VerifiablePresentation;
import id.walt.vclib.model.VerifiableCredential;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CustomDataAndPolicy {

    public static void main(String[] args) {
        new CustomDataAndPolicy().run();
    }

    public void run() {
        // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        // Define used services
        var signatory = Signatory.Companion.getService();
        var custodian = Custodian.Companion.getService();

        // Create VCs to verify:
        var idIter = MockedIdDatabase.INSTANCE.getMockedIds().values().iterator();
        var holder = idIter.next();
        var issuer = idIter.next();

        // Register custom data provider
        DataProviderRegistry.INSTANCE.register(kotlin.jvm.JvmClassMappingKt.getKotlinClass(VerifiableId.class), new IdDataCustomProvider());

        // Issue VC in JSON-LD and JWT format
        var vcJsonLd = signatory.issue("VerifiableId", createProofConfig(issuer.getDid(), holder.getDid(), ProofType.LD_PROOF, holder.getPersonalIdentifier()), null);
        System.out.println("\n------------------------------- VC in JSON_LD format -------------------------------");
        System.out.println(vcJsonLd);

        var vcJwt = signatory.issue("VerifiableId", createProofConfig(issuer.getDid(), holder.getDid(), ProofType.JWT, holder.getPersonalIdentifier()), null);
        System.out.println("\n------------------------------- VC in JWT format -------------------------------");
        System.out.println(vcJwt);

        // Present VC in JSON-LD and JWT format
        var vpJson = custodian.createPresentation(List.of(vcJsonLd), holder.getDid(), null, null, null, null);
        System.out.println("------------------------------- VP in JSON_LD format -------------------------------");
        System.out.println(vpJson);

        var vpJwt = custodian.createPresentation(List.of(vcJwt), holder.getDid(), null, null, null, null);
        System.out.println("\n------------------------------- VP in JWT format -------------------------------");
        System.out.println(vpJwt);

        // Verify VPs, using Signature, JsonSchema and a custom policy
        var resJson = Auditor.Companion.getService().verify(vpJson, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));
        var resJwt = Auditor.Companion.getService().verify(vpJwt, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));

        System.out.println("JSON verification result: " + resJson.getValid());
        System.out.println("JWT verification result: " + resJwt.getValid());
    }

    public ProofConfig createProofConfig(String issuerDid, String subjectDid, ProofType proofType, String dataProviderIdentifier) {
        return new ProofConfig(issuerDid, subjectDid, null, null, proofType, null, null, null, null, null, null, null, dataProviderIdentifier);
    }
}

class CustomPolicy extends VerificationPolicy {

    private String description = "A custom verification policy";

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected boolean doVerify(@NotNull VerifiableCredential vc) {
        if (vc instanceof VerifiableId) {
            var idData = MockedIdDatabase.INSTANCE.get(((VerifiableId) vc).getCredentialSubject().getPersonalIdentifier());
            if (idData != null) {
                return idData.getFamilyName().equals(((VerifiableId) vc).getCredentialSubject().getFamilyName())
                        && idData.getFirstName().equals(((VerifiableId) vc).getCredentialSubject().getFirstName());
            }
        } else if (vc instanceof VerifiablePresentation) {
            // This custom policy does not verify the VerifiablePresentation
            return true;
        }

        return false;
    }
}

class IdDataCustomProvider implements SignatoryDataProvider {

    @JsonIgnore
    private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_INSTANT;

    @NotNull
    @Override
    public VerifiableCredential populate(@NotNull VerifiableCredential template, @NotNull ProofConfig proofConfig) {
        if (template instanceof VerifiableId) {
            // get ID data for the given subject
            var idData = ofNullable(proofConfig.getDataProviderIdentifier())
                    .map(MockedIdDatabase.INSTANCE::get)
                    .orElseThrow(() -> new RuntimeException("No ID data found for the given data-povider identifier"));

            return updateCustomVerifiableCredential(template, proofConfig, idData);
        } else {
            throw new IllegalArgumentException("Only VerifiableId is supported by this data provider");
        }
    }

    private VerifiableCredential updateCustomVerifiableCredential(VerifiableCredential vc, ProofConfig proofConfig, IdData idData) {
        vc.setId("identity#verifiableID#" + UUID.randomUUID());
        vc.setIssuer(proofConfig.getIssuerDid());
        ofNullable(proofConfig.getIssueDate()).ifPresent(issueDate -> vc.setIssuanceDate(dateFormat.format(proofConfig.getIssueDate())));
        ofNullable(proofConfig.getExpirationDate()).ifPresent(expirationDate -> vc.setExpirationDate(dateFormat.format(proofConfig.getExpirationDate())));
        vc.setValidFrom(vc.getIssuanceDate());
        ((VerifiableId) vc).getEvidence().setVerifier(proofConfig.getIssuerDid());
        ((VerifiableId) vc).setCredentialSubject(createCredentialSubject(idData));

        return vc;
    }

    private VerifiableId.VerifiableIdSubject createCredentialSubject(IdData idData) {
        return new VerifiableId.VerifiableIdSubject(
                idData.getDid(),
                null,
                idData.getFamilyName(),
                idData.getFirstName(),
                idData.getDateOfBirth(),
                idData.getPersonalIdentifier(),
                idData.getNameAndFamilyNameAtBirth(),
                idData.getPlaceOfBirth(),
                idData.getCurrentAddress(),
                idData.getGender());
    }
}
