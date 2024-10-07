package me.bumiller.mol.model

/**
 * The status a book-invitation can be in
 */
enum class InvitationStatus {

    /**
     * Still pending
     */
    Open,

    /**
     * Was accepted by recipient
     */
    Accepted,

    /**
     * Was declined by recipient
     */
    Declined,

    /**
     * Was revoked by author
     */
    Revoked

}