package id.walt.ssikitexamples

import id.walt.signatory.ProofConfig
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.VerifiableIdDataProvider
import id.walt.signatory.dateFormat
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableId
import java.util.*

class CustomIdDataProvider : SignatoryDataProvider {
    override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        if(template is VerifiableId) {
            // get ID data for the given subject
            val idData = MockedIdDatabase.get(proofConfig.dataProviderIdentifier!!)
            if(idData == null) {
                throw Exception("No ID data found for the given subject did")
            }
            template.id = proofConfig.credentialId ?: "identity#verifiableID#${UUID.randomUUID()}"
            template.issuer = proofConfig.issuerDid
            if (proofConfig.issueDate != null) template.issuanceDate = dateFormat.format(proofConfig.issueDate)
            if (proofConfig.expirationDate != null) template.expirationDate = dateFormat.format(proofConfig.expirationDate)
            template.validFrom = template.issuanceDate
            template.evidence!!.verifier = proofConfig.issuerDid
            template.credentialSubject = VerifiableId.CredentialSubject(
                idData.did,
                idData.familyName,
                idData.firstName,
                idData.dateOfBirth,
                idData.personalIdentifier,
                idData.nameAndFamilyNameAtBirth,
                idData.placeOfBirth,
                idData.currentAddress,
                idData.gender
            )
            return template
        } else {
            throw IllegalArgumentException("Only VerifiableId is supported by this data provider")
        }
    }
}
