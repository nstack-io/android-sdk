package dk.nodes.nstack.lint.issues

import com.android.tools.lint.detector.api.*
import dk.nodes.nstack.lint.NStackIssuesDetector


object NStackHardcodedIssue {
    private const val ID = "NStackHardCodedIssue"
    private const val DESCRIPTION = "Detects nstack environment hardcoded in manifest"
    private const val EXPLANATION = "NStack environment is hardcoded in manifest"
    private const val PRIORITY = 7

    private val SEVERITY = Severity.ERROR
    private val CATEGORY = Category.USABILITY

    val ISSUE = Issue.create(
            ID,
            DESCRIPTION,
            EXPLANATION,
            CATEGORY,
            PRIORITY,
            SEVERITY,
            Implementation(
                    NStackIssuesDetector::class.java,
                    Scope.MANIFEST_SCOPE))

}