package monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MonitoreoScreen() {
    var distancia by remember { mutableStateOf(100f) }
    var nivelLuz by remember { mutableStateOf(500) }
    
    val database = FirebaseDatabase.getInstance()
    val distanciaRef = database.getReference("distanciaActual")
    val luzRef = database.getReference("luz")

    // Leer valores iniciales desde Firebase
    LaunchedEffect(Unit) {
        distanciaRef.get().addOnSuccessListener { snapshot ->
            distancia = snapshot.getValue(Float::class.java) ?: 100f
        }
        luzRef.get().addOnSuccessListener { snapshot ->
            nivelLuz = snapshot.getValue(Int::class.java) ?: 500
        }
    }

    // Escuchar cambios en tiempo real de distancia
    DisposableEffect(Unit) {
        val listenerDistancia = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevaDistancia = snapshot.getValue(Float::class.java)
                if (nuevaDistancia != null) {
                    distancia = nuevaDistancia
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MonitoreoDistancia", "Error: ${error.message}")
            }
        }
        distanciaRef.addValueEventListener(listenerDistancia)
        onDispose { distanciaRef.removeEventListener(listenerDistancia) }
    }

    // Escuchar cambios en tiempo real de luz
    DisposableEffect(Unit) {
        val listenerLuz = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoNivel = snapshot.getValue(Int::class.java)
                if (nuevoNivel != null) {
                    nivelLuz = nuevoNivel
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MonitoreoLuz", "Error: ${error.message}")
            }
        }
        luzRef.addValueEventListener(listenerLuz)
        onDispose { luzRef.removeEventListener(listenerLuz) }
    }

    // Interfaz unificada
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Monitoreo de Sensores",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF424242)
        )

        // SECCIÓN DISTANCIA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    distancia < 30 -> Color(0xFFFFEBEE)
                    distancia in 30f..80f -> Color(0xFFFFF8E1)
                    else -> Color(0xFFE8F5E9)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sensor de Distancia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${distancia.toInt()} cm",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        distancia < 30 -> "Vehículo muy cerca"
                        distancia in 30f..80f -> "Vehículo detectado"
                        distancia >= 999 -> "Sin sensor"
                        else -> "Sin detección"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        distancia < 30 -> Color(0xFFEF5350)
                        distancia in 30f..80f -> Color(0xFFFF9800)
                        else -> Color(0xFF66BB6A)
                    }
                )
            }
        }

        // SECCIÓN LUZ
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    nivelLuz < 100 -> Color(0xFF37474F)
                    nivelLuz in 100..400 -> Color(0xFF78909C)
                    nivelLuz in 400..800 -> Color(0xFFFFF59D)
                    else -> Color(0xFFFFEB3B)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sensor de Luz Ambiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (nivelLuz < 400) Color.White else Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${nivelLuz} lux",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (nivelLuz < 400) Color.White else Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        nivelLuz < 100 -> "Oscuro"
                        nivelLuz in 100..400 -> "Penumbra"
                        nivelLuz in 400..800 -> "Normal"
                        else -> "Muy brillante"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (nivelLuz < 400) Color(0xFFE0E0E0) else Color(0xFF616161)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

