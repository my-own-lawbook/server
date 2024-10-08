package me.bumiller.mol.validation.impl

import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.model.satisfies
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.GlobalPermission
import me.bumiller.mol.validation.ScopedPermission

internal class ServiceAccessValidator(
    private val lawContentService: LawContentService,
    private val memberContentService: MemberContentService,
    private val userService: UserService,
    private val invitationContentService: InvitationContentService
) : AccessValidator {


    private fun resolveBookPermission(
        role: MemberRole?,
        permission: ScopedPermission.Books
    ): Boolean = when (permission) {
        is ScopedPermission.Books.Write -> role satisfies MemberRole.Admin
        is ScopedPermission.Books.Read -> true
        is ScopedPermission.Books.Children.Create -> role satisfies MemberRole.Moderator
        is ScopedPermission.Books.Children.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Books.Members.ManageInvitations -> role satisfies MemberRole.Admin
        is ScopedPermission.Books.Members.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Books.Members.ReadInvitations -> role satisfies MemberRole.Moderator
        is ScopedPermission.Books.Members.Remove -> role satisfies MemberRole.Admin
        is ScopedPermission.Books.Roles.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Books.Roles.Write -> role satisfies MemberRole.Admin
    }

    private fun resolveEntryPermission(
        role: MemberRole?,
        permission: ScopedPermission.Entries
    ): Boolean = when (permission) {
        is ScopedPermission.Entries.Children.Create -> role satisfies MemberRole.Moderator
        is ScopedPermission.Entries.Children.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Entries.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Entries.Write -> role satisfies MemberRole.Moderator
    }

    private fun resolveSectionPermission(
        role: MemberRole?,
        permission: ScopedPermission.Sections
    ): Boolean = when (permission) {
        is ScopedPermission.Sections.Read -> role satisfies MemberRole.Member
        is ScopedPermission.Sections.Write -> role satisfies MemberRole.Moderator
    }

    private suspend fun resolveLawPermission(permission: ScopedPermission, userId: Long): Boolean {
        require(
            permission is ScopedPermission.Books ||
                    permission is ScopedPermission.Entries ||
                    permission is ScopedPermission.Sections
        )

        val parentBook = try {
            when (permission) {
                is ScopedPermission.Books -> lawContentService.getSpecificBook(id = permission.id)
                is ScopedPermission.Entries -> lawContentService.getBookByEntry(permission.id)
                is ScopedPermission.Sections -> {
                    val entry = lawContentService.getEntryForSection(permission.id)
                    lawContentService.getBookByEntry(entry.id)
                }

                else -> error("")
            }
        } catch (e: ServiceException.LawBookNotFound) {
            return false
        } catch (e: ServiceException.LawEntryNotFound) {
            return false
        } catch (e: ServiceException.LawSectionNotFound) {
            return false
        }

        val memberRole = try {
            memberContentService.getMemberRole(userId, parentBook.id)
        } catch (e: ServiceException.UserNotMemberOfBook) {
            null
        }

        return when (permission) {
            is ScopedPermission.Books -> resolveBookPermission(memberRole, permission)
            is ScopedPermission.Entries -> resolveEntryPermission(memberRole, permission)
            is ScopedPermission.Sections -> resolveSectionPermission(memberRole, permission)

            else -> error("")
        }
    }

    private suspend fun resolveInvitationPermission(
        permission: ScopedPermission.Invitations,
        userId: Long
    ): Boolean {
        val invitation = try {
            invitationContentService.getInvitationById(permission.id)
        } catch (e: ServiceException.InvitationNotFound) {
            return false
        }

        val memberRole = try {
            memberContentService.getMemberRole(userId, invitation.targetBook.id)
        } catch (e: ServiceException.UserNotMemberOfBook) {
            null
        }

        return when (permission) {
            is ScopedPermission.Invitations.Accept -> invitation.recipient.id == userId
            is ScopedPermission.Invitations.Deny -> invitation.recipient.id == userId
            is ScopedPermission.Invitations.Read -> memberRole satisfies MemberRole.Moderator
            is ScopedPermission.Invitations.Revoke -> memberRole satisfies MemberRole.Admin
        }
    }

    private fun resolveUserPermission(
        permission: ScopedPermission.Users
    ): Boolean = when (permission) {
        is ScopedPermission.Users.Read -> !permission.email
    }


    override suspend fun resolveScoped(
        permission: ScopedPermission,
        userId: Long,
        throwOnRestricted: Boolean
    ): Boolean {
        try {
            userService.getSpecific(id = userId)
        } catch (e: ServiceException.UserNotFound) {
            internal()
        }

        val granted = when (permission) {
            is ScopedPermission.Books,
            is ScopedPermission.Entries,
            is ScopedPermission.Sections -> resolveLawPermission(permission, userId)

            is ScopedPermission.Invitations -> resolveInvitationPermission(permission, userId)

            is ScopedPermission.Users -> resolveUserPermission(permission)
        }

        if (throwOnRestricted && !granted)
            notFoundIdentifier(permission.resourceName, permission.id.toString())
        else return granted
    }

    override suspend fun resolveGlobal(
        permission: GlobalPermission,
        userId: Long,
        throwOnRestricted: Boolean
    ): Boolean = when (permission) {
        is GlobalPermission.Users.Read -> !permission.email
    }

}