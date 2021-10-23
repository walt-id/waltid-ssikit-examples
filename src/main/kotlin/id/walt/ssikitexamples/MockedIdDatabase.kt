package id.walt.ssikitexamples

import id.walt.model.DidMethod
import id.walt.services.did.DidService

class IdData(
    val did: String,
    val familyName: String,
    val firstName: String,
    val dateOfBirth: String,
    val personalIdentifier: String,
    val nameAndFamilyNameAtBirth: String,
    val placeOfBirth: String,
    val currentAddress: String,
    val gender: String
) {}

object MockedIdDatabase {
    var mockedIds: Map<String, IdData>

    init {
        // generate id data
        val did1 = DidService.create(DidMethod.key)
        val did2 = DidService.create(DidMethod.key)

        val personalIdentifier1 = "0904008084H"
        val personalIdentifier2 = "0905108984G"

        mockedIds = mapOf(
            Pair(
                personalIdentifier1, IdData(
                    did1,
                    "DOE",
                    "Jane",
                    "1993-04-08",
                    personalIdentifier1,
                    "Jane DOE",
                    "LILLE, FRANCE",
                    "1 Boulevard de la Liberté, 59800 Lille",
                    "FEMALE"
                )
            ),
            Pair(
                personalIdentifier2, IdData(
                    did2,
                    "JAMES",
                    "Chris",
                    "1994-02-18",
                    personalIdentifier2,
                    "Christ JAMES",
                    "VIENNA, AUSTRIA",
                    "Mariahilferstraße 100, 1070 Wien",
                    "MALE"
                )
            )
        )
    }

    fun get(identifier: String): IdData? {
        return mockedIds[identifier]
    }
}
