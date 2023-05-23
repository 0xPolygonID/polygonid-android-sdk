package technology.polygon.polygonid_android_sdk.proof.domain.entities

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadInfoType { onDone, onError, onProgress }

@Serializable
sealed class DownloadInfoEntity {
    abstract val downloadInfoType: DownloadInfoType

    @Serializable
    data class DownloadInfoOnDone(
        val contentLenght: Int,
        val downloaded: Int,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onDone,
    ) : DownloadInfoEntity()

    @Serializable
    data class DownloadInfoOnProgress(
        val contentLenght: Int,
        val downloaded: Int,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onProgress,
    ) : DownloadInfoEntity()

    @Serializable
    data class DownloadInfoOnError(
        val errorMessage: String,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onError,
    ) : DownloadInfoEntity()
}
