package com.example.testutils.resources

import com.example.util.resources.loadResource
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

object CodeSourceResources {
    /**
     * URI / URL ...
     * returns e.g.: file:<PATH_TO_PROJECT>/out/test/classes/
     * NOTE: ends with "/"
     */
    private fun locationURL(): URL = object {}.javaClass.protectionDomain.codeSource.location

    fun locationURI(): URI = locationURL().toURI()
    /**
     * File
     * returns e.g.: <PATH_TO_PROJECT>/out/test/classes/
     * NOTE: ends with "/"
     */
    fun fileLocationAsString(): String = locationURL().file

    fun replaceLocationSuffix(location: String, oldSuffix: String, newSuffix: String, oldSuffixRequired: Boolean): String {
        if ((oldSuffixRequired) && (!location.endsWith(oldSuffix))) {
            error(
                    "Can not replace oldSuffice with newSuffix in location string!" +
                            " reason: location must end with oldSuffix !" +
                            " oldSuffix (expected): $oldSuffix newSuffix: $newSuffix location: $location"
            )
        }
        return location
                .removeSuffix(oldSuffix)
                .let { "$it$newSuffix" }
    }

    fun writeTextFile(location: String, content: String, charset: Charset = Charsets.UTF_8): File = try {
        val file = File(location)
        file.writeText(content, charset)
        file
    } catch (all: Exception) {
        throw RuntimeException(
                "Failed to save text file! sink location: $location reason: ${all.message}", all
        )
    }

}

data class CodeSourceResourceBucket(
        val qualifiedName: String,
        val codeSourceLocation: String
) {
    init {
        if (qualifiedName.isNotEmpty()) {
            if (!qualifiedName.startsWith("/")) {
                error("qualifiedName must start with '/' ! given: $qualifiedName")
            }
            if (qualifiedName.endsWith("/")) {
                error("qualifiedName must not end with '/' ! given: $qualifiedName")
            }
        }
        if (codeSourceLocation.endsWith("/")) {
            error("codeSourceLocation must not end with '/' ! given: $codeSourceLocation")
        }
    }

    val location: String = "$codeSourceLocation$qualifiedName"

    fun withQualifiedName(qualifiedName: (CodeSourceResourceBucket) -> String): CodeSourceResourceBucket =
            copy(qualifiedName = qualifiedName(this))
}

fun CodeSourceResourceBucket.loadResourceText():String = loadResource(qualifiedName)

