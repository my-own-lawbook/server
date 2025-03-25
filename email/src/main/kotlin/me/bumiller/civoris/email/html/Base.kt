package me.bumiller.civoris.email.html

import kotlinx.html.*
import kotlinx.html.stream.appendHTML

private const val WRAPPER_STYLE = """
    height: 100%;
    display: flex;
    flex-direction: column;
    
    font-family: sans-serif;
    
    box-sizing: border-box;
"""

private const val SPACER_STYLE = """
    flex: 1;
"""

/**
 * Base email component.
 *
 * @param logoSrc The source url of the logo
 * @param githubLink The link to the github page
 * @param webLink The link to the website
 * @param content The content to put inside the base email
 */
fun StringBuilder.emailBase(
    logoSrc: String,
    githubLink: String,
    webLink: String?,
    content: DIV.() -> Unit
) = appendHTML(true).html {

    head {
        meta(charset = Charsets.UTF_8.name())
    }

    body {
        div {
            style = WRAPPER_STYLE

            civorisHeader(logoSrc)

            content()

            spacer()

            civorisFooter(githubLink, webLink)
        }
    }
}

private fun DIV.spacer() = div {
    style = SPACER_STYLE
}