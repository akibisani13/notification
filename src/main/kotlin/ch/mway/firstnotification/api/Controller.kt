package ch.mway.firstnotification.api

import com.microsoft.azure.functions.signalr.SignalRConnectionInfo
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.spec.SecretKeySpec


@RestController
@RequestMapping("/signalr")
class Controller {

    @PostMapping("/negotiate")
    fun negotiate(): SignalRConnectionInfo {
        val signalRServiceBaseEndpoint = "mwaytrial.service.signalr.net"
        val hubName = "isani"
        val userId = "1313"
        val hubUrl = "$signalRServiceBaseEndpoint/client/?hub=$hubName"
        val accessKey: String = generateJwt(hubUrl, userId)

        val signalRConnectionInfo = SignalRConnectionInfo()
        signalRConnectionInfo.url = hubUrl
        signalRConnectionInfo.accessToken = accessKey

        return signalRConnectionInfo
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