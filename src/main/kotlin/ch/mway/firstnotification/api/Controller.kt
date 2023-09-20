package ch.mway.firstnotification.api

import ch.mway.firstnotification.data.ChatMessage
import com.microsoft.azure.functions.signalr.SignalRConnectionInfo
import com.microsoft.azure.functions.signalr.SignalRMessage
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kong.unirest.Unirest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.spec.SecretKeySpec


@RestController
class Controller {
    @PostMapping("/signalr/negotiate")
    fun negotiate(): SignalRConnectionInfo {
        val signalRServiceBaseEndpoint = "mwaytrial.service.signalr.net"
        val hubName = "notification"
        val userId = "12345"
        val hubUrl = "$signalRServiceBaseEndpoint/client/?hub=$hubName"
        val accessKey: String = generateJwt(hubUrl, userId)

        val signalRConnectionInfo = SignalRConnectionInfo()
        signalRConnectionInfo.url = hubUrl
        signalRConnectionInfo.accessToken = accessKey

        return signalRConnectionInfo
    }

    @PostMapping("/api/messages")
    fun sendMessage(@RequestBody message : ChatMessage) {
        val signalRServiceBaseEndpoint = "mwaytrial.service.signalr.net"
        val hubName = "notification"

        val hubUrl = "$signalRServiceBaseEndpoint/api/v1/hubs/$hubName"
        val accessKey : String  = generateJwt(hubUrl, null)

        Unirest.post(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessKey")
            .body(SignalRMessage("newMessage", listOf(message)))
            .asEmpty()
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