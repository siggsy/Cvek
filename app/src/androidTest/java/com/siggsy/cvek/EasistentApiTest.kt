package com.siggsy.cvek

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.siggsy.cvek.data.easistent.EasistentApi
import com.siggsy.cvek.data.easistent.TokenQueryFailed
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EasistentApiTest {

    @Test
    fun tokenFailedTest() = runBlocking {

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        var eaApi: EasistentApi? = null
        try {
            eaApi = EasistentApi.getInstance(
                appContext,
                "invalid_username",
                "invalid_password"
            )
        } catch (e: TokenQueryFailed) {
            Assert.assertNotNull(e)
        }
        Assert.assertNull(eaApi)

    }

}