import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
    var remoteName: String,
    val md5: String,
    val fileSize: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileInfo

        if (remoteName.cn2os() != other.remoteName.cn2os()) return false
        if (md5 != other.md5) return false
        if (fileSize != other.fileSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remoteName.cn2os().hashCode()
        result = 31 * result + md5.hashCode()
        result = 31 * result + fileSize.hashCode()
        return result
    }

}


fun String.cn2os() = replace("YuanShen_Data/", "GenshinImpact_Data/")
fun String.os2cn() = replace("GenshinImpact_Data/", "YuanShen_Data/")