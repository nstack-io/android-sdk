package dk.nodes.nstack.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import dk.nodes.nstack.lint.issues.AppOpenMissingIssue
import dk.nodes.nstack.lint.issues.NStackTestIssue
import dk.nodes.nstack.lint.issues.TextViewSetterIssue
import org.jetbrains.uast.*

class NStackIssuesDetector : Detector(), Detector.UastScanner {


    private var appOpenCalled: Boolean = false
    private var nstackInitLocation: Location? = null
    private var versionControlUsed: Boolean = false

    override fun beforeCheckRootProject(context: Context) {
        appOpenCalled = false
        versionControlUsed = false
        nstackInitLocation = null
    }


    override fun afterCheckRootProject(context: Context) {
        // When all methods/classes visited check if we required methods were implemented
        nstackInitLocation?.let { initLocation ->
            when {
                !appOpenCalled -> context.report(AppOpenMissingIssue.ISSUE, initLocation, "AppOpen")
                !versionControlUsed -> context.report(AppOpenMissingIssue.ISSUE, initLocation, "VersionControl")
            }
        }
    }


    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(ULiteralExpression::class.java)
    }


    override fun getApplicableMethodNames(): List<String>? {
        return listOf(METHOD_APP_OPEN, METHOD_SET_TEXT, METHOD_INIT, METHOD_VERSION_CONTROL)
    }


    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val methodName = node.methodName
        if (method.isFromNStack(context.evaluator)) {
            when (methodName) {
                METHOD_SET_TEXT -> {
                    val caller = node.receiverType ?: return
                    if (caller.canonicalText == "android.widget.TextView") {
                        context.report(TextViewSetterIssue.ISSUE, context.getLocation(node), "Found text view, fixsss setter")
                    }
                }
                METHOD_APP_OPEN -> {
                    appOpenCalled = true
                }

                METHOD_INIT -> {
                    nstackInitLocation = context.getLocation(node)

                }
                METHOD_VERSION_CONTROL -> {
                    versionControlUsed = true
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
        private const val METHOD_VERSION_CONTROL = "onAppUpdateListener"

    }

}
