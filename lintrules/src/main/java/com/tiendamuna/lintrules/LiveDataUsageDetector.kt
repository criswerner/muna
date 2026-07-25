package com.tiendamuna.lintrules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UTypeReferenceExpression

class LiveDataUsageDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UTypeReferenceExpression::class.java, UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitTypeReferenceExpression(node: UTypeReferenceExpression) {
                checkType(node.type, node)
            }

            override fun visitCallExpression(node: UCallExpression) {
                // Catching constructor calls or method returns
                node.returnType?.let { checkType(it, node) }
            }

            private fun checkType(type: PsiType, node: UElement) {
                val qualifiedName = type.canonicalText
                if (qualifiedName.startsWith("androidx.lifecycle.LiveData") ||
                    qualifiedName.startsWith("androidx.lifecycle.MutableLiveData")
                ) {
                    context.report(
                        issue = ISSUE,
                        location = context.getLocation(node),
                        message = "Evita el uso de LiveData. Considera usar 'StateFlow' o 'SharedFlow' para un manejo reactivo Kotlin-first."
                    )
                }
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "AvoidLiveDataUsage",
            briefDescription = "Uso de LiveData detectado",
            explanation = "En la arquitectura moderna de Android, se recomienda preferir 'StateFlow' o 'SharedFlow' sobre 'LiveData' para mantener una capa de presentación puramente basada en Corrutinas de Kotlin.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                LiveDataUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
