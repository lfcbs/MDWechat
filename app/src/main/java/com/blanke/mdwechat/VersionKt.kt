package com.blanke.mdwechat

// Version is a helper class for comparing version strings.
class VersionKt(private val versionName: String) {

    val version: List<Int> = versionName.split('.').mapNotNull(String::toIntOrNull)

    override fun toString() = versionName

    override fun hashCode(): Int = version.hashCode()

    override fun equals(other: Any?): Boolean = when (other) {
        null -> false
        !is VersionKt -> false
        else -> this.version == other.version
    }

    operator fun compareTo(other: VersionKt): Int {
        var result = 0
        when {
            this.version.size > other.version.size -> result = 1
            this.version.size < other.version.size -> result = -1
        }

        var index = 0
        while (index < this.version.size && index < other.version.size) {
            when {
                this.version[index] > other.version[index] -> return 1
                this.version[index] < other.version[index] -> return -1
            }
            index++
        }

        return result
    }
}
