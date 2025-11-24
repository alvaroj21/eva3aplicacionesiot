package deteccioncontrol

import com.google.firebase.database.FirebaseDatabase
import android.util.Log

fun escribirDistancia(valor: Float) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("distanciaActual")

    ref.setValue(valor.toInt())
        .addOnSuccessListener {
            Log.d("FirebaseEscritura", " Distancia guardada: $valor cm")
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseEscritura", " Error al guardar: ${e.message}")
        }
}