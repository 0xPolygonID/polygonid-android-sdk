package technology.polygon.polygonid_android_sdk

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.Iden3MessageEntity
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.Iden3MessageType
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth.AuthBodyRequest

class AuthIden3MessageEntityTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize AuthIden3MessageEntity from JSON`() {
        val jsonString = """
            {
                "id": "1a71397b-73a4-481d-acaf-bdedb5f839f6",
                "typ": "application/iden3comm-plain-json",
                "type": "https://iden3-communication.io/authorization/1.0/request",
                "thid": "1a71397b-73a4-481d-acaf-bdedb5f839f6",
                "body": {
                    "callbackUrl": "https://self-hosted-testing-testnet-backend-platform.polygonid.me/api/callback?sessionId=340404",
                    "reason": "test flow",
                    "scope": []
                },
                "from": "did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9"
            }
        """.trimIndent()

        val expected = Iden3MessageEntity.AuthIden3MessageEntity(
            id = "1a71397b-73a4-481d-acaf-bdedb5f839f6",
            typ = "application/iden3comm-plain-json",
            type = "https://iden3-communication.io/authorization/1.0/request",
            messageType = Iden3MessageType.auth,
            thid = "1a71397b-73a4-481d-acaf-bdedb5f839f6",
            body = AuthBodyRequest(
                callbackUrl = "https://self-hosted-testing-testnet-backend-platform.polygonid.me/api/callback?sessionId=340404",
                reason = "test flow",
                scope = emptyList(),
                message = null,
                url = null,
                credentials = null,
            ),
            from = "did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9"
        )

        val actual = json.decodeFromString<Iden3MessageEntity.AuthIden3MessageEntity>(jsonString)

        assertEquals(expected, actual)
    }
}