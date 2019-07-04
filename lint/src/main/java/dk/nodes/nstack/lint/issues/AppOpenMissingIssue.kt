package dk.nodes.nstack.lint.issues

import com.android.tools.lint.detector.api.*
import dk.nodes.nstack.lint.NStackIssueDetector

object AppOpenMissingIssue {

    private const val ID = "AppOpenMissing"
    private const val DESCRIPTION = "Detects whether or not appOpen() call was used"
    private const val EXPLANATION = "appOpen() call was not used in any Activity/Fragment"
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
                    NStackIssueDetector::class.java,
                    Scope.JAVA_FILE_SCOPE))


}