import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.util.Locale

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

// --- CONFIGURACIÓN DE PONDERACIÓN (REGLAS DE PUNTOS) ---
val WEIGHT_EMPTY_STRING = 1         // 1 pt por cada ""
val WEIGHT_HARDCODED_COMPOSE = 3   // 3 pts por String hardcodeado en Compose
val WEIGHT_LINT_ERROR = 5          // 5 pts por errores de Lint generales
val WEIGHT_LINT_WARNING = 2        // 2 pts por warnings de Lint
val WEIGHT_COVERAGE_GAP = 2        // 2 pts por cada 1% de cobertura faltante (de 100%)

data class ModuleHealth(
    val name: String,
    var lintScore: Int = 0,
    var emptyStringCount: Int = 0,
    var composeHardcodedCount: Int = 0,
    var lintErrors: Int = 0,
    var lintWarnings: Int = 0,
    var coveragePercentage: Double = 0.0,
    var coverageScore: Int = 0
) {
    val totalScore: Int get() = lintScore + coverageScore
}

abstract class GenerateHealthReportTask : DefaultTask() {
    @get:Input
    abstract val subprojectData: MapProperty<String, File>

    @get:Input
    abstract val weights: MapProperty<String, Int>

    @get:OutputDirectory
    abstract val outputReportDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val wMap = weights.get()
        val wEmpty = wMap["EMPTY_STRING"] ?: 20
        val wHardcoded = wMap["HARDCODED_COMPOSE"] ?: 3
        val wError = wMap["LINT_ERROR"] ?: 5
        val wWarning = wMap["LINT_WARNING"] ?: 2
        val wGap = wMap["COVERAGE_GAP"] ?: 2

        val modulesHealth = mutableListOf<ModuleHealth>()

        subprojectData.get().forEach { (subName, subBuildDir) ->
            val health = ModuleHealth(name = subName)

            // 1. PARSEAR REPORTES DE LINT
            val lintFile = File(subBuildDir, "reports/lint-results-debug.xml")
            if (lintFile.exists()) {
                parseLintXml(lintFile, health, wEmpty, wHardcoded, wError, wWarning)
            }

            // 2. PARSEAR REPORTES DE JACOCO
            val jacocoFile = File(subBuildDir, "reports/jacoco/testDebugUnitTestReport/jacocoTestReport.xml")
            if (jacocoFile.exists()) {
                parseJacocoXml(jacocoFile, health, wGap)
            } else {
                health.coveragePercentage = 0.0
                health.coverageScore = 100 * wGap
            }

            modulesHealth.add(health)
        }

        modulesHealth.sortBy { it.totalScore }

        // 3. GENERAR REPORTE HTML
        val outputDir = outputReportDir.get().asFile
        outputDir.mkdirs()
        val htmlReportFile = File(outputDir, "index.html")

        generateHtmlReport(modulesHealth, htmlReportFile)

        println("✅ Reporte de salud generado con éxito en: ${htmlReportFile.absolutePath}")
    }

    private fun parseLintXml(xmlFile: File, health: ModuleHealth, wEmpty: Int, wHardcoded: Int, wError: Int, wWarning: Int) {
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.parse(xmlFile)
            val issues = doc.getElementsByTagName("issue")

            for (i in 0 until issues.length) {
                val item = issues.item(i)
                val attributes = item.attributes
                val id = attributes.getNamedItem("id")?.nodeValue ?: ""
                val severity = attributes.getNamedItem("severity")?.nodeValue ?: ""

                when (id) {
                    "UseStringEmptyExtension" -> {
                        health.emptyStringCount++
                        health.lintScore += wEmpty
                    }
                    "NoHardcodedStringInCompose" -> {
                        health.composeHardcodedCount++
                        health.lintScore += wHardcoded
                    }
                    else -> {
                        if (severity == "Error" || severity == "Fatal") {
                            health.lintErrors++
                            health.lintScore += wError
                        } else if (severity == "Warning") {
                            health.lintWarnings++
                            health.lintScore += wWarning
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ No se pudo leer el archivo Lint en ${xmlFile.path}: ${e.message}")
        }
    }

    private fun parseJacocoXml(xmlFile: File, health: ModuleHealth, wGap: Int) {
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            docBuilder.setEntityResolver { _, _ -> InputSource(StringReader("")) }
            val doc = docBuilder.parse(xmlFile)
            val counters = doc.getElementsByTagName("counter")

            var missedInstructions = 0L
            var coveredInstructions = 0L

            for (i in 0 until counters.length) {
                val item = counters.item(i)
                val attributes = item.attributes
                val type = attributes.getNamedItem("type")?.nodeValue
                if (type == "INSTRUCTION") {
                    missedInstructions += attributes.getNamedItem("missed")?.nodeValue?.toLong() ?: 0L
                    coveredInstructions += attributes.getNamedItem("covered")?.nodeValue?.toLong() ?: 0L
                }
            }

            val total = missedInstructions + coveredInstructions
            if (total > 0) {
                health.coveragePercentage = (coveredInstructions.toDouble() / total.toDouble()) * 100
                val gap = (100.0 - health.coveragePercentage).toInt()
                health.coverageScore = gap * wGap
            } else {
                health.coverageScore = 100 * wGap
            }
        } catch (e: Exception) {
            println("⚠️ No se pudo leer el archivo JaCoCo en ${xmlFile.path}: ${e.message}")
        }
    }

    private fun generateHtmlReport(modules: List<ModuleHealth>, outputFile: File) {
        val htmlContent = StringBuilder().apply {
            append("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Ranking de Salud de Módulos Android</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background-color: #f4f6f8; margin: 20px; color: #333; }
                        h1 { color: #111; margin-bottom: 5px; }
                        p.subtitle { color: #666; font-size: 14px; margin-bottom: 25px; }
                        table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
                        th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #edf2f7; }
                        th { background-color: #2d3748; color: #fff; font-weight: 600; font-size: 13px; text-transform: uppercase; }
                        tr:hover { background-color: #f8fafc; }
                        .rank { font-weight: bold; width: 50px; text-align: center; }
                        .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 12px; }
                        .badge-score { background-color: #ebf8ff; color: #2b6cb0; border: 1px solid #bee3f8; }
                        .badge-good { background-color: #f0fff4; color: #276749; }
                        .badge-warn { background-color: #fffaf0; color: #9c4221; }
                    </style>
                </head>
                <body>
                    <h1>🏆 Ranking de Salud de Módulos</h1>
                    <p class="subtitle">Métrica semanal de deuda técnica. <b>Menor puntaje = Módulo más sano.</b></p>
                    <table>
                        <thead>
                            <tr>
                                <th class="rank">#</th>
                                <th>Módulo</th>
                                <th>Puntaje Total</th>
                                <th>Strings ""</th>
                                <th>Hardcoded Compose</th>
                                <th>Otros Errors/Warns</th>
                                <th>Cobertura Tests</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent())

            modules.forEachIndexed { index, m ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> (index + 1).toString()
                }

                append("""
                    <tr>
                        <td class="rank">$medal</td>
                        <td><strong>:${m.name}</strong></td>
                        <td><span class="badge badge-score">${m.totalScore} pts</span></td>
                        <td>${m.emptyStringCount}</td>
                        <td>${m.composeHardcodedCount}</td>
                        <td>${m.lintErrors} E / ${m.lintWarnings} W</td>
                        <td><span class="badge ${if (m.coveragePercentage > 70) "badge-good" else "badge-warn"}">${"%.1f".format(Locale.getDefault(), m.coveragePercentage)}%</span></td>
                    </tr>
                """.trimIndent())
            }

            append("""
                        </tbody>
                    </table>
                </body>
                </html>
            """.trimIndent())
        }

        outputFile.writeText(htmlContent.toString())
    }
}

tasks.register<GenerateHealthReportTask>("generateHealthReport") {
    group = "verification"
    description = "Calcula la métrica de salud de cada módulo a partir de los XML de Lint y JaCoCo"

    subprojectData.set(subprojects.associate { it.name to it.layout.buildDirectory.asFile.get() })
    outputReportDir.set(layout.buildDirectory.dir("reports/health"))
    
    weights.set(mapOf(
        "EMPTY_STRING" to WEIGHT_EMPTY_STRING,
        "HARDCODED_COMPOSE" to WEIGHT_HARDCODED_COMPOSE,
        "LINT_ERROR" to WEIGHT_LINT_ERROR,
        "LINT_WARNING" to WEIGHT_LINT_WARNING,
        "COVERAGE_GAP" to WEIGHT_COVERAGE_GAP
    ))
}
