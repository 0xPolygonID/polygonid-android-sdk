package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.onchain

import kotlinx.serialization.Serializable

@Serializable
data class ContractFunctionCallBodyTxDataRequest(
    val contractAddress: String, // required, ethereum contract address format
    val methodId: String, // required, hex string, ethereum function selector
    val chainId: Int,  // required, number of chain.
    val network: String?
)
