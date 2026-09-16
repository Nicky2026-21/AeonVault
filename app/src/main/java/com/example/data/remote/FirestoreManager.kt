package com.example.data.remote

import android.util.Log
import com.example.data.model.VaultItem
import com.example.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreManager"

    suspend fun syncUser(user: User) {
        try {
            db.collection("users").document(user.id)
                .set(user, SetOptions.merge())
                .await()
            Log.d(TAG, "User ${user.id} synced to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user: ${e.message}")
        }
    }

    suspend fun syncVaultItem(item: VaultItem) {
        try {
            db.collection("vault_items").document(item.id)
                .set(item, SetOptions.merge())
                .await()
            Log.d(TAG, "Item ${item.id} synced to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing item: ${e.message}")
        }
    }

    suspend fun deleteVaultItem(itemId: String) {
        try {
            db.collection("vault_items").document(itemId)
                .delete()
                .await()
            Log.d(TAG, "Item $itemId deleted from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}")
        }
    }

    suspend fun fetchUserVaultItems(userId: String): List<VaultItem> {
        return try {
            val snapshot = db.collection("vault_items")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(VaultItem::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items: ${e.message}")
            emptyList()
        }
    }
}
