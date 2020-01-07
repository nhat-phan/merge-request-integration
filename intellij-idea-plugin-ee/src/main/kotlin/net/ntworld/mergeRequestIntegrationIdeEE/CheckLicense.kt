package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.ui.LicensingFacade
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.security.cert.*
import java.util.*

/**
 * @author Eugene Zhuravlev
 * Date: 26-Jul-18
 */
object CheckLicense {
    /**
     * PRODUCT_CODE must be the same specified in plugin.xml inside the <productCode> tag
    </productCode> */
    private const val PRODUCT_CODE = "PMRINTEGEE"
    private const val KEY_PREFIX = "key:"
    private const val STAMP_PREFIX = "stamp:"
    private const val EVAL_PREFIX = "eval:"
    /**
     * Public root certificates needed to verify JetBrains-signed licenses
     */
    private val ROOT_CERTIFICATES = arrayOf(
        "-----BEGIN CERTIFICATE-----\n" +
            "MIIFOzCCAyOgAwIBAgIJANJssYOyg3nhMA0GCSqGSIb3DQEBCwUAMBgxFjAUBgNV\n" +
            "BAMMDUpldFByb2ZpbGUgQ0EwHhcNMTUxMDAyMTEwMDU2WhcNNDUxMDI0MTEwMDU2\n" +
            "WjAYMRYwFAYDVQQDDA1KZXRQcm9maWxlIENBMIICIjANBgkqhkiG9w0BAQEFAAOC\n" +
            "Ag8AMIICCgKCAgEA0tQuEA8784NabB1+T2XBhpB+2P1qjewHiSajAV8dfIeWJOYG\n" +
            "y+ShXiuedj8rL8VCdU+yH7Ux/6IvTcT3nwM/E/3rjJIgLnbZNerFm15Eez+XpWBl\n" +
            "m5fDBJhEGhPc89Y31GpTzW0vCLmhJ44XwvYPntWxYISUrqeR3zoUQrCEp1C6mXNX\n" +
            "EpqIGIVbJ6JVa/YI+pwbfuP51o0ZtF2rzvgfPzKtkpYQ7m7KgA8g8ktRXyNrz8bo\n" +
            "iwg7RRPeqs4uL/RK8d2KLpgLqcAB9WDpcEQzPWegbDrFO1F3z4UVNH6hrMfOLGVA\n" +
            "xoiQhNFhZj6RumBXlPS0rmCOCkUkWrDr3l6Z3spUVgoeea+QdX682j6t7JnakaOw\n" +
            "jzwY777SrZoi9mFFpLVhfb4haq4IWyKSHR3/0BlWXgcgI6w6LXm+V+ZgLVDON52F\n" +
            "LcxnfftaBJz2yclEwBohq38rYEpb+28+JBvHJYqcZRaldHYLjjmb8XXvf2MyFeXr\n" +
            "SopYkdzCvzmiEJAewrEbPUaTllogUQmnv7Rv9sZ9jfdJ/cEn8e7GSGjHIbnjV2ZM\n" +
            "Q9vTpWjvsT/cqatbxzdBo/iEg5i9yohOC9aBfpIHPXFw+fEj7VLvktxZY6qThYXR\n" +
            "Rus1WErPgxDzVpNp+4gXovAYOxsZak5oTV74ynv1aQ93HSndGkKUE/qA/JECAwEA\n" +
            "AaOBhzCBhDAdBgNVHQ4EFgQUo562SGdCEjZBvW3gubSgUouX8bMwSAYDVR0jBEEw\n" +
            "P4AUo562SGdCEjZBvW3gubSgUouX8bOhHKQaMBgxFjAUBgNVBAMMDUpldFByb2Zp\n" +
            "bGUgQ0GCCQDSbLGDsoN54TAMBgNVHRMEBTADAQH/MAsGA1UdDwQEAwIBBjANBgkq\n" +
            "hkiG9w0BAQsFAAOCAgEAjrPAZ4xC7sNiSSqh69s3KJD3Ti4etaxcrSnD7r9rJYpK\n" +
            "BMviCKZRKFbLv+iaF5JK5QWuWdlgA37ol7mLeoF7aIA9b60Ag2OpgRICRG79QY7o\n" +
            "uLviF/yRMqm6yno7NYkGLd61e5Huu+BfT459MWG9RVkG/DY0sGfkyTHJS5xrjBV6\n" +
            "hjLG0lf3orwqOlqSNRmhvn9sMzwAP3ILLM5VJC5jNF1zAk0jrqKz64vuA8PLJZlL\n" +
            "S9TZJIYwdesCGfnN2AETvzf3qxLcGTF038zKOHUMnjZuFW1ba/12fDK5GJ4i5y+n\n" +
            "fDWVZVUDYOPUixEZ1cwzmf9Tx3hR8tRjMWQmHixcNC8XEkVfztID5XeHtDeQ+uPk\n" +
            "X+jTDXbRb+77BP6n41briXhm57AwUI3TqqJFvoiFyx5JvVWG3ZqlVaeU/U9e0gxn\n" +
            "8qyR+ZA3BGbtUSDDs8LDnE67URzK+L+q0F2BC758lSPNB2qsJeQ63bYyzf0du3wB\n" +
            "/gb2+xJijAvscU3KgNpkxfGklvJD/oDUIqZQAnNcHe7QEf8iG2WqaMJIyXZlW3me\n" +
            "0rn+cgvxHPt6N4EBh5GgNZR4l0eaFEV+fxVsydOQYo1RIyFMXtafFBqQl6DDxujl\n" +
            "FeU3FZ+Bcp12t7dlM4E0/sS1XdL47CfGVj4Bp+/VbF862HmkAbd7shs7sDQkHbU=\n" +
            "-----END CERTIFICATE-----\n",
        "-----BEGIN CERTIFICATE-----\n" +
            "MIIFTDCCAzSgAwIBAgIJAMCrW9HV+hjZMA0GCSqGSIb3DQEBCwUAMB0xGzAZBgNV\n" +
            "BAMMEkxpY2Vuc2UgU2VydmVycyBDQTAgFw0xNjEwMTIxNDMwNTRaGA8yMTE2MTIy\n" +
            "NzE0MzA1NFowHTEbMBkGA1UEAwwSTGljZW5zZSBTZXJ2ZXJzIENBMIICIjANBgkq\n" +
            "hkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAoT7LvHj3JKK2pgc5f02z+xEiJDcvlBi6\n" +
            "fIwrg/504UaMx3xWXAE5CEPelFty+QPRJnTNnSxqKQQmg2s/5tMJpL9lzGwXaV7a\n" +
            "rrcsEDbzV4el5mIXUnk77Bm/QVv48s63iQqUjVmvjQt9SWG2J7+h6X3ICRvF1sQB\n" +
            "yeat/cO7tkpz1aXXbvbAws7/3dXLTgAZTAmBXWNEZHVUTcwSg2IziYxL8HRFOH0+\n" +
            "GMBhHqa0ySmF1UTnTV4atIXrvjpABsoUvGxw+qOO2qnwe6ENEFWFz1a7pryVOHXg\n" +
            "P+4JyPkI1hdAhAqT2kOKbTHvlXDMUaxAPlriOVw+vaIjIVlNHpBGhqTj1aqfJpLj\n" +
            "qfDFcuqQSI4O1W5tVPRNFrjr74nDwLDZnOF+oSy4E1/WhL85FfP3IeQAIHdswNMJ\n" +
            "y+RdkPZCfXzSUhBKRtiM+yjpIn5RBY+8z+9yeGocoxPf7l0or3YF4GUpud202zgy\n" +
            "Y3sJqEsZksB750M0hx+vMMC9GD5nkzm9BykJS25hZOSsRNhX9InPWYYIi6mFm8QA\n" +
            "2Dnv8wxAwt2tDNgqa0v/N8OxHglPcK/VO9kXrUBtwCIfZigO//N3hqzfRNbTv/ZO\n" +
            "k9lArqGtcu1hSa78U4fuu7lIHi+u5rgXbB6HMVT3g5GQ1L9xxT1xad76k2EGEi3F\n" +
            "9B+tSrvru70CAwEAAaOBjDCBiTAdBgNVHQ4EFgQUpsRiEz+uvh6TsQqurtwXMd4J\n" +
            "8VEwTQYDVR0jBEYwRIAUpsRiEz+uvh6TsQqurtwXMd4J8VGhIaQfMB0xGzAZBgNV\n" +
            "BAMMEkxpY2Vuc2UgU2VydmVycyBDQYIJAMCrW9HV+hjZMAwGA1UdEwQFMAMBAf8w\n" +
            "CwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4ICAQCJ9+GQWvBS3zsgPB+1PCVc\n" +
            "oG6FY87N6nb3ZgNTHrUMNYdo7FDeol2DSB4wh/6rsP9Z4FqVlpGkckB+QHCvqU+d\n" +
            "rYPe6QWHIb1kE8ftTnwapj/ZaBtF80NWUfYBER/9c6To5moW63O7q6cmKgaGk6zv\n" +
            "St2IhwNdTX0Q5cib9ytE4XROeVwPUn6RdU/+AVqSOspSMc1WQxkPVGRF7HPCoGhd\n" +
            "vqebbYhpahiMWfClEuv1I37gJaRtsoNpx3f/jleoC/vDvXjAznfO497YTf/GgSM2\n" +
            "LCnVtpPQQ2vQbOfTjaBYO2MpibQlYpbkbjkd5ZcO5U5PGrQpPFrWcylz7eUC3c05\n" +
            "UVeygGIthsA/0hMCioYz4UjWTgi9NQLbhVkfmVQ5lCVxTotyBzoubh3FBz+wq2Qt\n" +
            "iElsBrCMR7UwmIu79UYzmLGt3/gBdHxaImrT9SQ8uqzP5eit54LlGbvGekVdAL5l\n" +
            "DFwPcSB1IKauXZvi1DwFGPeemcSAndy+Uoqw5XGRqE6jBxS7XVI7/4BSMDDRBz1u\n" +
            "a+JMGZXS8yyYT+7HdsybfsZLvkVmc9zVSDI7/MjVPdk6h0sLn+vuPC1bIi5edoNy\n" +
            "PdiG2uPH5eDO6INcisyPpLS4yFKliaO4Jjap7yzLU9pbItoWgCAYa2NpxuxHJ0tB\n" +
            "7tlDFnvaRnQukqSG+VqNWg==\n" +
            "-----END CERTIFICATE-----"
    )
    private const val SECOND: Long = 1000
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    private const val TIMESTAMP_VALIDITY_PERIOD_MS =
        1 * HOUR // configure period that suits your needs better// licensed via ticket obtained from JetBrains Floating License Server

    // the license is obtained via JetBrainsAccount or entered as an activation code
    val isLicensed: Boolean
        get() {
            val facade = LicensingFacade.getInstance() ?: return false
            val cstamp = facade.getConfirmationStamp(PRODUCT_CODE) ?: return false
            if (cstamp.startsWith(KEY_PREFIX)) { // the license is obtained via JetBrainsAccount or entered as an activation code
                return isKeyValid(cstamp.substring(KEY_PREFIX.length))
            }
            if (cstamp.startsWith(STAMP_PREFIX)) { // licensed via ticket obtained from JetBrains Floating License Server
                return isLicenseServerStampValid(cstamp.substring(STAMP_PREFIX.length))
            }
            return if (cstamp.startsWith(EVAL_PREFIX)) {
                isEvaluationValid(cstamp.substring(EVAL_PREFIX.length))
            } else false
        }

    private fun isEvaluationValid(expirationTime: String): Boolean {
        return try {
            val now = Date()
            val expiration = Date(expirationTime.toLong())
            now.before(expiration)
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isKeyValid(key: String): Boolean {
        val licenseParts = key.split("-").toTypedArray()
        if (licenseParts.size != 4) {
            return false // invalid format
        }
        val licenseId = licenseParts[0]
        val licensePartBase64 = licenseParts[1]
        val signatureBase64 = licenseParts[2]
        val certBase64 = licenseParts[3]
        try {
            val sig = Signature.getInstance("SHA1withRSA")
            // the last parameter of 'createCertificate()' set to 'false' switches off certificate expiration checks.
            // This might be the case if the key is at the same time a perpetual fallback license for older IDE versions.
            // Here it is only important that the key was signed with an authentic JetBrains certificate.
            sig.initVerify(
                createCertificate(
                    Base64.getMimeDecoder().decode(certBase64.toByteArray(StandardCharsets.UTF_8)),
                    emptySet(),
                    false
                )
            )
            val licenseBytes = Base64.getMimeDecoder()
                .decode(licensePartBase64.toByteArray(StandardCharsets.UTF_8))
            sig.update(licenseBytes)
            if (!sig.verify(Base64.getMimeDecoder().decode(signatureBase64.toByteArray(StandardCharsets.UTF_8)))) {
                return false
            }
            // Optional additional check: the licenseId corresponds to the licenseId encoded in the signed license data
            // The following is a 'least-effort' code. It would be more accurate to parse json and then find there the value of the attribute "licenseId"
            val licenseData = String(licenseBytes, Charset.forName("UTF-8"))
            return licenseData.contains("\"licenseId\":\"$licenseId\"")
        } catch (ignored: Throwable) {
        }
        return false
    }

    private fun isLicenseServerStampValid(serverStamp: String): Boolean {
        try {
            val parts = serverStamp.split(":").toTypedArray()
            val base64 = Base64.getMimeDecoder()
            val timeStamp = parts[0].toLong()
            val machineId = parts[1]
            val signatureType = parts[2]
            val signatureBytes =
                base64.decode(parts[3].toByteArray(StandardCharsets.UTF_8))
            val certBytes =
                base64.decode(parts[4].toByteArray(StandardCharsets.UTF_8))
            val intermediate: MutableCollection<ByteArray> =
                ArrayList()
            for (idx in 5 until parts.size) {
                intermediate.add(base64.decode(parts[idx].toByteArray(StandardCharsets.UTF_8)))
            }
            val sig = Signature.getInstance(signatureType)
            // the last parameter of 'createCertificate()' set to 'true' causes the certificate to be checked for
            // expiration. Expired certificates from a license server cannot be trusted
            sig.initVerify(createCertificate(certBytes, intermediate, true))
            sig.update((parts[0] + ":" + parts[1]).toByteArray(StandardCharsets.UTF_8))
            if (sig.verify(signatureBytes)) {
                val thisMachineId = PermanentInstallationID.get()
                // machineId must match the machineId from the server reply and
                // server reply should be relatively 'fresh'
                return thisMachineId == machineId && Math.abs(System.currentTimeMillis() - timeStamp) < TIMESTAMP_VALIDITY_PERIOD_MS
            }
        } catch (ignored: Throwable) { // consider serverStamp invalid
        }
        return false
    }

    @Throws(Exception::class)
    private fun createCertificate(
        certBytes: ByteArray,
        intermediateCertsBytes: Collection<ByteArray>,
        checkValidityAtCurrentDate: Boolean
    ): X509Certificate {
        val x509factory =
            CertificateFactory.getInstance("X.509")
        val cert =
            x509factory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
        val allCerts: MutableCollection<Certificate?> =
            HashSet()
        allCerts.add(cert)
        for (bytes in intermediateCertsBytes) {
            allCerts.add(x509factory.generateCertificate(ByteArrayInputStream(bytes)))
        }
        try { // Create the selector that specifies the starting certificate
            val selector = X509CertSelector()
            selector.certificate = cert
            // Configure the PKIX certificate builder algorithm parameters
            val trustAchors: MutableSet<TrustAnchor> = HashSet()
            for (rc in ROOT_CERTIFICATES) {
                trustAchors.add(
                    TrustAnchor(
                        x509factory.generateCertificate(ByteArrayInputStream(rc.toByteArray(StandardCharsets.UTF_8))) as X509Certificate,
                        null
                    )
                )
            }
            val pkixParams = PKIXBuilderParameters(trustAchors, selector)
            pkixParams.isRevocationEnabled = false
            if (!checkValidityAtCurrentDate) { // deliberately check validity on the start date of cert validity period, so that we do not depend on
                // the actual moment when the check is performed
                pkixParams.date = cert.notBefore
            }
            pkixParams.addCertStore(
                CertStore.getInstance("Collection", CollectionCertStoreParameters(allCerts))
            )
            // Build and verify the certification chain
            val path = CertPathBuilder.getInstance("PKIX").build(pkixParams).certPath
            if (path != null) {
                CertPathValidator.getInstance("PKIX").validate(path, pkixParams)
                return cert
            }
        } catch (e: Exception) { // debug the reason here
        }
        throw Exception("Certificate used to sign the license is not signed by JetBrains root certificate")
    }
}