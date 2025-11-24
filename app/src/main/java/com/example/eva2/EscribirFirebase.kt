package com.example.eva2

import com.google.firebase.database.DatabaseReference

// Función para escribir el estado del LED en Firebase
fun escribirEstadoLed(ledRef: DatabaseReference, nuevoEstado: String) {
    ledRef.setValue(nuevoEstado)
}