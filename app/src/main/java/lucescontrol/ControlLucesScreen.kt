package lucescontrol

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun ControlLucesScreen() {

    val database = FirebaseDatabase.getInstance()
    val lucesRef = database.getReference("lucesHorarios")

    val context = LocalContext.current

    var horaEncendido by remember { mutableStateOf("18:00") }
    var horaApagado by remember { mutableStateOf("06:00") }
    var horaActual by remember { mutableStateOf("") }

    // Función para verificar si el LED debería estar encendido según horarios
    fun estaEncendidoSegunHorario(horaActual: String, encendido: String, apagado: String): Boolean {
        try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val actual = formato.parse(horaActual)
            val on = formato.parse(encendido)
            val off = formato.parse(apagado)
            
            if (actual == null || on == null || off == null) return false
            
            // Si la hora de apagado es menor que la de encendido, significa que cruza medianoche
            return if (off.before(on)) {
                // Ejemplo: encendido 18:00, apagado 06:00
                actual.after(on) || actual.before(off) || actual == on
            } else {
                // Ejemplo: encendido 06:00, apagado 18:00
                (actual.after(on) || actual == on) && actual.before(off)
            }
        } catch (e: Exception) {
            Log.e("ControlLuces", "Error al calcular estado: ${e.message}")
            return false
        }
    }

    // --- Lectura inicial desde Firebase ---
    LaunchedEffect(Unit) {
        lucesRef.get().addOnSuccessListener { snapshot ->
            horaEncendido = snapshot.child("encendido").getValue(String::class.java) ?: "18:00"
            horaApagado = snapshot.child("apagado").getValue(String::class.java) ?: "06:00"
            Log.d("ControlLuces", "Horarios iniciales → Encendido=$horaEncendido | Apagado=$horaApagado")
        }
    }

    // Actualizar hora actual cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            horaActual = formato.format(Date())
            delay(1000) // Actualizar cada segundo
        }
    }

    // Escuchar cambios en los horarios desde Firebase
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                horaEncendido = snapshot.child("encendido").getValue(String::class.java) ?: "18:00"
                horaApagado = snapshot.child("apagado").getValue(String::class.java) ?: "06:00"
                Log.d("ControlLuces", "Horarios actualizados → Encendido=$horaEncendido | Apagado=$horaApagado")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlLuces", "Error al escuchar horarios: ${error.message}")
            }
        }
        lucesRef.addValueEventListener(listener)
        onDispose { lucesRef.removeEventListener(listener) }
    }

    // Calcular estado actual del LED basándose en horarios
    val estadoLed = if (horaActual.isNotEmpty()) {
        if (estaEncendidoSegunHorario(horaActual, horaEncendido, horaApagado)) "ON" else "OFF"
    } else {
        "OFF"
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
        Text("Control de Luces", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = if (estadoLed == "ON") "ENCENDIDO" else "APAGADO",
            color = if (estadoLed == "ON") Color.Green else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Horarios Automáticos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Encendido: $horaEncendido", fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Editar", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Apagado: $horaApagado", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Editar", color = Color.White, fontSize = 16.sp)
                }
            }
        }

