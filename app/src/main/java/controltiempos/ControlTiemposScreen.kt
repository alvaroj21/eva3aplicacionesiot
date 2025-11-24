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
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Control de Tiempos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "${tiempoLocal.toInt()} segundos",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                Slider(
                    value = tiempoLocal,
                    onValueChange = { tiempoLocal = it },
                    valueRange = 3f..30f,
                    steps = 27,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
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
                            "Tiempo guardado",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(50.dp)
                ) {
                    Text(
                        "Guardar",
                        color = Color.White,
                        fontSize = 16.sp
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
