package br.com.felipeacerbi.buddies

import br.com.felipeacerbi.buddies.models.Buddy
import com.google.firebase.database.DataSnapshot
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class BuddyTest {

    val testName = "Test Name"
    val testBreed = "Test Breed"
    val testPhoto = "testphotopath"
    val testTagId = "testtagid123"
    val testUsername = "testusername"
    val testUsernameTwo = "testusernametwo"
    val testUsernameThree = "testusernamethree"
    val testUsernameFour = "testusernamefour"
    val testOwnsMap = mapOf(Pair(testUsername, true), Pair(testUsernameTwo, false))
    val testFollowsMap = mapOf(Pair(testUsernameThree, false), Pair(testUsernameFour, true))

    fun createTestBuddy() = Buddy()//testName, testBreed, testPhoto, testTagId, testOwnsMap, testFollowsMap)

    @Test
    fun test_cleanConstructor() {
        Assert.assertNotNull(Buddy())
    }

    @Test
    fun test_fromMapConstructor() {
        val mockData = mock(DataSnapshot::class.java)

        val nameMockData = mock(DataSnapshot::class.java)
        `when`(nameMockData.value).thenReturn(testName)

        val breedMockData = mock(DataSnapshot::class.java)
        `when`(breedMockData.value).thenReturn(testBreed)

        val tagIdMockData = mock(DataSnapshot::class.java)
        `when`(tagIdMockData.value).thenReturn(testTagId)

        val ownsMockData = mock(DataSnapshot::class.java)
        `when`(ownsMockData.value).thenReturn(testOwnsMap)

        val followsMockData = mock(DataSnapshot::class.java)
        `when`(followsMockData.value).thenReturn(testFollowsMap)

        `when`(mockData.child(Buddy.DATABASE_NAME_CHILD)).thenReturn(nameMockData)
        `when`(mockData.child(Buddy.DATABASE_BREED_CHILD)).thenReturn(breedMockData)
        `when`(mockData.child(Buddy.DATABASE_TAG_CHILD)).thenReturn(tagIdMockData)
        `when`(mockData.child(Buddy.DATABASE_OWNS_CHILD)).thenReturn(ownsMockData)
        `when`(mockData.child(Buddy.DATABASE_FOLLOWS_CHILD)).thenReturn(followsMockData)

        val buddy = Buddy(mockData)

        Assert.assertTrue(testName == buddy.name)
        Assert.assertTrue(testBreed == buddy.breed)
        Assert.assertTrue(testTagId == buddy.tagId)
        Assert.assertTrue(testOwnsMap[testUsername] == buddy.owners[testUsername])
        Assert.assertTrue(testOwnsMap[testUsernameTwo] == buddy.owners[testUsernameTwo])
        Assert.assertTrue(testFollowsMap[testUsernameThree] == buddy.followers[testUsernameThree])
        Assert.assertTrue(testFollowsMap[testUsernameFour] == buddy.followers[testUsernameFour])
    }

    @Test
    fun test_ToMap() {
        val buddy = createTestBuddy()

        val resultMap = buddy.toMap()

        Assert.assertTrue(testName == resultMap[Buddy.DATABASE_NAME_CHILD])
        Assert.assertTrue(testBreed == resultMap[Buddy.DATABASE_BREED_CHILD])
        Assert.assertTrue(testTagId == resultMap[Buddy.DATABASE_TAG_CHILD])
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_OWNS_CHILD) as Map<String, Boolean>)[testUsername] == true)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_OWNS_CHILD) as Map<String, Boolean>)[testUsernameTwo] == false)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_FOLLOWS_CHILD) as Map<String, Boolean>)[testUsernameThree] == false)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_FOLLOWS_CHILD) as Map<String, Boolean>)[testUsernameFour] == true)
    }

    @Test
    fun test_checkNull() {
        val buddy = createTestBuddy()
        Assert.assertTrue(buddy.checkNull("test"))
        Assert.assertFalse(buddy.checkNull(null))
    }

}