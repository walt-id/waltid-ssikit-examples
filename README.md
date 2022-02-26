# walt.id SSI-Kit example project
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=security_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=ncloc)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples)

[comment]: <> ([![Technical Debt]&#40;https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=sqale_index&#41;]&#40;https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples&#41;)

[comment]: <> ([![Bugs]&#40;https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=bugs&#41;]&#40;https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples&#41;)

[comment]: <> ([![Duplicated Lines &#40;%&#41;]&#40;https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=duplicated_lines_density&#41;]&#40;https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples&#41;)

[comment]: <> ([![Quality Gate Status]&#40;https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=alert_status&#41;]&#40;https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit-examples&#41;)


This repository demonstrates several usage examples of the walt.id SSI-Kit project.

### Setup

Build with **Gradle**

    gradle build

Build with **Maven**

    mvn install

### Examples

- **KeyManagement** - shows how to generate, to import/export and to delete cryptographic keys.
- **Credentials** - shows how to generate JSON_LD as well as JWT credentials.
- **CustomCredential** - shows how to add and to en-/decode a custom credential.
- **CustomData** - shows how to integrate a custom-data provider for populating Verifiable Credentials with data, as well as adding a custom policy for verifying Verifiable Presentations.
- **CustomDataRest** - shows how to launch a RESTfull issuance- & verification-service using a custom data provider, custom credential template and custom verification policy.
- **CustomVcTemplate** - shows how to add custom data to default VC template
- **JsonLdCredentials** - shows how to utilize the low-level credential-service interface for signing JSON_LD credentials.
- **DidEbsi** - shows the creation and anchoring of a **did:ebsi** on the EBSI ledger.
