package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
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

  @Test
  fun `verify folder creation and query`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.repository.VaultRepository(context)
    // Wait for current user initialization
    var user = repository.currentUser.value
    var attempts = 0
    while (user == null && attempts < 20) {
      kotlinx.coroutines.delay(100)
      user = repository.currentUser.value
      attempts++
    }
    println("DEBUG user after wait: $user")
    
    val testFolder = repository.createFolder("Test Quantum Dir", null)
    println("DEBUG testFolder: $testFolder")
    org.junit.Assert.assertTrue("Folder creation failed: $testFolder", testFolder.isSuccess)
    
    val itemsFlow = repository.getItemsInFolder(null)
    val items = itemsFlow.first()
    println("DEBUG items in folder null: ${items.map { it.name to it.parentId to it.isFolder }}")
    val found = items.any { it.name == "Test Quantum Dir" && it.isFolder }
    org.junit.Assert.assertTrue("Created folder not found in getItemsInFolder(null). All: ${items.map { it.name }}", found)
  }
}
