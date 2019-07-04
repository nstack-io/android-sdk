package dk.nodes.nstack.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Location
import com.intellij.psi.PsiMethod
import dk.nodes.nstack.lint.issues.AppOpenMissingIssue
import dk.nodes.nstack.lint.issues.NStackTestIssue
import dk.nodes.nstack.lint.issues.TextViewSetterIssue
import org.jetbrains.uast.*

class NStackIssueDetector : Detector(), Detector.UastScanner {


    private var appOpenCalled: Boolean = false
    private var nstackInitLocation: Location? = null


    override fun beforeCheckRootProject(context: Context) {
        appOpenCalled = false
        nstackInitLocation = null
    }


    override fun afterCheckRootProject(context: Context) {
        // When all methods/classes visited check if we met appOpen()
        if (!appOpenCalled && nstackInitLocation != null) {
            context.report(AppOpenMissingIssue.ISSUE, nstackInitLocation!!, "NStack was initilized, appOpen() not found")
        } else if (nstackInitLocation != null) {
            context.report(NStackTestIssue.ISSUE, nstackInitLocation!!, "AppOpensiize")
        } else {

        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(ULiteralExpression::class.java)
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf(METHOD_APP_OPEN, METHOD_SET_TEXT, METHOD_INIT)
    }


    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val methodName = node.methodName
        when (methodName) {
            METHOD_SET_TEXT -> {
                val caller = node.receiverType ?: return
                if (caller.canonicalText == "android.widget.TextView") {
                    context.report(TextViewSetterIssue.ISSUE, context.getLocation(node), "Found text view, fixsss setter")
                }
            }
            METHOD_APP_OPEN -> {
                if (method.isFromNStack(context.evaluator)) {
                    appOpenCalled = true
                }
            }

            METHOD_INIT -> {
                if (method.isFromNStack(context.evaluator)) {
                     nstackInitLocation = context.getLocation(node)
                }

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
        val ISSUES = listOf(NStackTestIssue.ISSUE, TextViewSetterIssue.ISSUE, AppOpenMissingIssue.ISSUE)
        private const val METHOD_SET_TEXT = "setText"
        private const val METHOD_APP_OPEN = "appOpen"
        private const val METHOD_INIT = "init"
    }

}
