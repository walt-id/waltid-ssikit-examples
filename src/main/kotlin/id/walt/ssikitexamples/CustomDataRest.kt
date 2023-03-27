package id.walt.ssikitexamples

import id.walt.auditor.AuditorRestAPI
import id.walt.auditor.PolicyRegistry
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.templates.VcTemplateService
import id.walt.servicematrix.ServiceMatrix
import id.walt.signatory.rest.SignatoryRestAPI


fun main(){
    customDataRest()
}

fun customDataRest() {
    // Load walt.id SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    // Creating custom credential and set the data to be issued
    val myCustomCredential = VerifiableCredential.fromJson(customCredentialData)

    // Registering custom verification policy
    PolicyRegistry.register(MyCustomPolicy::class, "My custom policy")

    // Registering a custom Credential Template
    VcTemplateService.getService().register(customCredentialData::class.java.name, myCustomCredential)

    // Starting REST Services
    val bindAddress = "127.0.0.1"
    SignatoryRestAPI.start(7001, bindAddress)
    AuditorRestAPI.start(7003, bindAddress)

    println(" walt.id Signatory API: http://${bindAddress}:7001")
    println(" walt.id Auditor API:   http://${bindAddress}:7003")
}