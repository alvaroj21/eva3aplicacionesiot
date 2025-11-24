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
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Monitoreo de Sensores",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // DISTANCIA
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sensor de Distancia",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${distancia.toInt()} cm",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    distancia < 30 -> "Muy cerca"
                    distancia in 30f..80f -> "Detectado"
                    distancia >= 999 -> "Sin sensor"
                    else -> "Normal"
                },
                fontSize = 14.sp,
                color = when {
                    distancia < 30 -> Color.Red
                    distancia in 30f..80f -> Color(0xFFFF9800)
                    else -> Color.Green
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(20.dp))

        // LUZ
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sensor de Luz",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${nivelLuz} lux",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    nivelLuz > 800 -> "Oscuro"
                    nivelLuz in 400..800 -> "Penumbra"
                    nivelLuz in 100..400 -> "Normal"
                    else -> "Muy brillante"
                },
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

