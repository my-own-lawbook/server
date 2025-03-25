package me.bumiller.civoris.email.html

import kotlinx.html.*

private const val CENTERED_STYLE = """
    width: 100%;
    display: flex;
    align-content: center;
    justify-content: center;
"""

private const val WEB_LINK_BUTTON_STYLE = """
    text-decoration: none;
    color: #FFFFFF;
    font-weight: bold;
    background: #415F91;
    padding: 12px 24px;
    border-radius: 16px;
"""

/**
 * Ending of every email.
 */
fun DIV.emailEnd() {
    p { +"Yours, the Civoris team!" }
}

/**
 * A button that links to the website.
 *
 * @param text The text to show on the button
 * @param webLink The link to which to redirect
 */
fun DIV.webLinkButton(
    text: String,
    webLink: String
) {
    a(
        href = webLink
    ) {
        style = WEB_LINK_BUTTON_STYLE

        +text
    }
}

/**
 * Component for the title of the email.
 *
 * @param title The title to show
 */
fun DIV.emailTitle(
    title: String
) {
    centered {
        h1 {
            +title
        }
    }
}

/**
 * Component that centers the content.
 *
 * @param content The content
 */
fun DIV.centered(
    content: DIV.() -> Unit
) {
    div {
        style = CENTERED_STYLE

        content()
    }
}

/**
 * Component that centers the content.
 *
 * @param content The content
 */
fun HEADER.centered(
    content: DIV.() -> Unit
) {
    div {
        style = CENTERED_STYLE

        content()
    }
}