package id.walt.ssikitexamples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.walt.auditor.Auditor;
import id.walt.auditor.JsonSchemaPolicy;
import id.walt.auditor.SignaturePolicy;
import id.walt.auditor.SimpleVerificationPolicy;
import id.walt.credentials.w3c.VerifiableCredential;
import id.walt.credentials.w3c.W3CIssuer;
import id.walt.credentials.w3c.builder.W3CCredentialBuilder;
import id.walt.custodian.Custodian;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.signatory.*;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CustomDataAndPolicy {

    public static void main(String[] args) {
        new CustomDataAndPolicy().run();
    }

    public void run() {
        // Load walt.id SSI Kit services from "$workingDirectory/service-matrix.properties"
        new ServiceMatrix("service-matrix.properties");

        // Define used services
        var signatory = Signatory.Companion.getService();
        var custodian = Custodian.Companion.getService();

        // Create VCs to verify:
        var idIter = MockedIdDatabase.INSTANCE.getMockedIds().values().iterator();
        var holder = idIter.next();
        var issuer = idIter.next();

        // Issue VC in JSON-LD and JWT format
        var vcJsonLd = signatory.issue("VerifiableId", createProofConfig(issuer.getDid(), holder.getDid(), ProofType.LD_PROOF, holder.getPersonalIdentifier()), new IdDataCustomProvider(), null, false);
        System.out.println("\n------------------------------- VC in JSON-LD format -------------------------------");
        System.out.println(vcJsonLd);

        var vcJwt = signatory.issue("VerifiableId", createProofConfig(issuer.getDid(), holder.getDid(), ProofType.JWT, holder.getPersonalIdentifier()), new IdDataCustomProvider(), null, false);
        System.out.println("\n------------------------------- VC in JWT format -------------------------------");
        System.out.println(vcJwt);

        // Present VC in JSON-LD and JWT format
        var vpJsonLd = custodian.createPresentation(List.of(vcJsonLd), holder.getDid(), null, null, null, null);
        System.out.println("------------------------------- VP in JSON-LD format -------------------------------");
        System.out.println(vpJsonLd);

        var vpJwt = custodian.createPresentation(List.of(vcJwt), holder.getDid(), null, null, null, null);
        System.out.println("\n------------------------------- VP in JWT format -------------------------------");
        System.out.println(vpJwt);

        // Verify VPs, using Signature, JsonSchema and a custom policy
        var resJsonLd = Auditor.Companion.getService().verify(vpJsonLd, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));
        var resJwt = Auditor.Companion.getService().verify(vpJwt, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));

        System.out.println("JSON verification result: " + resJsonLd.getValid());
        System.out.println("JWT verification result: " + resJwt.getValid());
    }

    public ProofConfig createProofConfig(String issuerDid, String subjectDid, ProofType proofType, String dataProviderIdentifier) {
        return new ProofConfig(issuerDid, subjectDid, null, null, proofType, null, null,
                null, null, null, null, null, dataProviderIdentifier, null , null, Ecosystem.DEFAULT  );
    }
}

class CustomPolicy extends SimpleVerificationPolicy {

    private String description = "A custom verification policy";

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected boolean doVerify(@NotNull VerifiableCredential vc) {
        var idData = MockedIdDatabase.INSTANCE.get((String) vc.getCredentialSubject().getProperties().get("personalIdentifier"));
        if (idData != null) {
            return idData.getFamilyName().equals(vc.getCredentialSubject().getProperties().get("familyName"))
                    && idData.getFirstName().equals(vc.getCredentialSubject().getProperties().get("firstName"));
        }

        return false;
    }
}

class IdDataCustomProvider implements SignatoryDataProvider {

    @JsonIgnore
    private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_INSTANT;

    @NotNull
    @Override
    public W3CCredentialBuilder populate(@NotNull W3CCredentialBuilder credentialBuilder, @NotNull ProofConfig proofConfig) {
        if (credentialBuilder.getType().get(0).equals("VerifiableId")) {

            var idData = ofNullable(proofConfig.getDataProviderIdentifier())
                    .map(MockedIdDatabase.INSTANCE::get)
                    .orElseThrow(() -> new RuntimeException("No ID data found for the given data-provider identifier"));

            return updateCustomVerifiableCredential(credentialBuilder, proofConfig, idData);
        } else {
            throw new IllegalArgumentException("Only VerifiableId is supported by this data provider");
        }
    }

    private W3CCredentialBuilder updateCustomVerifiableCredential(W3CCredentialBuilder credentialBuilder, ProofConfig proofConfig, IdData idData) {
        credentialBuilder.setId("identity#verifiableID#" + UUID.randomUUID());
        credentialBuilder.setIssuer(new W3CIssuer(proofConfig.getIssuerDid()));
        ofNullable(proofConfig.getIssueDate()).ifPresent(issueDate -> credentialBuilder.setIssuanceDate(Instant.parse(dateFormat.format(issueDate))));
        ofNullable(proofConfig.getExpirationDate()).ifPresent(expirationDate -> credentialBuilder.setExpirationDate(Instant.parse(dateFormat.format(expirationDate))));
        credentialBuilder.setValidFrom(proofConfig.getIssueDate());
        credentialBuilder.setProperty("evidence", Map.of(
                "verifier", proofConfig.getIssuerDid()
        ));
        return credentialBuilder.buildSubject(subjectBuilder -> {
            subjectBuilder.setId(idData.getDid());
            subjectBuilder.setProperty("familyName", idData.getFamilyName());
            subjectBuilder.setProperty("firstName", idData.getFirstName());
            subjectBuilder.setProperty("dateOfBirth", idData.getDateOfBirth());
            subjectBuilder.setProperty("personalIdentifier", idData.getPersonalIdentifier());
            subjectBuilder.setProperty("nameAndFamilyNameAtBirth", idData.getNameAndFamilyNameAtBirth());
            subjectBuilder.setProperty("placeOfBirth", idData.getPlaceOfBirth());
            subjectBuilder.setProperty("currentAddress", Collections.singletonList(idData.getCurrentAddress()));
            subjectBuilder.setProperty("gender", idData.getGender());

            return Unit.INSTANCE;
        });
    }
}