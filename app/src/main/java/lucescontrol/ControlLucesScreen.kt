package lucescontrol

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import com.google.firebase.database.*
import java.util.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun ControlLucesScreen() {

    val database = FirebaseDatabase.getInstance()
    val lucesRef = database.getReference("lucesHorarios")
    val ledRef = database.getReference("ledEstado")

    val context = LocalContext.current

    var horaEncendido by remember { mutableStateOf("18:00") }
    var horaApagado by remember { mutableStateOf("06:00") }
    var estadoLed by remember { mutableStateOf("Desconocido") }
    var mostrarLuces by remember { mutableStateOf(false) }

    // --- Lectura inicial desde Firebase ---
    LaunchedEffect(Unit) {
        lucesRef.get().addOnSuccessListener { snapshot ->
            horaEncendido = snapshot.child("encendido").getValue(String::class.java) ?: "18:00"
            horaApagado = snapshot.child("apagado").getValue(String::class.java) ?: "06:00"
            Log.d("ControlLuces", "Horarios iniciales → Encendido=$horaEncendido | Apagado=$horaApagado")
        }
        
        // Leer estado inicial del LED
        ledRef.get().addOnSuccessListener { snapshot ->
            estadoLed = snapshot.getValue(String::class.java) ?: "OFF"
            Log.d("ControlLuces", "Estado LED inicial: $estadoLed")
        }
    }

    // ver el LED en tiempo real desde terminal o firebase
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoEstado = snapshot.getValue(String::class.java) ?: "OFF"
                if (nuevoEstado != estadoLed) {
                    estadoLed = nuevoEstado
                    Log.d("ControlLuces", "Estado LED actualizado: $estadoLed")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlLuces", "Error al escuchar LED: ${error.message}")
            }
        }
        ledRef.addValueEventListener(listener)
        onDispose { ledRef.removeEventListener(listener) }
    }

    // Mostrar contenido directamente
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Control de Luces", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(32.dp))

        // Card con estado del LED
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (estadoLed) {
                    "ON" -> Color(0xFFE8F5E9)
                    "OFF" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFF5F5F5)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Estado del LED",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (estadoLed == "ON") "ENCENDIDO" else if (estadoLed == "OFF") "APAGADO" else "DESCONOCIDO",
                    color = when (estadoLed) {
                        "ON" -> Color(0xFF4CAF50)
                        "OFF" -> Color(0xFF757575)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Control automático por horario",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Control Manual (60 segundos)",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "El LED volverá a control automático después de 60 segundos",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para control manual del LED
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    ledRef.setValue("ON")
                    Log.d("ControlLuces", "LED encendido manualmente")
                },
                modifier = Modifier.weight(1f).height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Encender", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    ledRef.setValue("OFF")
                    Log.d("ControlLuces", "LED apagado manualmente")
                },
                modifier = Modifier.weight(1f).height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apagar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider(modifier = Modifier.fillMaxWidth(0.85f), color = Color(0xFFE0E0E0))

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Horarios Automáticos",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(16.dp))


                // --- ENCENDIDO ---
                Text("Encendido: $horaEncendido", fontSize = 15.sp)
                Button(
                    onClick = {
                        val calendario = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hora, minuto ->
                                val nuevaHora = String.format("%02d:%02d", hora, minuto)
                                horaEncendido = nuevaHora

                                // Genera y registra log desde la terminal
                                val logHora = SimpleDateFormat("HH:mm:ss a", Locale.getDefault()).format(Date())
                                Log.d("ControlLuces", "Se ha editado en Control de Luces el ENCENDIDO a las: $nuevaHora (registrado $logHora)")

                                // Guardar en Firebase con log de confirmación
                                lucesRef.child("encendido").setValue(nuevaHora)
                                    .addOnSuccessListener {
                                        Log.d("ControlLuces", "Firebase: Horario de ENCENDIDO guardado correctamente → $nuevaHora")
                                    }
                                    .addOnFailureListener {
                                        Log.e("ControlLuces", "Firebase: Error al guardar horario de ENCENDIDO → ${it.message}")
                                    }
                            },
                            calendario.get(Calendar.HOUR_OF_DAY),
                            calendario.get(Calendar.MINUTE),
                            true
                        ).apply { setTitle("Seleccionar hora de encendido") }.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Editar Encendido", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- APAGADO ---
                Text("Apagado: $horaApagado", fontSize = 15.sp)
                Button(
                    onClick = {
                        val calendario = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hora, minuto ->
                                val nuevaHora = String.format("%02d:%02d", hora, minuto)
                                horaApagado = nuevaHora

                                val logHora = SimpleDateFormat("HH:mm:ss a", Locale.getDefault()).format(Date())
                                Log.d("ControlLuces", "Se ha editado en Control de Luces el APAGADO a las: $nuevaHora (registrado $logHora)")

                                lucesRef.child("apagado").setValue(nuevaHora)
                                    .addOnSuccessListener {
                                        Log.d("ControlLuces", "Firebase: Horario de APAGADO guardado correctamente → $nuevaHora")
                                    }
                                    .addOnFailureListener {
                                        Log.e("ControlLuces", "Firebase: Error al guardar horario de APAGADO → ${it.message}")
                                    }
                            },
                            calendario.get(Calendar.HOUR_OF_DAY),
                            calendario.get(Calendar.MINUTE),
                            true
                        ).apply { setTitle("Seleccionar hora de apagado") }.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Text("Editar Apagado", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

