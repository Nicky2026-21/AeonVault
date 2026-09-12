package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ÆonVault", appName)
  }

  @Test
  fun `verify storage size formatting`() {
    assertEquals("0 B", com.example.data.model.User.formatStorageSize(0L))
    assertEquals("1 KB", com.example.data.model.User.formatStorageSize(1024L))
    assertEquals("10 MB", com.example.data.model.User.formatStorageSize(10L * 1024 * 1024))
  }
}
