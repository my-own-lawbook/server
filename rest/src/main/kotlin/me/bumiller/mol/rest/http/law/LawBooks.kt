package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.MemberService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.http.PathBookId
import me.bumiller.mol.rest.http.PathUserId
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.response.user.BookRoleUserResponse
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.*
import me.bumiller.mol.validation.actions.isMemberRole
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints:
 *
 * Core endpoints for books:
 * - GET /law-books/: Get all law-books
 * - GET /law-books/:id/: Get a specific law-book
 * - POST /law-books/: Create a law-book
 * - PATCH /law-books/:id/: Perform a partial update on a law-book
 * - DELETE /law-books/:id/: Delete a law-book
 *
 *
 * Endpoints for book-members
 * - GET /law-books/:id/members/: Get all members of a law-book
 * - PUT /law-books/:id/members/:id/: Add a member to a law-book
 * - DELETE /law-books/:id/members/:id/: Delete a member from a law-book
 *
 * Endpoints for book-roles
 * - GET /law-books/:id/roles/: Get roles for each member of a law-book
 * - GET /law-books/:id/roles/:id/: Get role for a specific member
 * - PUT /law-books/:id/roles/:id/: Set role of a user in a law-book
 */
internal fun Route.lawBooks() {
    val lawContentService by inject<LawContentService>()
    val memberContentService by inject<MemberContentService>()
    val memberService by inject<MemberService>()
    val userService by inject<UserService>()
    val accessValidator by inject<AccessValidator>()

    route("law-books/") {
        getById(lawContentService, accessValidator)
        create(lawContentService)
        update(lawContentService, accessValidator)
        delete(lawContentService, accessValidator)

        route("{$PathBookId}/members/") {
            getMembers(memberContentService, accessValidator)
            route("{$PathUserId}/") {
                putMember(memberContentService, memberService, accessValidator)
                removeMember(memberContentService, memberService, accessValidator)
            }
        }

        route("{$PathBookId}/roles/") {
            memberRoles(memberContentService, accessValidator)

            route("{$PathUserId}/") {
                memberRole(memberContentService, userService, accessValidator)
                putMemberRole(memberService, accessValidator)
            }
        }
    }
}

//
// Request-Bodies
//

@Serializable
internal data class CreateLawBookRequest(

    val key: String,

    val name: String,

    val description: String

) : Validatable

@Serializable
internal data class UpdateLawBookRequest(

    val key: Optional<String> = empty(),

    val name: Optional<String> = empty(),

    val description: Optional<String> = empty()

) : Validatable

@Serializable
internal data class PutUserBookRoleRequest(

    val role: Int

) : Validatable {

    override suspend fun validate() {
        validateThat(role).isMemberRole()
    }

}

//
// Endpoints
//

/**
 * Endpoint to GET /law-books/:id that returns a specific law-book
 */
private fun Route.getById(lawContentService: LawContentService, accessValidator: AccessValidator) =
    get("{$PathBookId}/") {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

        accessValidator.resolveScoped(ScopedPermission.Books.Read(bookId), user.id)

        val book = lawContentService.getSpecificBook(id = bookId)
    val response = LawBookResponse.create(book)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /law-books/ that allows a user to add a new law-book
 */
private fun Route.create(lawContentService: LawContentService) = post {
    val body = call.validated<CreateLawBookRequest>()

    val created = try {
        lawContentService.createBook(body.key, body.name, body.description, user.id)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    val response = LawBookResponse.create(created)

    call.respond(HttpStatusCode.Created, response)
}

/**
 * Endpoint to PATCH /law-books/:id that allows a user to update an existing law-book
 */
private fun Route.update(lawContentService: LawContentService, accessValidator: AccessValidator) =
    patch("{$PathBookId}/") {
    val body = call.validated<UpdateLawBookRequest>()
    val bookId = call.parameters.longOrBadRequest(PathBookId)

        accessValidator.resolveScoped(ScopedPermission.Books.Write(bookId), user.id)

    val updated = lawContentService.updateBook(
        bookId = bookId,
        key = body.key,
        name = body.name,
        description = body.description
    )
    val response = LawBookResponse.create(updated)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint for DELETE /law-books/:id/ that allows a user to delete a law-book
 */
private fun Route.delete(lawContentService: LawContentService, accessValidator: AccessValidator) =
    delete("{$PathBookId}/") {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

        accessValidator.resolveScoped(ScopedPermission.Books.Write(bookId), user.id)

        val deleted = lawContentService.deleteBook(bookId)
    val response = LawBookResponse.create(deleted)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint for GET /law-books/:id/members/ that gets all members for a law-book
 */
private fun Route.getMembers(memberContentService: MemberContentService, accessValidator: AccessValidator) = get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    accessValidator.resolveScoped(ScopedPermission.Books.Members.Read(bookId), user.id)

    val members = memberContentService.getMembersInBook(bookId)
    val response = members.map(UserWithProfileResponse::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to PUT /law-books/:id/members/:id/ that adds a new user to the members of a law-book
 */
private fun Route.putMember(
    memberContentService: MemberContentService,
    memberService: MemberService,
    accessValidator: AccessValidator
) = put {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val userId = call.parameters.longOrBadRequest(PathUserId)

    accessValidator.resolveScoped(ScopedPermission.Books.Members.ManageInvitations(bookId), user.id)

    val members = try {
        memberService.addMemberToBook(bookId, userId)
    } catch (e: ServiceException.UserAlreadyMemberOfBook) {
        memberContentService.getMembersInBook(bookId)
    }

    val response = members.map(UserWithProfileResponse::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to DELETE /law-books/:id/members/:id/ that removes a user from the members of a law-book
 */
private fun Route.removeMember(
    memberContentService: MemberContentService,
    memberService: MemberService,
    accessValidator: AccessValidator
) = delete {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val userId = call.parameters.longOrBadRequest(PathUserId)

    accessValidator.resolveScoped(ScopedPermission.Books.Members.Remove(bookId), user.id)

    val members = try {
        memberService.removeMemberFromBook(bookId, userId)
    } catch (e: ServiceException.UserNotMemberOfBook) {
        memberContentService.getMembersInBook(bookId)
    } catch (e: ServiceException.UserNotFound) {
        memberContentService.getMembersInBook(bookId)
    }

    val response = members.map(UserWithProfileResponse::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-books/:id/roles/ that gets all users and their role
 */
private fun Route.memberRoles(memberContentService: MemberContentService, accessValidator: AccessValidator) = get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    accessValidator.resolveScoped(ScopedPermission.Books.Roles.Read(bookId), user.id)

    val members = memberContentService.getMembersInBook(bookId)
    val rolesForMembers = members.map { member ->
        memberContentService.getMemberRole(member.id, bookId)
    }
    val response = members.zip(rolesForMembers) { member, role ->
        BookRoleUserResponse.create(role.value, member)
    }

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-books/:id/roles/:id/ that gets a user and it's role
 */
private fun Route.memberRole(
    memberContentService: MemberContentService,
    userService: UserService,
    accessValidator: AccessValidator
) =
    get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val userId = call.parameters.longOrBadRequest(PathUserId)

        accessValidator.resolveScoped(ScopedPermission.Books.Roles.Read(bookId), user.id)

        val user = userService.getSpecific(id = userId)

        val role = memberContentService.getMemberRole(userId, bookId)
        val response = BookRoleUserResponse.create(role.value, user)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to PUT /law-books/:id/roles/:id/ that changes the role of a member.
 */
private fun Route.putMemberRole(
    memberService: MemberService,
    accessValidator: AccessValidator
) = put {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val userId = call.parameters.longOrBadRequest(PathUserId)

    val body = call.validated<PutUserBookRoleRequest>()

    accessValidator.resolveScoped(ScopedPermission.Books.Roles.Write(bookId), user.id)

    val role = MemberRole.entries.find { it.value == body.role }!!
    memberService.setMemberRole(userId, bookId, role)

    call.respond(HttpStatusCode.NoContent)
}