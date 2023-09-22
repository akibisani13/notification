package ch.mway.firstnotification.api

import ch.mway.firstnotification.data.ChatMessage
import com.microsoft.azure.functions.signalr.SignalRConnectionInfo
import com.microsoft.azure.functions.signalr.SignalRMessage
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.spec.SecretKeySpec


@RestController
@CrossOrigin(origins = [
    "http://localhost:4200",
    "http://localhost:61219"
])
class Controller(private val restTemplate: RestTemplate) {
    companion object {
        const val SIGNALR_SERVICE_BASE_ENDPOINT = "https://mwaytrial.service.signalr.net"
        const val HUB_NAME = "notification"
        const val SIGNALR_SERVICE_KEY = "9tFdHphXIZFBWw+2jDW+t5Mqm87ZNrevECe7BSN8wUM="
    }

    @PostMapping("/signalr/negotiate")
    fun negotiate(
        @RequestParam("userId") userId: String
    ): SignalRConnectionInfo
    {
        val hubUrl = "$SIGNALR_SERVICE_BASE_ENDPOINT/client/?hub=$HUB_NAME"
        val accessKey = generateJwt(hubUrl, userId)

        val signalRConnectionInfo = SignalRConnectionInfo()
        signalRConnectionInfo.url = hubUrl
        signalRConnectionInfo.accessToken = accessKey

        return signalRConnectionInfo
    }

    @PostMapping("/api/messages")
    fun sendMessage(
        @RequestParam("userId") userId: String,
        @RequestBody message: ChatMessage): HttpStatusCode
    {
        val hubUrl = "$SIGNALR_SERVICE_BASE_ENDPOINT/api/v1/hubs/$HUB_NAME/users/$userId"
        val accessKey = generateJwt(hubUrl, userId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $accessKey")

        val signalRMessage = SignalRMessage("newMessage", listOf(message))
        val requestEntity = HttpEntity(signalRMessage, headers)

        return try {
            restTemplate.exchange(
                hubUrl,
                HttpMethod.POST,
                requestEntity,
                Void::class.java
            ).statusCode
        } catch (e: Exception) {
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    @PostMapping("/api/adduser")
    fun addUser(
        @RequestParam("userId") userId: String,
        @RequestParam("groupName") groupName: String
    ): HttpStatusCode {
        val hubUrl = "$SIGNALR_SERVICE_BASE_ENDPOINT/api/v1/hubs/$HUB_NAME/groups/$groupName/users/$userId"
        val accessKey = generateJwt(hubUrl, userId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $accessKey")

        val requestEntity = HttpEntity(null ,headers)

        return try {
            restTemplate.exchange(
                hubUrl,
                HttpMethod.PUT,
                requestEntity,
                Void::class.java
            ).statusCode
        } catch (e: Exception) {
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    @PostMapping("/api/groupmsgs")
    fun sendGroupMessage(
        @RequestParam("groupName") groupName: String,
        @RequestBody message: ChatMessage): HttpStatusCode
    {
        val hubUrl = "$SIGNALR_SERVICE_BASE_ENDPOINT/api/v1/hubs/$HUB_NAME/groups/$groupName"
        val accessKey = generateJwt(hubUrl, null)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $accessKey")

        val signalRMessage = SignalRMessage("newMessage", listOf(message))
        val requestEntity = HttpEntity(signalRMessage, headers)

        return try {
            restTemplate.exchange(
                hubUrl,
                HttpMethod.POST,
                requestEntity,
                Void::class.java
            ).statusCode
        } catch (e: Exception) {
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun generateJwt(audience: String, userId: String?): String {
        val nowMillis = System.currentTimeMillis()
        val now = Date(nowMillis)
        val expMillis = nowMillis + 30 * 60 * 1000
        val exp = Date(expMillis)

        val apiKeySecretBytes = SIGNALR_SERVICE_KEY.toByteArray(StandardCharsets.UTF_8)
        val signingKey = SecretKeySpec(
            apiKeySecretBytes,
            SignatureAlgorithm.HS256.jcaName
        )

        val builder: JwtBuilder = Jwts.builder()
            .setAudience(audience)
            .setIssuedAt(now)
            .setExpiration(exp)
            .setSubject(userId)
            .signWith(signingKey)

        return builder.compact()
    }
}