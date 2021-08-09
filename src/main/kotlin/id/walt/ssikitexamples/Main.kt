package id.walt.ssikitexamples

import id.walt.servicematrix.ServiceMatrix
import id.walt.services.vc.VCService
import id.walt.signatory.Signatory

// Define used services
val signatory = Signatory.getService()
val credentialService = VCService.getService()

fun main() {
    // Load Walt.ID SSI-Kit services from "$workingDirectory/service-matrix.properties"
    ServiceMatrix("service-matrix.properties")

    /* Use services... */

    // List registered VC templates
    signatory.listTemplates().forEachIndexed { index, templateName ->
        println("$index: $templateName")
    }

    // Get default VC template from VCService
    println("Default VC template: ${credentialService.defaultVcTemplate()}")
}
