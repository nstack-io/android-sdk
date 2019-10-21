package dk.nodes.nstack.lint

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UExpression

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
