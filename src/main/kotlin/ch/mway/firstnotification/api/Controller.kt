package ch.mway.firstnotification.api

import ch.mway.firstnotification.data.ChatMessage
import com.microsoft.azure.functions.signalr.SignalRConnectionInfo
import com.microsoft.azure.functions.signalr.SignalRMessage
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kong.unirest.Unirest
import org.springframework.http.*
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.spec.SecretKeySpec


@RestController
class Controller (
    val restTemplate: RestTemplate
){
    @PostMapping("/signalr/negotiate")
    fun negotiate(
        @RequestParam ("userId") userId: String,
    ): SignalRConnectionInfo
    {
        val signalRServiceBaseEndpoint = "https://mwaytrial.service.signalr.net"
        val hubName = "notification"
        val hubUrl = "$signalRServiceBaseEndpoint/client/?hub=$hubName"
        val accessKey: String = generateJwt(hubUrl, userId)

        val signalRConnectionInfo = SignalRConnectionInfo()
        signalRConnectionInfo.url = hubUrl
        signalRConnectionInfo.accessToken = accessKey

        return signalRConnectionInfo
    }

    @PostMapping("/api/messages")
    fun sendMessage(
        @RequestParam ("userId") userId: String,
        @RequestBody message : ChatMessage
    ): HttpStatusCode
    {
        val scheme = "https"
        val signalRServiceBaseEndpoint = "mwaytrial.service.signalr.net"
        val hubName = "notification"

        val hubUrl = UriComponentsBuilder.newInstance()
            .scheme(scheme)
            .host(signalRServiceBaseEndpoint)
            .path("/api/v1/hubs/$hubName")
            .toUriString()

        val accessKey : String  = generateJwt(hubUrl, "12345")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $accessKey")

        val signalRMessage = SignalRMessage("newMessage", listOf(message))
        val requestEntity = HttpEntity(signalRMessage, headers)

        return restTemplate.exchange(
            hubUrl,
            HttpMethod.POST,
            requestEntity,
            Void::class.java
        ).statusCode
    }

    private fun generateJwt(audience: String, userId: String?): String {

        val signalRServiceKey = "9tFdHphXIZFBWw+2jDW+t5Mqm87ZNrevECe7BSN8wUM="
        val nowMillis = System.currentTimeMillis()
        val now = Date(nowMillis)
        val expMillis = nowMillis + 30 * 60 * 1000
        val exp = Date(expMillis)

        val apiKeySecretBytes = signalRServiceKey.byteInputStream(StandardCharsets.UTF_8).readBytes()
        val signatureAlgorithm: SignatureAlgorithm = SignatureAlgorithm.HS256
        val signingKey = SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.jcaName)

        val builder: JwtBuilder = Jwts.builder()
            .setAudience(audience)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(signingKey)

        if (userId != null) {
            builder.claim("nameid", userId)
        }
        return builder.compact()
    }
}