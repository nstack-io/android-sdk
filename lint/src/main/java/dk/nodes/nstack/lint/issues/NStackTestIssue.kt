package dk.nodes.nstack.lint.issues

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

import dk.nodes.nstack.lint.NStackIssuesDetector

object NStackTestIssue {


    private const val ID = "TestIssue"
    private const val DESCRIPTION = "Lint Mentions"
    private const val EXPLANATION = "This check highlights string literals in code which mentions " +
            "the word `lint`. Blah blah blah.\n" +
            "\n" +
            "Another paragraph here.\n"
    private const val PRIORITY = 6

    private val SEVERITY = Severity.WARNING
    private val CATEGORY = Category.CORRECTNESS

    val ISSUE = Issue.create(
            ID,
            DESCRIPTION,
            EXPLANATION,
            CATEGORY,
            PRIORITY,
            SEVERITY,
            Implementation(
                    NStackIssuesDetector::class.java,
                    Scope.JAVA_FILE_SCOPE))

}
