package br.com.felipeacerbi.buddies

import br.com.felipeacerbi.buddies.activities.LoginActivity
import org.junit.Assert
import org.junit.Test

class LoginUnitTest {

    @Test
    fun test_isPasswordValid() {
        val activity = LoginActivity()

        val shortPassword = "11111"
        val validPassword = "111111"

        Assert.assertTrue(activity.isPasswordValid(validPassword))
        Assert.assertFalse(activity.isPasswordValid(shortPassword))
    }

    @Test
    fun test_isEmailValid() {
        val activity = LoginActivity()

        val wrongEmail = "testgmail.com"
        val validEmail = "test@gmail.com"

        Assert.assertTrue(activity.isEmailValid(validEmail))
        Assert.assertFalse(activity.isEmailValid(wrongEmail))
    }
}
