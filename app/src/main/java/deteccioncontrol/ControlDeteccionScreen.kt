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
            .padding(24.dp)
    ) {
        Text(
            text = "Control de Detección",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar distancia actual del sensor
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (distanciaActual <= distanciaDeteccion && distanciaActual > 0) 
                    Color(0xFFFFEBEE) // Rojo claro cuando detecta
                else 
                    Color(0xFFF5F5F5) // Gris cuando no detecta
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sensor de Distancia",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${distanciaActual.toInt()} cm",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (distanciaActual <= distanciaDeteccion && distanciaActual > 0) 
                        Color(0xFFEF5350) 
                    else 
                        Color(0xFF424242)
                )
                if (distanciaActual <= distanciaDeteccion && distanciaActual > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¡Vehículo detectado!",
                        fontSize = 12.sp,
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Configurar distancia de detección
        Text(
            text = "Distancia de detección:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mostrar umbral configurado
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${distanciaDeteccion.toInt()} cm",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF42A5F5)
                )
                Text(
                    text = "Umbral de detección",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ajustar umbral de detección:",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = distanciaDeteccion,
            onValueChange = { distanciaDeteccion = it },
            valueRange = 5f..100f,
            steps = 95,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF42A5F5),
                activeTrackColor = Color(0xFF42A5F5),
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores de rango
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("5 cm", fontSize = 12.sp, color = Color.Gray)
            Text("100 cm", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                containerColor = Color(0xFF42A5F5)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(60.dp)
        ) {
            Text("Guardar Distancia", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        AnimatedVisibility(visible = mostrarMensaje) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Distancia de detección actualizada",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
