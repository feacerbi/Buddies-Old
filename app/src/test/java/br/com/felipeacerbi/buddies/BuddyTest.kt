package br.com.felipeacerbi.buddies

import br.com.felipeacerbi.buddies.models.Buddy
import com.google.firebase.database.DataSnapshot
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Created by felipe.acerbi on 14/07/2017.
 */
class BuddyTest {

    val testName = "Test Name"
    val testBreed = "Test Breed"
    val testTagId = "testtagid123"
    val testUsername = "testusername"
    val testUsernameTwo = "testusernametwo"
    val testUsernameThree = "testusernamethree"
    val testUsernameFour = "testusernamefour"
    val testOwnsMap = mapOf(Pair(testUsername, true), Pair(testUsernameTwo, false))
    val testFollowsMap = mapOf(Pair(testUsernameThree, false), Pair(testUsernameFour, true))

    fun createTestBuddy() = Buddy(testName, testBreed, testTagId, testOwnsMap, testFollowsMap)

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
        `when`(mockData.child(Buddy.DATABASE_OWNERS_CHILD)).thenReturn(ownsMockData)
        `when`(mockData.child(Buddy.DATABASE_FOLLOWERS_CHILD)).thenReturn(followsMockData)

        val buddy = Buddy(mockData)

        Assert.assertTrue(testName == buddy.name)
        Assert.assertTrue(testBreed == buddy.breed)
        Assert.assertTrue(testTagId == buddy.tagId)
        Assert.assertTrue(testOwnsMap.get(testUsername) == buddy.owners.get(testUsername))
        Assert.assertTrue(testOwnsMap.get(testUsernameTwo) == buddy.owners.get(testUsernameTwo))
        Assert.assertTrue(testFollowsMap.get(testUsernameThree) == buddy.followers.get(testUsernameThree))
        Assert.assertTrue(testFollowsMap.get(testUsernameFour) == buddy.followers.get(testUsernameFour))
    }

    @Test
    fun test_ToMap() {
        val buddy = createTestBuddy()

        val resultMap = buddy.toMap()

        Assert.assertTrue(testName == resultMap.get(Buddy.DATABASE_NAME_CHILD))
        Assert.assertTrue(testBreed == resultMap.get(Buddy.DATABASE_BREED_CHILD))
        Assert.assertTrue(testTagId == resultMap.get(Buddy.DATABASE_TAG_CHILD))
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_OWNERS_CHILD) as Map<String, Boolean>).get(testUsername) == true)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_OWNERS_CHILD) as Map<String, Boolean>).get(testUsernameTwo) == false)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_FOLLOWERS_CHILD) as Map<String, Boolean>).get(testUsernameThree) == false)
        Assert.assertTrue((resultMap.get(Buddy.DATABASE_FOLLOWERS_CHILD) as Map<String, Boolean>).get(testUsernameFour) == true)
    }

    @Test
    fun test_checkNull() {
        val buddy = createTestBuddy()
        Assert.assertTrue(buddy.checkNull("test"))
        Assert.assertFalse(buddy.checkNull(null))
    }

}