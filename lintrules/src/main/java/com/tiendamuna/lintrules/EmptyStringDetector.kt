package com.tiendamuna.lintrules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression

class EmptyStringDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        // Escuchamos todas las expresiones literales (números, strings, booleans)
        return listOf(ULiteralExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitLiteralExpression(node: ULiteralExpression) {
                // Comprobamos si el literal es un String
                val value = node.value as? String ?: return

                // Verificamos si la cadena está completamente vacía
                if (value.isEmpty()) {
                    // Creamos una solución rápida (QuickFix) para el IDE
                    val fix = LintFix.create()
                        .name("Reemplazar por String.empty()")
                        .replace()
                        .text("\"\"")
                        .with("String.empty()")
                        .build()

                    context.report(
                        issue = ISSUE,
                        location = context.getLocation(node),
                        message = "Evita el uso de '\"\"'. Utiliza la extensión 'String.empty()' definida en el proyecto.",
                        quickfixData = fix
                    )
                }
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "UseStringEmptyExtension",
            briefDescription = "Uso de literal de cadena vacía \"\"",
            explanation = "Para mantener la consistencia en el proyecto, se recomienda usar la función de extensión 'String.empty()' en lugar de '\"\"'.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING, // Puedes usar Severity.ERROR si deseas que bloquee el build
            implementation = Implementation(
                EmptyStringDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}