package deteccioncontrol

import com.google.firebase.database.FirebaseDatabase
import android.util.Log

fun leerDistancia(onResultado: (Float) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("distanciaActual")

    ref.get()
        .addOnSuccessListener { snapshot ->
            val valor = snapshot.getValue(Float::class.java)
            if (valor != null) {
                onResultado(valor)
                Log.d("FirebaseLectura", " Distancia leída: $valor cm")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseLectura", " Error al leer: ${e.message}")
        }
}