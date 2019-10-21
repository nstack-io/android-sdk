package dk.nodes.nstack.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.XmlContext
import com.android.xml.AndroidManifest
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import dk.nodes.nstack.lint.issues.AppOpenMissingIssue
import dk.nodes.nstack.lint.issues.NStackHardcodedIssue
import dk.nodes.nstack.lint.issues.NStackTestIssue
import dk.nodes.nstack.lint.issues.TextViewSetterIssue
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.getValueIfStringLiteral
import org.w3c.dom.Element

class NStackIssuesDetector :
    Detector(),
    Detector.UastScanner,
    Detector.XmlScanner {

    private var appOpenCalled: Boolean = false
    private var nstackInitLocation: Location? = null

    override fun beforeCheckRootProject(context: Context) {
        appOpenCalled = false
        nstackInitLocation = null
    }

    override fun afterCheckRootProject(context: Context) {
        // When all methods/classes visited check if we required methods were implemented
        nstackInitLocation?.let { initLocation ->
            when {
                !appOpenCalled -> context.report(
                    AppOpenMissingIssue.ISSUE,
                    initLocation,
                    "AppOpen is not called"
                )
            }
        }
    }

    override fun getApplicableElements(): Collection<String>? {
        return listOf(TAG_META_DATA)
    }

    override fun visitElement(context: XmlContext, element: Element) {
        // Don't check library manifests
        if (context.project != context.mainProject ||
            context.mainProject.isLibrary
        ) {
            return
        }

        if (TAG_META_DATA == element.nodeName) {
            val name = element.getAttributeNS(ANDROID_URI, AndroidManifest.ATTRIBUTE_NAME)
            if (name == NSTACK_API_KEY || name == NSTACK_APP_ID || name == NSTACK_ENV) {
                val value = element.getAttributeNS(ANDROID_URI, AndroidManifest.ATTRIBUTE_VALUE)
                val lastPathSegment = name.split(".").last()
                if (!value.contains("\${")) {
                    val fix: LintFix = fix().set(
                        ANDROID_URI,
                        AndroidManifest.ATTRIBUTE_VALUE,
                        "\${$lastPathSegment}"
                    ).build()
                    context.report(
                        NStackHardcodedIssue.ISSUE,
                        context.getLocation(element),
                        "You can not hardcoded NStack Environment variables",
                        fix
                    )
                }
            }
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
        if (method.isFromNStack(context.evaluator)) {
            when (methodName) {
                METHOD_SET_TEXT -> {
                    val caller = node.receiverType ?: return
                    if (caller.canonicalText == "android.widget.TextView") {
                        context.report(
                            TextViewSetterIssue.ISSUE,
                            context.getLocation(node),
                            "Found text view, fixsss setter"
                        )
                    }
                }
                METHOD_APP_OPEN -> {
                    appOpenCalled = true
                }

                METHOD_INIT -> {
                    nstackInitLocation = context.getLocation(node)
                }
            }
        }
    }

    override fun getApplicableReferenceNames(): List<String>? {
        return listOf()
    }

    override fun visitReference(
        context: JavaContext,
        reference: UReferenceExpression,
        referenced: PsiElement
    ) {

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
                    context.report(
                        NStackTestIssue.ISSUE, expression, context.getLocation(expression),
                        "This code mentions `lint`: **Congratulations**"
                    )
                }
            }
        }
    }

    companion object {
        val ISSUES = listOf(
            NStackTestIssue.ISSUE,
            TextViewSetterIssue.ISSUE,
            AppOpenMissingIssue.ISSUE,
            NStackHardcodedIssue.ISSUE
        )
        // Methods
        private const val METHOD_SET_TEXT = "setText"
        private const val METHOD_APP_OPEN = "appOpen"
        private const val METHOD_INIT = "init"

        // TAGS
        private const val TAG_META_DATA = "meta-data"

        private const val NSTACK_APP_ID = "dk.nodes.nstack.appId"
        private const val NSTACK_API_KEY = "dk.nodes.nstack.apiKey"
        private const val NSTACK_ENV = "dk.nodes.nstack.env"
        private const val ANDROID_URI = "http://schemas.android.com/apk/res/android"
    }
}
