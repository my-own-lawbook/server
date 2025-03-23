package me.bumiller.civoris.email.template

private val Reference = object {}

private const val TEMPLATE_FILE_EXTENSION = ".html"

private const val BASE_FILE_NAME = "base" + TEMPLATE_FILE_EXTENSION

private const val PARENT_DIRECTORY = "/templates/"

private const val STYLE_OPENING_TAG = "<style>"
private const val STYLE_CLOSING_TAG = "</style>"

private const val INCLUDE_OPENING_TAG = "<include>"
private const val INCLUDE_CLOSING_TAG = "</include>"
private const val INCLUDE_OPTIONAL_OPENING_TAG = "<includeoptional>"
private const val INCLUDE_OPTIONAL_CLOSING_TAG = "</includeoptional>"

private const val PLACEHOLDER_OPENING_TAG = '{'
private const val PLACEHOLDER_CLOSING_TAG = '}'

private const val CONTENT_PLACEHOLDER = "{{content}}"

private val CUSTOM_TAG_CONTENT_REGEX = "^[a-z0-9-_]+$".toRegex()

/**
 * Generates the full HTML for a given template.
 *
 * @param name The name of the template
 * @param placeholderValues The values of the placeholders to replace
 * @return The fulltext formatted html to embed in the mail. Null if placeholders were requested that could not be
 * satisfied by the [placeholderValues] map.
 */
fun generateTemplate(
    name: String, placeholderValues: Map<String, String?>
): TemplateResult<String> {
    val baseFile = readHtmlFile(BASE_FILE_NAME).let { file ->
        file.copy(
            content = file.content.map { it.replace(CONTENT_PLACEHOLDER, name) })
    }

    return generateTemplateInternal(baseFile, placeholderValues).map(HtmlFile::getFileContent)
}

private fun generateTemplateInternal(
    name: String, placeholderValues: Map<String, String?>
) = generateTemplateInternal(
    file = readHtmlFile(name + TEMPLATE_FILE_EXTENSION),
    placeholderValues = placeholderValues,
)

private fun generateTemplateInternal(
    file: HtmlFile, placeholderValues: Map<String, String?>
): TemplateResult<HtmlFile> {
    val includeInfos = getIncludedInfos(file)
    val placeholderInfos = getPlaceholderInfos(file)

    placeholderInfos.forEach { info ->
        if (placeholderValues[info.content] == null) {
            return TemplateResult.MissingPlaceholder(info.content)
        }
    }

    val includedTemplates = includeInfos.associateWith { info ->
        val template = generateTemplateInternal(info.content, placeholderValues)

        when (template) {
            is TemplateResult.MissingPlaceholder<HtmlFile> -> if (info.isOptional) null else return template
            is TemplateResult.Template<HtmlFile> -> template.html
        }
    }

    val style = mutableListOf<String>()
    val content = mutableListOf<String>()

    val styleLines =
        includedTemplates.map(Map.Entry<CustomTagInfo, HtmlFile?>::value).filterNotNull().map(HtmlFile::style).flatten()

    style.addAll(file.style)
    style.addAll(styleLines)

    file.content.forEachIndexed { index, contentLine ->
        val includeInfo = includeInfos.find { it.lineIndex == index }
        val template = includedTemplates[includeInfo]
        val placeholderInfosForLine = placeholderInfos.filter { it.lineIndex == index }

        if (template != null) {
            content.addAll(template.content)
        } else if (placeholderInfosForLine.isNotEmpty()) {
            var newLine = contentLine
            placeholderInfosForLine.forEach { placeholderInfo ->
                newLine = newLine.replace(
                    PLACEHOLDER_OPENING_TAG + placeholderInfo.content + PLACEHOLDER_CLOSING_TAG,
                    placeholderValues[placeholderInfo.content]!!
                )
            }
            content.add(newLine)
        } else if (includeInfo == null) {
            content.add(contentLine)
        }
    }

    return TemplateResult.Template(HtmlFile(style, content))
}

private fun getPlaceholderInfos(file: HtmlFile): List<CustomTagInfo> {
    val placeholders = mutableListOf<CustomTagInfo>()

    var beginAt: Int? = null
    file.content.forEachIndexed { lineIndex, line ->
        line.forEachIndexed { lineCharIndex, lineChar ->
            if (lineChar == PLACEHOLDER_OPENING_TAG) {
                check(beginAt == null) { "Illegal new placeholder opening found" }
                beginAt = lineCharIndex
            } else if (lineChar == PLACEHOLDER_CLOSING_TAG) {
                checkNotNull(beginAt) { "Illegal new placeholder closing found" }

                val content = line.substring(beginAt!! + 1, lineCharIndex)
                check(CUSTOM_TAG_CONTENT_REGEX.matches(content)) { "Illegal placeholder content not matching regex: '$content'" }
                placeholders.add(CustomTagInfo(lineIndex, content, false))
                beginAt = null
            }
        }
    }

    return placeholders
}

private fun getIncludedInfos(file: HtmlFile): List<CustomTagInfo> {
    val includes = mutableListOf<CustomTagInfo>()

    file.content.forEachIndexed { index, line ->
        val isInclude = line.trim().run {
            startsWith(INCLUDE_OPENING_TAG) && endsWith(INCLUDE_CLOSING_TAG)
        }
        val isIncludeOptional = line.trim().run {
            startsWith(INCLUDE_OPTIONAL_OPENING_TAG) && endsWith(INCLUDE_OPTIONAL_CLOSING_TAG)
        }
        if (isInclude) {
            val name = line.trim().removePrefix(INCLUDE_OPENING_TAG).removeSuffix(INCLUDE_CLOSING_TAG)

            check(CUSTOM_TAG_CONTENT_REGEX.matches(name)) {
                "Invalid template name found in include tag: '$name'."
            }

            includes.add(CustomTagInfo(index, name, false))
        } else if (isIncludeOptional) {
            val name = line.trim().removePrefix(INCLUDE_OPTIONAL_OPENING_TAG).removeSuffix(INCLUDE_OPTIONAL_CLOSING_TAG)

            check(CUSTOM_TAG_CONTENT_REGEX.matches(name)) {
                "Invalid template name found in include tag: '$name'."
            }

            includes.add(CustomTagInfo(index, name, true))
        }
    }

    return includes
}

private fun readHtmlFile(name: String, parent: String = PARENT_DIRECTORY): HtmlFile =
    Reference.javaClass.getResourceAsStream(parent + name)?.bufferedReader()?.readLines()?.run {
        val centerIndex = indexOfLast { it == STYLE_CLOSING_TAG } + 1

        val style = take(centerIndex).filterNot { STYLE_OPENING_TAG in it || STYLE_CLOSING_TAG in it }

        val content = if (centerIndex == 0) takeLast(size) else takeLast(size - centerIndex - 1)

        HtmlFile(style, content)
    } ?: throw IllegalArgumentException("Could not find a file at resource location '$parent$name'.")

private data class HtmlFile(
    val style: List<String>, val content: List<String>
) {

    fun getFileContent() = """
        <html lang='en'>
            <head>
                <meta charset='UTF-8'>
                
                <style>
                    ${style.joinToString(System.lineSeparator())}
                </style>
            </head>
            <body>
                ${content.joinToString(System.lineSeparator())}
            </body>
        </html>
    """.trimIndent()

}

/**
 * Results of creating a template.
 *
 * @param Data The data representing the finshed html
 */
sealed interface TemplateResult<Data> {

    /**
     * The template was created successfully.
     *
     * @param html The finished html
     */
    data class Template<Data>(val html: Data) : TemplateResult<Data>

    /**
     * Could not create a template because a placeholder was missing.
     *
     * @param missingKey The placeholder key that was missing
     */
    data class MissingPlaceholder<Data>(val missingKey: String) : TemplateResult<Data>

    /**
     * Maps a result to another encapsulated data type.
     *
     * @param mapper The mapper converting the data types
     * @param R  The new data type
     */
    fun <R> map(mapper: (Data) -> R): TemplateResult<R> = when (this) {
        is TemplateResult.MissingPlaceholder<Data> -> MissingPlaceholder(missingKey)
        is TemplateResult.Template<Data> -> Template(mapper(html))
    }

}

private data class CustomTagInfo(
    val lineIndex: Int, val content: String, val isOptional: Boolean
)