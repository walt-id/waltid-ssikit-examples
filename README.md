# Walt.ID SSI-Kit example project

This repository demonstrates several usage examples of the Walt.ID SSI-Kit project.

### Setup

Build with **Gradle**

    gradle build

Build with **Maven**

    mvn install

### Examples

- **KeyManagement** - shows how to generate, to import/export and to delete cryptographic keys.
- **JsonLdCredentials** - shows the creation of DIDs as well as the issuance/verification of JSON LD based credentials.
- **CustomData** - shows how to integrate a custom-data provider for populating Verifiable Credentials with data, as well as adding a custom policy for verifying Verifiable Presentations.
- **DidEbsi** - shows the creation and anchoring of a **did:ebsi** on the EBSI ledger.
