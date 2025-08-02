package us.wprust.jvm.utils

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val title: String,
    val user: User,
    val coverData: ByteArray,
    val mature: Boolean
) {
    // The default equals and hashCode for data classes are sufficient
    // unless you have specific requirements. Overriding for ByteArray is correct.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Story
        if (title != other.title) return false
        if (user != other.user) return false
        if (mature != other.mature) return false
        if (!coverData.contentEquals(other.coverData)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + coverData.contentHashCode()
        result = 31 * result + mature.hashCode()
        return result
    }
}

@Serializable
data class User(
    val name: String
)