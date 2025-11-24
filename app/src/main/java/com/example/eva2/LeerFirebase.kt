package com.example.eva2

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

// Función para leer el estado del LED desde Firebase
fun leerEstadoLed(ledRef: DatabaseReference, callback: (String?) -> Unit) {
    ledRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val estado = snapshot.getValue(String::class.java)
            callback(estado)
        }

        override fun onCancelled(error: DatabaseError) {
            callback(null)
        }
    })
}