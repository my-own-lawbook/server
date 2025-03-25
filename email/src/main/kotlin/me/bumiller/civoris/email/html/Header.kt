package me.bumiller.civoris.email.html

import kotlinx.html.DIV
import kotlinx.html.header
import kotlinx.html.img
import kotlinx.html.style

private const val IMAGE_STYLE = """
    width: min(50%, 400px);
"""

/**
 * Component for the email header.
 *
 * @param logoSrc The source url of the logo
 */
fun DIV.civorisHeader(
    logoSrc: String
) {
    header {
        centered {
            img(
                src = logoSrc
            ) {
                style = IMAGE_STYLE
            }
        }
    }
}