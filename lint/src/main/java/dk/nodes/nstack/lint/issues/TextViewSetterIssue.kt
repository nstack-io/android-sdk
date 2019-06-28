package dk.nodes.nstack.lint.issues

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

import dk.nodes.nstack.lint.NStackIssueDetector

object TextViewSetterIssue {


    private const val ID = "TextViewSetter"
    private const val DESCRIPTION = "Setting a TextView's text"
    private const val EXPLANATION = "This check highlights incorrect text property setting"
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
                    NStackIssueDetector::class.java,
                    Scope.JAVA_FILE_SCOPE))

}
