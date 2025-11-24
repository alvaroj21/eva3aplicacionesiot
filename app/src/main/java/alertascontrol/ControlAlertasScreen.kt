package alertascontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.util.Log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.google.firebase.database.ServerValue

@Composable
fun ControlAlertasScreen() {
    var mostrarAlertas by remember { mutableStateOf(false) }
    var distancia by remember { mutableStateOf(0f) }
    var estado by remember { mutableStateOf("Normal") }

    val database = FirebaseDatabase.getInstance()
    val distanciaRef = database.getReference("distanciaActual")
    val alertasRef = database.getReference("alertas") // Nuevo nodo de registro automático

    // Escucha de cambios en Firebase desde la terminal subiendo el volumen del PC
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valor = snapshot.getValue(Float::class.java)
                if (valor != null) {
                    distancia = valor
                    estado = when {
                        valor < 20 -> "Crítico"
                        valor in 20.0..50.0 -> "Peligro"
                        else -> "Normal"
                    }

                    // Avisa una notificacion por Logcat en tiempo real de mensaje
                    Log.d("ControlAlertas", "Distancia: $valor cm | Estado: $estado")

                    // Registro automático en Firebase al poner un valor
                    val nuevaAlerta = mapOf(
                        "distancia" to valor,
                        "estado" to estado,
                        "timestamp" to ServerValue.TIMESTAMP
                    )
                    alertasRef.push().setValue(nuevaAlerta)
                    // limpieza automatica de historiales
                    // conserva la cantidad limitada de numeros
                    // ajustados
                    alertasRef.limitToFirst(10).get().addOnSuccessListener { if (it.childrenCount > 10) it.children.take((it.childrenCount - 10).toInt()).forEach { c -> c.ref.removeValue() } }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlAlertas", "Error en Firebase: ${error.message}")
            }
        }
        distanciaRef.addValueEventListener(listener)
        onDispose { distanciaRef.removeEventListener(listener) }
    }

    // Mostrar contenido directamente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Control de Alertas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "${distancia.toInt()} cm",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(15.dp))
        
        Text(
            text = "Estado: $estado",
            fontSize = 20.sp,
            color = when (estado) {
                "Crítico" -> Color.Red
                "Peligro" -> Color(0xFFFF9800)
                else -> Color.Green
            },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
            horizontalAlignment = Alignment.Start
        ) {
            Text("• Crítico: < 20 cm", fontSize = 14.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Peligro: 20-50 cm", fontSize = 14.sp, color = Color(0xFFFF9800))
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Normal: > 50 cm", fontSize = 14.sp, color = Color.Green)
        }
    }
}

