package id.walt.ssikitexamples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.walt.auditor.Auditor;
import id.walt.auditor.SimpleVerificationPolicy;
import id.walt.auditor.VerificationPolicyResult;
import id.walt.auditor.policies.JsonSchemaPolicy;
import id.walt.auditor.policies.SignaturePolicy;
import id.walt.credentials.w3c.PresentableCredential;
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

        // Verify VPs, using Signature, JsonSchema and a custom policy
        var resVcJsonLd = Auditor.Companion.getService().verify(vcJsonLd, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));
        var resVcJwt = Auditor.Companion.getService().verify(vcJwt, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));

        System.out.println("JSON verification result: " + resVcJsonLd.getResult());
        System.out.println("JWT verification result: " + resVcJwt.getResult());

        // Present VC in JSON-LD and JWT format
        var vpJsonLd = custodian.createPresentation(List.of(new PresentableCredential(VerifiableCredential.Companion.fromString(vcJsonLd), null, false)), holder.getDid(), null, null, null, null);
        System.out.println("------------------------------- VP in JSON-LD format -------------------------------");
        System.out.println(vpJsonLd);

        var vpJwt = custodian.createPresentation(List.of(new PresentableCredential(VerifiableCredential.Companion.fromString(vcJwt), null, false)), holder.getDid(), null, null, null, null);
        System.out.println("\n------------------------------- VP in JWT format -------------------------------");
        System.out.println(vpJwt);

        // Verify VPs, using Signature, JsonSchema and a custom policy
        var resJsonLd = Auditor.Companion.getService().verify(vpJsonLd, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));
        var resJwt = Auditor.Companion.getService().verify(vpJwt, List.of(new SignaturePolicy(), new JsonSchemaPolicy(), new CustomPolicy()));

        System.out.println("JSON verification result: " + resJsonLd.getResult());
        System.out.println("JWT verification result: " + resJwt.getResult());
    }

    public ProofConfig createProofConfig(String issuerDid, String subjectDid, ProofType proofType, String dataProviderIdentifier) {
        return new ProofConfig(issuerDid, subjectDid, null, null, proofType, null, null,
                null, null, null, null, null, dataProviderIdentifier, null, null, Ecosystem.DEFAULT,
                null, "", "", null);
    }
}

class CustomPolicy extends SimpleVerificationPolicy {

    private String description = "A custom verification policy";

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    @NotNull
    @Override
    protected VerificationPolicyResult doVerify(@NotNull VerifiableCredential vc) {
        IdData idData = null;
        var result = false;
        if (vc.getType().get(vc.getType().size() - 1).equals("VerifiableId")) {
            if (vc.getCredentialSubject() != null) {
                idData = MockedIdDatabase.INSTANCE.get((String) vc.getCredentialSubject().getProperties().get("personalIdentifier"));
            }
            if (idData != null) {
                result = idData.getFamilyName().equals(vc.getCredentialSubject().getProperties().get("familyName"))
                        && idData.getFirstName().equals(vc.getCredentialSubject().getProperties().get("firstName"));
            }
        } else if (vc.getType().get(vc.getType().size() - 1).equals("VerifiablePresentation")) {
            // This custom policy does not verify the VerifiablePresentation
            result = true;
        }
        return result ? VerificationPolicyResult.Companion.success() : VerificationPolicyResult.Companion.failure(new Exception(""));
    }
}

class IdDataCustomProvider implements SignatoryDataProvider {

    @JsonIgnore
    private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_INSTANT;

    @NotNull
    @Override
    public W3CCredentialBuilder populate(@NotNull W3CCredentialBuilder credentialBuilder, @NotNull ProofConfig proofConfig) {
        if (credentialBuilder.getType().get(credentialBuilder.getType().size() - 1).equals("VerifiableId")) {

            var idData = ofNullable(proofConfig.getDataProviderIdentifier())
                    .map(MockedIdDatabase.INSTANCE::get)
                    .orElseThrow(() -> new RuntimeException("No ID data found for the given data-provider identifier"));

            return updateCustomVerifiableCredential(credentialBuilder, proofConfig, idData);
        } else {
            throw new IllegalArgumentException("Only VerifiableId is supported by this data provider");
        }
    }

    private W3CCredentialBuilder updateCustomVerifiableCredential(W3CCredentialBuilder credentialBuilder, ProofConfig proofConfig, IdData idData) {
        return credentialBuilder
                .setId("identity#verifiableID#" + UUID.randomUUID())
                .setIssuer(new W3CIssuer(proofConfig.getIssuerDid()))
                .setProperty("evidence", List.of(Map.of(
                        "verifier", proofConfig.getIssuerDid())
                ))
                .buildSubject(subjectBuilder -> {
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
                }).setIssuanceDate(
                        proofConfig.getIssueDate() != null ? Instant.parse(dateFormat.format(proofConfig.getIssueDate())) : Instant.now()
                )
                .setExpirationDate(
                        proofConfig.getExpirationDate() != null ? Instant.parse(dateFormat.format(proofConfig.getExpirationDate())) : Instant.now()
                )
                .setValidFrom(
                        proofConfig.getIssueDate() != null ? Instant.parse(dateFormat.format(proofConfig.getExpirationDate())) : Instant.now()
                );
    }
}