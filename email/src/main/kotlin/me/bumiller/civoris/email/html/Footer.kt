package me.bumiller.civoris.email.html

import kotlinx.html.*

private const val FOOTER_STYLE = """
    background: #D6E3FF;
    height: min(100px, 30%);
    border-top-left-radius: 32px;
    border-top-right-radius: 32px;
    padding: 32px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
"""

private const val LINKS_CONTAINER_STYLE = """
    display: flex;
    flex-direction: row;
    gap: 32px;
"""

private const val LINK_STYLE = """
    color: #001B3E;
    text-decoration: none;
"""

/**
 * Component for the email footer.
 *
 * @param githubLink The link to the github page
 * @param webLink The link to the website
 */
fun DIV.civorisFooter(
    githubLink: String,
    webLink: String?
) {
    footer {
        style = FOOTER_STYLE

        div {
            style = LINKS_CONTAINER_STYLE

            footerLink(githubLink, "Visit GitHub")
            webLink?.let { footerLink(it, "Visit Website") }
        }

        p { +"2025 Civoris" }
    }
}

private fun DIV.footerLink(
    link: String,
    text: String
) {
    a(
        href = link
    ) {
        style = LINK_STYLE

        +text
    }
}