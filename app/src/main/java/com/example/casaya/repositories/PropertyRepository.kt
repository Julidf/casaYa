package com.example.casaya.repositories

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.casaya.entities.Property
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.*

class PropertyRepository(){

    private val COLLECTION: String = "properties"

    //Inicializacion de una instancia de Firestore
    private val db = Firebase.firestore
    //Inicializacion de una instancia de Storage
    private val storage = Firebase.storage

    suspend fun savePropertyImage(uri: Uri, context: Context): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child(UUID.randomUUID().toString())
        try {
            imageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl
            }.addOnFailureListener { exception ->
                // Manejar el error en caso de que no se pueda subir la imagen
            }
        } catch (err: Exception) {
            showErrorDialog(err.toString(), context)
        }
        Log.d("AAAAAAAAAAAAAAAAAAAAAAAA", imageRef.toString())
        return imageRef.toString()
    }

    suspend fun getPropertyImage(propertyImageRef: String): Uri? {
        Log.d("ASDASDASDASDASDASD", propertyImageRef)
        val storageImageRef = storage.getReferenceFromUrl(propertyImageRef)
        Log.d("ZZZZZZZZZZZZZZZZZZZZZZZZZ", storageImageRef.toString())
        var storageUri: Uri? = null
        try {
            storageImageRef.downloadUrl.addOnSuccessListener { uri ->
                storageUri = uri
            }.addOnFailureListener { exception ->
                Log.e("ERRORERRORERRORERRORERROR1", "${exception.message}")
            }
        } catch (err: Exception){
            Log.e("ERRORERRORERRORERRORERROR2", "${err.message}")
        }
        return storageUri
    }

    private fun showErrorDialog(message: String, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Guarda una Property en la DB
     */
    suspend fun saveProperty(newProperty: Property) {
        try {
            val reference = db.collection(COLLECTION).document()

            db.collection(COLLECTION)
                .document(reference.id)
                .set(newProperty)
                .await()

            Log.d("New Property", "Se agrego exitosamente el documento con ID ${reference.id}")
        }catch (e: Exception) {
            Log.e("Error Message", "Exception thrown: ${e.message}")
        }
    }

    /**
     * Devuelve todas las propiedades almacenadas en la DB
     */
    suspend fun getAllProperties() : MutableList<Property> {
        var propertiesList = mutableListOf<Property>()
        try {
            val documents = db.collection(COLLECTION)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .await()

            propertiesList = documents.toObjects(Property::class.java)
        }catch (e: Exception) {
            Log.e("Error Message", "Exception thrown: ${e.message}")
        }

        return propertiesList
    }
}
