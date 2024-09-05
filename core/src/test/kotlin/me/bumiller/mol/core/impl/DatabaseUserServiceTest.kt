package me.bumiller.mol.core.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.database.table.User
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import me.bumiller.mol.database.table.UserProfile.Model as UserProfileModel

class DatabaseUserServiceTest {

    lateinit var mockUserRepo: UserRepository

    lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        mockUserRepo = mockk()

        userService = DatabaseUserService(mockUserRepo)
    }

    val models = (1..10).map {
        User.Model(
            it.toLong(),
            "email-$it",
            "username-$it",
            "password-$it",
            isEmailVerified = it % 2 == 0,
            if (it % 2 == 0) null else UserProfileModel(
                it.toLong(),
                LocalDate(2000, 1, 1).plus(1, DateTimeUnit.MONTH),
                "firstName-$it",
                "lastName-$it",
                "male"
            )
        )
    }

    @Test
    fun `getAll returns all users and calls repo`() = runTest {
        coEvery { mockUserRepo.getAll() } returns models

        val returned = userService.getAll()

        assertEquals(models.size, returned.size)
        coVerify { mockUserRepo.getAll() }
    }

    @Test
    fun `getSpecific forwards nullable arguments properly`() = runTest {
        val idSlot = slot<Optional<Long>>()
        val emailSlot = slot<Optional<String>>()
        val usernameSlot = slot<Optional<String>>()

        coEvery {
            mockUserRepo.getSpecific(
                capture(idSlot),
                capture(usernameSlot),
                capture(emailSlot)
            )
        } returns models.first()

        userService.getSpecific(
            id = 1L
        )
        assertEquals(
            Optional.of(1L),
            idSlot.captured
        )
        assertEquals(
            Optional.empty<String>(),
            emailSlot.captured
        )
        assertEquals(
            Optional.empty<String>(),
            usernameSlot.captured
        )

        userService.getSpecific(
            email = "email"
        )
        assertEquals(
            Optional.empty<Long>(),
            idSlot.captured
        )
        assertEquals(
            Optional.of("email"),
            emailSlot.captured
        )
        assertEquals(
            Optional.empty<String>(),
            usernameSlot.captured
        )

        userService.getSpecific(
            username = "username",
            id = 5L
        )
        assertEquals(
            Optional.of(5L),
            idSlot.captured
        )
        assertEquals(
            Optional.empty<String>(),
            emailSlot.captured
        )
        assertEquals(
            Optional.of("username"),
            usernameSlot.captured
        )

        userService.getSpecific(
            email = "email",
            id = 6L
        )
        assertEquals(
            Optional.of(6L),
            idSlot.captured
        )
        assertEquals(
            Optional.of("email"),
            emailSlot.captured
        )
        assertEquals(
            Optional.empty<String>(),
            usernameSlot.captured
        )
    }

    @Test
    fun `createUser passes arguments and creates correct user model`() = runTest {
        val userModelSlot = slot<User.Model>()

        coEvery { mockUserRepo.create(capture(userModelSlot)) } returns models.first()

        userService.createUser("email", "password", "username")

        assertEquals(
            User.Model(-1L, "email", "username", "password", false, null),
            userModelSlot.captured
        )
    }

    @Test
    fun `createProfile returns null when user is not found`() = runTest {
        coEvery { mockUserRepo.getSpecific(1L) } returns null

        val mockProfile = mockk<UserProfile>()

        val result = userService.createProfile(1L, mockProfile)

        assertNull(result)
    }

    @Test
    fun `createProfile returns user with added profile`() = runTest {
        coEvery { mockUserRepo.update(any()) } answers { t -> t.invocation.args.first() as User.Model }  // Returns argument

        val user = models.first()
        coEvery { mockUserRepo.getSpecific(1L) } returns user

        val profile = UserProfile(1L, LocalDate(2000, 1, 1), Gender.Male, "firstname", "lastname")
        val userWithProfile = userService.createProfile(1L, profile)

        assertEquals(models.first().email, userWithProfile?.email)
        assertEquals(profile, userWithProfile?.profile)
    }

    @Test
    fun `deleteUser returns null when not found`() = runTest {
        coEvery { mockUserRepo.delete(1L) } returns null

        val deleted = userService.deleteUser(1L)

        assertNull(deleted)
    }

    @Test
    fun `deleteUser returns user`() = runTest {
        coEvery { mockUserRepo.delete(1L) } returns models[0]

        val deleted = userService.deleteUser(1L)

        assertEquals(models[0].email, deleted?.email)
        assertEquals(models[0].username, deleted?.username)
        assertEquals(models[0].id, deleted?.id)
        assertEquals(models[0].password, deleted?.password)
    }

}