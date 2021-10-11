# Walt.ID SSI-Kit example project

This repository demonstrates several usage examples of the Walt.ID SSI-Kit project.

### Setup

Build with **Gradle**

    gradle build

Build with **Maven**

    mvn install

### Examples

- **KeyManagement** - shows how to generate, to import/export and to delete cryptographic keys.
- **Credentials** - shows how to generate JSON_LD as well as JWT credentials.
- **CustomData** - shows how to integrate a custom-data provider for populating Verifiable Credentials with data, as well as adding a custom policy for verifying Verifiable Presentations.
- **JsonLdCredentials** - shows how to utilize the low-level credential-service interface for signing JSON_LD credentials.
- **DidEbsi** - shows the creation and anchoring of a **did:ebsi** on the EBSI ledger.
