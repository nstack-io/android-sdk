package dk.nodes.nstack.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import dk.nodes.nstack.lint.issues.NStackTestIssue
import dk.nodes.nstack.lint.issues.TextViewSetterIssue
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getValueIfStringLiteral

class NStackIssueDetector : Detector(), Detector.UastScanner {


    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(ULiteralExpression::class.java)
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("setText")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val methodName = node.methodName
        if (methodName == "setText") {
            val caller = node.receiverType ?: return
            if (caller.canonicalText == "android.widget.TextView") {
                context.report(TextViewSetterIssue.ISSUE, context.getLocation(node), "Found text view, fix setter")
            }
        }
    }


    override fun createUastHandler(context: JavaContext): UElementHandler? {
        // Note: Visiting UAST nodes is a pretty general purpose mechanism;
        // Lint has specialized support to do common things like "visit every class
        // that extends a given super class or implements a given interface", and
        // "visit every call site that calls a method by a given name" etc.
        // Take a careful look at UastScanner and the various existing lint check
        // implementations before doing things the "hard way".
        // Also be aware of context.getJavaEvaluator() which provides a lot of
        // utility functionality.
        return object : UElementHandler() {
            override fun visitLiteralExpression(expression: ULiteralExpression) {
                val string = expression.getValueIfStringLiteral() ?: return

                if (string.contains("lint") && string!!.matches(".*\\blint\\b.*".toRegex())) {
                    context.report(NStackTestIssue.ISSUE, expression, context.getLocation(expression),
                            "This code mentions `lint`: **Congratulations**")
                }
            }
        }
    }

    companion object {
        val ISSUES = listOf(NStackTestIssue.ISSUE, TextViewSetterIssue.ISSUE)
    }

}
