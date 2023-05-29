package technology.polygon.polygonid_android_sdk

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.Iden3MessageEntity
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.Iden3MessageType
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth.AuthBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.fetch.FetchBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.offer.CredentialOfferData
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.offer.OfferBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.onchain.ContractFunctionCallBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.onchain.ContractFunctionCallBodyTxDataRequest

class Iden3MessageEntityTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test AuthIden3MessageEntity serialization`() {
        val authMessage = Iden3MessageEntity.AuthIden3MessageEntity(
            id = "123",
            typ = "typ",
            type = "type",
            messageType = Iden3MessageType.auth,
            thid = "thid",
            body = AuthBodyRequest(
                "address",
                "signature",
                "publicKey",
                null,
                url = "url",
                credentials = null,
            ),
            from = "from",
            to = "to"
        )
        val jsonStr = json.encodeToString(authMessage)
        val expectedJsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"auth\",\"thid\":\"thid\",\"body\":{\"address\":\"address\",\"signature\":\"signature\",\"publicKey\":\"publicKey\",\"url\":\"url\"},\"from\":\"from\",\"to\":\"to\"}"
        assertEquals(expectedJsonStr, jsonStr)
    }

    @Test
    fun `test AuthIden3MessageEntity deserialization`() {
        val jsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"auth\",\"thid\":\"thid\",\"body\":{\"address\":\"address\",\"signature\":\"signature\",\"publicKey\":\"publicKey\",\"url\":\"url\"},\"from\":\"from\",\"to\":\"to\"}"
        val authMessage =
            json.decodeFromString<Iden3MessageEntity>(jsonStr) as Iden3MessageEntity.AuthIden3MessageEntity
        assertEquals("123", authMessage.id)
        assertEquals("typ", authMessage.typ)
        assertEquals("type", authMessage.type)
        assertEquals(Iden3MessageType.auth, authMessage.messageType)
        assertEquals("thid", authMessage.thid)
        assertEquals(
            AuthBodyRequest(
                "address",
                "signature",
                "publicKey",
                null,
                url = "url",
                credentials = null,
            ), authMessage.body
        )
        assertEquals("from", authMessage.from)
        assertEquals("to", authMessage.to)
    }

    @Test
    fun `test FetchIden3MessageEntity serialization`() {
        val fetchMessage = Iden3MessageEntity.FetchIden3MessageEntity(
            id = "123",
            typ = "typ",
            type = "type",
            messageType = Iden3MessageType.issuance,
            thid = "thid",
            body = FetchBodyRequest(
                "address",
            ),
            from = "from",
            to = "to"
        )
        val jsonStr = json.encodeToString(fetchMessage)
        val expectedJsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"issuance\",\"thid\":\"thid\",\"body\":{\"address\":\"address\"},\"from\":\"from\",\"to\":\"to\"}"
        assertEquals(expectedJsonStr, jsonStr)
    }

    @Test
    fun `test FetchIden3MessageEntity deserialization`() {
        val jsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"issuance\",\"thid\":\"thid\",\"body\":{\"address\":\"address\"},\"from\":\"from\",\"to\":\"to\"}"
        val fetchMessage =
            json.decodeFromString<Iden3MessageEntity>(jsonStr) as Iden3MessageEntity.FetchIden3MessageEntity
        assertEquals("123", fetchMessage.id)
        assertEquals("typ", fetchMessage.typ)
        assertEquals("type", fetchMessage.type)
        assertEquals(Iden3MessageType.issuance, fetchMessage.messageType)
        assertEquals("thid", fetchMessage.thid)
        assertEquals(
            FetchBodyRequest(
                "address",
            ), fetchMessage.body
        )
        assertEquals("from", fetchMessage.from)
        assertEquals("to", fetchMessage.to)
    }

    @Test
    fun `test OfferIden3MessageEntity serialization`() {
        val offerMessage = Iden3MessageEntity.OfferIden3MessageEntity(
            id = "123",
            typ = "typ",
            type = "type",
            messageType = Iden3MessageType.offer,
            thid = "thid",
            body = OfferBodyRequest(
                "address",
                credentials = listOf(CredentialOfferData("type", "value")),
            ),
            from = "from",
            to = "to"
        )
        val jsonStr = json.encodeToString(offerMessage)
        val expectedJsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"offer\",\"thid\":\"thid\",\"body\":{\"address\":\"address\",\"credentials\":[{\"type\":\"type\",\"value\":\"value\"}]},\"from\":\"from\",\"to\":\"to\"}"
        assertEquals(expectedJsonStr, jsonStr)
    }

    @Test
    fun `test OfferIden3MessageEntity deserialization`() {
        val jsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"offer\",\"thid\":\"thid\",\"body\":{\"address\":\"address\",\"credentials\":[{\"type\":\"type\",\"value\":\"value\"}]},\"from\":\"from\",\"to\":\"to\"}"
        val offerMessage =
            json.decodeFromString<Iden3MessageEntity>(jsonStr) as Iden3MessageEntity.OfferIden3MessageEntity
        assertEquals("123", offerMessage.id)
        assertEquals("typ", offerMessage.typ)
        assertEquals("type", offerMessage.type)
        assertEquals(Iden3MessageType.offer, offerMessage.messageType)
        assertEquals("thid", offerMessage.thid)
        assertEquals(
            OfferBodyRequest(
                "address",
                credentials = listOf(CredentialOfferData("type", "value")),
            ), offerMessage.body
        )
        assertEquals("from", offerMessage.from)
        assertEquals("to", offerMessage.to)
    }

    @Test
    fun `test ContractFunctionCallIden3MessageEntity serialization`() {
        val contractFunctionCallMessage = Iden3MessageEntity.ContractFunctionCallIden3MessageEntity(
            id = "123",
            typ = "typ",
            type = "type",
            messageType = Iden3MessageType.contractFunctionCall,
            thid = "thid",
            body = ContractFunctionCallBodyRequest(
                transactionData = ContractFunctionCallBodyTxDataRequest(
                    "address",
                    "method",
                    1,
                    "network",
                ),
                reason = "reason",
                scope = null,
            ),
            from = "from",
            to = "to"
        )
        val jsonStr = json.encodeToString(contractFunctionCallMessage)
        val expectedJsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"contractFunctionCall\",\"thid\":\"thid\",\"body\":{\"transactionData\":{\"address\":\"address\",\"method\":\"method\",\"nonce\":1,\"network\":\"network\"},\"reason\":\"reason\"},\"from\":\"from\",\"to\":\"to\"}"
        assertEquals(expectedJsonStr, jsonStr)
    }

    @Test
    fun `test ContractFunctionCallIden3MessageEntity deserialization`() {
        val jsonStr =
            "{\"id\":\"123\",\"typ\":\"typ\",\"type\":\"type\",\"messageType\":\"contractFunctionCall\",\"thid\":\"thid\",\"body\":{\"transactionData\":{\"address\":\"address\",\"method\":\"method\",\"nonce\":1,\"network\":\"network\"},\"reason\":\"reason\"},\"from\":\"from\",\"to\":\"to\"}"
        val contractFunctionCallMessage =
            json.decodeFromString<Iden3MessageEntity>(jsonStr) as Iden3MessageEntity.ContractFunctionCallIden3MessageEntity
        assertEquals("123", contractFunctionCallMessage.id)
        assertEquals("typ", contractFunctionCallMessage.typ)
        assertEquals("type", contractFunctionCallMessage.type)
        assertEquals(Iden3MessageType.contractFunctionCall, contractFunctionCallMessage.messageType)
        assertEquals("thid", contractFunctionCallMessage.thid)
        assertEquals(ContractFunctionCallBodyRequest(
            transactionData = ContractFunctionCallBodyTxDataRequest(
                "address",
                "method",
                1,
                "network",
            ),
            reason = "reason",
            scope = null,
        ), contractFunctionCallMessage.body)
        assertEquals("from", contractFunctionCallMessage.from)
        assertEquals("to", contractFunctionCallMessage.to)
    }
}