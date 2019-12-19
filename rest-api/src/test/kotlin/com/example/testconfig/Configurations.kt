package com.example.testconfig

import com.example.testutils.resources.CodeSourceResourceBucket
import com.example.testutils.resources.CodeSourceResources

object TestConfigurations {
    val codeSourceResourcesLocation: String = CodeSourceResources
            .fileLocationAsString()
            .let {
                CodeSourceResources.replaceLocationSuffixes(
                        location = it,
                        oldSuffixes = listOf("/out/test/classes/","/classes/kotlin/test/"),
                        newSuffix = "/src/test/resources",
                        oldSuffixRequired = true
                )
            }
}


private fun foo() = TestConfigurations.codeSourceResourcesLocation
enum class CodeSourceResourceBuckets(val bucket: CodeSourceResourceBucket) {
    ROOT(
            bucket = CodeSourceResourceBucket(
                    qualifiedName = "", codeSourceLocation = TestConfigurations.codeSourceResourcesLocation
            )
    ),
    GOLDEN_TEST_DATA(
            bucket = CodeSourceResourceBucket(
                    qualifiedName = "/golden-test-data", codeSourceLocation = TestConfigurations.codeSourceResourcesLocation
            )
    )

    ;

}
