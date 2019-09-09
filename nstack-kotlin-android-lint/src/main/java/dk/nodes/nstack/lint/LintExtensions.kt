package dk.nodes.nstack.lint

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import org.jetbrains.uast.UExpression
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.client.api.JavaEvaluator
import com.intellij.psi.PsiMethod




internal fun UExpression.isSubclassOf(context: JavaContext, cls: Class<*>): Boolean {
    val expressionType = getExpressionType()
    if (expressionType is PsiClassType) {
        val classType = expressionType as PsiClassType?
        val resolvedClass = classType!!.resolve()
        return context.evaluator.extendsClass(resolvedClass, cls.name, false)
    }
    return false
}

internal fun PsiMethod.isFromNStack(evaluator: JavaEvaluator): Boolean {
    return (evaluator.isMemberInClass(this, "dk.nodes.nstack.kotlin.NStack"))
}
