package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.onchain

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.ProofScopeRequestEntity

@Serializable
data class ContractFunctionCallBodyRequest(
    val transactionData: ContractFunctionCallBodyTxDataRequest,
    val reason: String?,
    val scope: List<ProofScopeRequestEntity>?,
)
