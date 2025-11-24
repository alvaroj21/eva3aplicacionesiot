package deteccioncontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ControlDeteccionScreen() {
    var distanciaDeteccion by remember { mutableStateOf(10f) } // Distancia de detección (umbral)
    var distanciaActual by remember { mutableStateOf(0f) } // Distancia medida por el sensor
    var mostrarMensaje by remember { mutableStateOf(false) }

    // Referencia a Firebase
    val database = FirebaseDatabase.getInstance()
    val distanciaDeteccionRef = database.getReference("distanciaDeteccion") // Umbral configurado
    val distanciaActualRef = database.getReference("distanciaActual") // Lectura del sensor

    // Leer valores iniciales desde Firebase
    LaunchedEffect(Unit) {
        // Leer distancia de detección (umbral)
        distanciaDeteccionRef.get().addOnSuccessListener { snapshot ->
            val valor = snapshot.getValue(Int::class.java)
            if (valor != null) {
                distanciaDeteccion = valor.toFloat()
                Log.d("ControlDeteccion", "Distancia de detección inicial: $valor cm")
            }
        }
        
        // Leer distancia actual (sensor)
        distanciaActualRef.get().addOnSuccessListener { snapshot ->
            val valor = snapshot.getValue(Long::class.java)
            if (valor != null) {
                distanciaActual = valor.toFloat()
                Log.d("ControlDeteccion", "Distancia actual del sensor: $valor cm")
            }
        }
    }

    // Escuchar cambios en tiempo real de la distancia medida por el sensor
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val valor = snapshot.getValue(Long::class.java)
                if (valor != null) {
                    distanciaActual = valor.toFloat()
                    Log.d("ControlDeteccion", "Sensor actualizado: $valor cm")
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("ControlDeteccion", "Error al leer sensor: ${error.message}")
            }
        }

        distanciaActualRef.addValueEventListener(listener)

        onDispose {
            distanciaActualRef.removeEventListener(listener)
            Log.d("ControlDeteccion", "Listener removido")
        }
    }

    // Mostrar contenido directamente
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Control de Detección",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "${distanciaActual.toInt()} cm",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = if (distanciaActual <= distanciaDeteccion && distanciaActual > 0) 
                Color.Red 
            else 
                Color.Black
        )
        
        if (distanciaActual <= distanciaDeteccion && distanciaActual > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "¡Detectado!",
                fontSize = 16.sp,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Umbral: ${distanciaDeteccion.toInt()} cm",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Slider(
            value = distanciaDeteccion,
            onValueChange = { distanciaDeteccion = it },
            valueRange = 5f..100f,
            steps = 95,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("5 cm", fontSize = 12.sp, color = Color.Gray)
            Text("100 cm", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                mostrarMensaje = true
                distanciaDeteccionRef.setValue(distanciaDeteccion.toInt())
                    .addOnSuccessListener {
                        Log.d("ControlDeteccion", "Umbral guardado: ${distanciaDeteccion.toInt()} cm")
                    }

                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    mostrarMensaje = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp)
        ) {
            Text("Guardar", color = Color.White, fontSize = 16.sp)
        }

        AnimatedVisibility(visible = mostrarMensaje) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Guardado",
                    color = Color.Green,
                    fontSize = 14.sp
                )
            }
        }
    }
}
