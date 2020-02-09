package net.ntworld.mergeRequestIntegration.provider

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.ntworld.mergeRequest.api.ApiCredentials
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

abstract class FuelClient (
    private val credentials: ApiCredentials
) {
    val json = Json(JsonConfiguration.Stable.copy(strictMode = false))

    protected abstract fun injectAuthentication(httpRequest: Request): Request

    protected fun makeRequestFactory(): RequestFactory.Convenience {
        if (credentials.ignoreSSLCertificateErrors) {
            return FuelManager().apply {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                })

                socketFactory = SSLContext.getInstance("SSL").apply {
                    init(null, trustAllCerts, java.security.SecureRandom())
                }.socketFactory

                hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
        }
        return FuelManager()
    }

    fun postJson(url: String, parameters: Parameters? = null): String {
        return executeRequest(makeRequestFactory().post(url, parameters))
    }

    fun deleteJson(url: String, parameters: Parameters? = null): String {
        return executeRequest(makeRequestFactory().delete(url, parameters))
    }

    fun putJson(url: String, parameters: Parameters? = null): String {
        return executeRequest(makeRequestFactory().put(url, parameters))
    }

    fun getJson(url: String, parameters: Parameters? = null): String {
        return executeRequest(makeRequestFactory().get(url, parameters))
    }

    protected fun executeRequest(request: Request) : String {
        val httpRequest = injectAuthentication(request)
        val (_, response, result) = httpRequest.responseString()
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                throw HttpException(response.statusCode, result.error.message ?: "Unknown")
            }
        }
    }

}
