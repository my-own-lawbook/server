package me.bumiller.mol.core.impl

import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.database.repository.UserProfileRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.database.table.User
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import me.bumiller.mol.database.table.UserProfile.Model as UserProfileModel

class DatabaseUserServiceTest {

    lateinit var mockUserRepo: UserRepository
    lateinit var mockProfileRepo: UserProfileRepository

    lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        mockUserRepo = mockk()
        mockProfileRepo = mockk()

        userService = DatabaseUserService(mockUserRepo, mockProfileRepo)
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
            present(1L),
            idSlot.captured
        )
        assertEquals(
            empty<String>(),
            emailSlot.captured
        )
        assertEquals(
            empty<String>(),
            usernameSlot.captured
        )

        userService.getSpecific(
            email = "email"
        )
        assertEquals(
            empty<Long>(),
            idSlot.captured
        )
        assertEquals(
            present("email"),
            emailSlot.captured
        )
        assertEquals(
            empty<String>(),
            usernameSlot.captured
        )

        userService.getSpecific(
            username = "username",
            id = 5L
        )
        assertEquals(
            present(5L),
            idSlot.captured
        )
        assertEquals(
            empty<String>(),
            emailSlot.captured
        )
        assertEquals(
            present("username"),
            usernameSlot.captured
        )

        userService.getSpecific(
            email = "email",
            id = 6L
        )
        assertEquals(
            present(6L),
            idSlot.captured
        )
        assertEquals(
            present("email"),
            emailSlot.captured
        )
        assertEquals(
            empty<String>(),
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

    val userNoProfile = User.Model(1L, "email", "username", "password", true, null)
    val userWithProfile = userNoProfile.copy(
        profile = UserProfileModel(1L, LocalDate(2000, 1, 1), "firstname", "lastname", "male")
    )

    @Test
    fun `createProfile returns user with added profile`() = runTest {
        coEvery { mockUserRepo.update(any()) } answers { t -> t.invocation.args.first() as User.Model }  // Returns argument

        coEvery { mockUserRepo.getSpecific(1L) } returns userNoProfile
        coEvery { mockProfileRepo.create(any()) } returns userWithProfile.profile!!

        val profile = UserProfile(1L, LocalDate(2000, 1, 1), Gender.Male, "firstname", "lastname")
        val updatedUser = userService.createProfile(1L, profile)

        coVerify(exactly = 1) { mockProfileRepo.create(any()) }

        assertEquals(userNoProfile.email, updatedUser?.email)
        assertEquals(profile, updatedUser?.profile)
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

    @Test
    fun `update returns null when user is not found`() = runTest {
        coEvery { mockUserRepo.getSpecific(1L) } returns null

        val returned = userService.update(1L)

        assertNull(returned)
    }

    val user = User.Model(1L, "email", "username", "password", true, null)

    @Test
    fun `update properly maps optionals to default value`() = runTest {
        coEvery { mockUserRepo.getSpecific(1L) } returns user
        coEvery { mockUserRepo.update(any()) } returns user

        userService.update(
            userId = 1L,
            email = present("email-1"),
            username = empty(),
            password = present("password-1"),
            isEmailVerified = empty()
        )

        coVerify { mockUserRepo.update(User.Model(1L, "email-1", user.username, "password-1", user.isEmailVerified, null)) }
    }

    @Test
    fun `update returns the user`() = runTest {
        coEvery { mockUserRepo.getSpecific(1L) } returns user
        coEvery { mockUserRepo.update(any()) } returns user

        val returned = userService.update(1L)

        assertEquals(user.id, returned?.id)
        assertEquals(user.email, returned?.email)
        assertEquals(user.username, returned?.username)
        assertEquals(user.password, returned?.password)
        assertEquals(user.isEmailVerified, returned?.isEmailVerified)
    }

    @Test
    fun `updateProfile returns null when user not found`() = runTest {
        coEvery { mockUserRepo.getSpecific(any<Long>()) } returns null

        val returned = userService.updateProfile(1L, empty(), empty(), empty(), empty())
        assertNull(returned)
    }

    @Test
    fun `updateProfile properly uses optional arguments`() = runTest {
        val userModelSlot = slot<UserProfileModel>()

        coEvery { mockUserRepo.getSpecific(any<Long>()) } returns userWithProfile
        coEvery { mockProfileRepo.update(capture(userModelSlot)) } returns userWithProfile.profile

        userService.updateProfile(
            userId = 1L,
            firstName = empty(),
            lastName = present("lastname-1"),
            birthday = empty(),
            gender = present(Gender.Disclosed)
        )

        assertEquals(userWithProfile.profile?.firstName, userModelSlot.captured.firstName)
        assertEquals("lastname-1", userModelSlot.captured.lastName)
        assertEquals(userWithProfile.profile?.birthday, userModelSlot.captured.birthday)
        assertEquals("disclosed", userModelSlot.captured.gender)
    }

}