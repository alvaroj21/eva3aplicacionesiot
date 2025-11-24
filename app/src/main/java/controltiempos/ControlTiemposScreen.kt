package controltiempos

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
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.widget.Toast

@Composable
fun ControlTiemposScreen() {
    var tiempoCierre by remember { mutableStateOf(5) } // En segundos
    val context = LocalContext.current
    var tiempoLocal by remember { mutableStateOf(tiempoCierre.toFloat()) }

    val database = FirebaseDatabase.getInstance()
    val tiemposRef = database.getReference("tiemposPorton")

    // Leer valor inicial desde Firebase
    LaunchedEffect(Unit) {
        tiemposRef.child("tiempoCierre").get().addOnSuccessListener { snapshot ->
            tiempoCierre = snapshot.getValue(Int::class.java) ?: 5
            tiempoLocal = tiempoCierre.toFloat()
            Log.d("ControlTiempos", "Tiempo de cierre inicial: $tiempoCierre segundos")
        }
    }

    // Mostrar la interfaz de control directamente
    Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Control de Tiempos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF424242)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Tiempo de cierre automático",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Indicador del tiempo actual
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${tiempoLocal.toInt()}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                        Text(
                            text = "segundos",
                            fontSize = 18.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Slider para ajustar tiempo de cierre
                Slider(
                    value = tiempoLocal,
                    onValueChange = { tiempoLocal = it },
                    valueRange = 3f..30f,
                    steps = 27,
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
                    Text("3 seg", fontSize = 12.sp, color = Color.Gray)
                    Text("30 seg", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        tiempoCierre = tiempoLocal.toInt()
                        tiemposRef.child("tiempoCierre").setValue(tiempoCierre)
                            .addOnSuccessListener {
                                Log.d("ControlTiempos", "Tiempo guardado: $tiempoCierre segundos")
                            }
                        Toast.makeText(
                            context,
                            "Tiempo ajustado a ${tiempoLocal.toInt()} segundos",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp)
                ) {
                    Text(
                        "Guardar tiempo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

// APUNTES:
// - Permite configurar el tiempo de cierre automático (3-30 segundos)
// - Los valores se guardan en Firebase en tiempo real
// - Útil para modo automático: el portón se cierra solo después de X segundos
// - Todos los cambios se registran en Logcat
// - Sigue la misma estructura que los demás controles (botón en barra navegadora + pantalla emergente)
// - Interfaz simplificada enfocada solo en el tiempo de cierre automático
// - Usa Slider para ajustar el tiempo de forma intuitiva
// - Muestra vista previa en tiempo real del tiempo seleccionado
// - Botón "Guardar tiempo" para confirmar los cambios
