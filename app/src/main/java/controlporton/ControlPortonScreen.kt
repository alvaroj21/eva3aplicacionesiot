package controlporton

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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun ControlPortonScreen() {
    var estadoPorton by remember { mutableStateOf("Cerrado") }
    var modoActual by remember { mutableStateOf("Manual") }
    var tiempoCierre by remember { mutableIntStateOf(5) }
    var tiempoRestante by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val database = FirebaseDatabase.getInstance()
    val portonRef = database.getReference("portonAbierto")  // Boolean - para enviar comandos
    val estadoPortonRef = database.getReference("estadoPorton")  // String - para leer estado real
    val modoRef = database.getReference("modoOperacion")
    val tiempoRef = database.getReference("tiemposPorton/tiempoCierre")
    val temporizadorRef = database.getReference("temporizador")

    // Leer estado inicial desde Firebase
    LaunchedEffect(Unit) {
        // Leer estado del portón (String): "Abierto", "Cerrado", "Abriendo", "Cerrando"
        estadoPortonRef.get().addOnSuccessListener { snapshot ->
            val estado = snapshot.getValue(String::class.java) ?: "Cerrado"
            estadoPorton = estado
            Log.d("ControlPorton", "Estado inicial del portón: $estadoPorton")
        }

        // Leer modo actual
        modoRef.get().addOnSuccessListener { snapshot ->
            modoActual = snapshot.getValue(String::class.java) ?: "Manual"
            Log.d("ControlPorton", "Modo actual: $modoActual")
        }

        // Leer tiempo de cierre
        tiempoRef.get().addOnSuccessListener { snapshot ->
            tiempoCierre = snapshot.getValue(Int::class.java) ?: 5
            Log.d("ControlPorton", "Tiempo de cierre: $tiempoCierre segundos")
        }
    }

    // Escuchar cambios en tiempo real desde Firebase
    DisposableEffect(Unit) {
        // Listener para estadoPorton (String) - El Arduino actualiza este campo
        val listenerEstado = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoEstado = snapshot.getValue(String::class.java) ?: "Cerrado"
                
                if (nuevoEstado != estadoPorton) {
                    estadoPorton = nuevoEstado
                    Log.d("ControlPorton", "Estado actualizado desde Arduino: $nuevoEstado")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlPorton", "Error al leer estadoPorton: ${error.message}")
            }
        }

        val listenerModo = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoModo = snapshot.getValue(String::class.java)
                if (nuevoModo != null && nuevoModo != modoActual) {
                    modoActual = nuevoModo
                    Log.d("ControlPorton", "Modo actualizado: $nuevoModo")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlPorton", "Error al leer modo: ${error.message}")
            }
        }

        val listenerTiempo = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoTiempo = snapshot.getValue(Int::class.java)
                if (nuevoTiempo != null && nuevoTiempo != tiempoCierre) {
                    tiempoCierre = nuevoTiempo
                    Log.d("ControlPorton", "Tiempo actualizado: $nuevoTiempo segundos")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlPorton", "Error al leer tiempo: ${error.message}")
            }
        }

        val listenerTemporizador = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoTemporizador = snapshot.getValue(Int::class.java)
                if (nuevoTemporizador != null && nuevoTemporizador != tiempoRestante) {
                    tiempoRestante = nuevoTemporizador
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlPorton", "Error al leer temporizador: ${error.message}")
            }
        }

        estadoPortonRef.addValueEventListener(listenerEstado)
        modoRef.addValueEventListener(listenerModo)
        tiempoRef.addValueEventListener(listenerTiempo)
        temporizadorRef.addValueEventListener(listenerTemporizador)

        onDispose {
            estadoPortonRef.removeEventListener(listenerEstado)
            modoRef.removeEventListener(listenerModo)
            tiempoRef.removeEventListener(listenerTiempo)
            temporizadorRef.removeEventListener(listenerTemporizador)
        }
    }

    // Verificar si el sistema está desactivado
    val sistemaDesactivado = modoActual == "Desactivado"

    // Mostrar la interfaz de control directamente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Control de Portón",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Indicador visual del estado
        Text(
            text = "Estado actual:",
            fontSize = 18.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (sistemaDesactivado) "Sistema Desactivado" else estadoPorton,
            fontSize = 32.sp,
            color = when {
                sistemaDesactivado -> Color(0xFF9E9E9E)
                estadoPorton == "Abierto" -> Color(0xFF66BB6A)
                estadoPorton == "Cerrado" -> Color(0xFFEF5350)
                estadoPorton == "Abriendo" -> Color(0xFFFF9800)
                estadoPorton == "Cerrando" -> Color(0xFFFF9800)
                else -> Color.Gray
            },
            fontWeight = FontWeight.Bold
        )

        // Mostrar información de cierre automático si está en modo automático
        if (modoActual == "Automatico" && estadoPorton == "Abierto" && !sistemaDesactivado) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Se cerrará automáticamente en $tiempoRestante segundos",
                fontSize = 14.sp,
                color = Color(0xFF42A5F5),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón Abrir Portón
        Button(
            onClick = {
                if (sistemaDesactivado) {
                    Toast.makeText(context, "Sistema desactivado. No se puede abrir el portón.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                if (estadoPorton == "Cerrado") {
                    Toast.makeText(context, "Abriendo portón...", Toast.LENGTH_SHORT).show()
                    
                    // Enviar comando a Firebase: true = ABRIR
                    portonRef.setValue(true).addOnSuccessListener {
                        Log.d("ControlPorton", "Comando ABRIR enviado a Firebase (portonAbierto = true)")
                    }.addOnFailureListener { error ->
                        Toast.makeText(context, "Error al enviar comando: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ControlPorton", "Error Firebase: ${error.message}")
                    }
                    
                    // El Arduino manejará el temporizador y actualizará el estado
                    // La app solo muestra los cambios que vienen de Firebase
                } else {
                    Toast.makeText(context, "El portón ya está abierto", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (estadoPorton == "Abierto") Color(0xFFBDBDBD) else Color(0xFF66BB6A),
                disabledContainerColor = Color(0xFFBDBDBD)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !sistemaDesactivado && estadoPorton != "Abierto" && estadoPorton != "Abriendo"
        ) {
            Text(
                text = "Abrir Portón",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Cerrar Portón
        Button(
            onClick = {
                if (sistemaDesactivado) {
                    Toast.makeText(context, "Sistema desactivado. No se puede cerrar el portón.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                if (estadoPorton == "Abierto") {
                    Toast.makeText(context, "Cerrando portón...", Toast.LENGTH_SHORT).show()
                    
                    // Enviar comando a Firebase: false = CERRAR
                    portonRef.setValue(false).addOnSuccessListener {
                        Log.d("ControlPorton", "Comando CERRAR enviado a Firebase (portonAbierto = false)")
                    }.addOnFailureListener { error ->
                        Toast.makeText(context, "Error al enviar comando: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ControlPorton", "Error Firebase: ${error.message}")
                    }
                    
                    // El Arduino manejará el estado y actualizará Firebase
                    // La app solo muestra los cambios que vienen de Firebase
                } else {
                    Toast.makeText(context, "El portón ya está cerrado", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (estadoPorton == "Cerrado") Color(0xFFBDBDBD) else Color(0xFFEF5350),
                disabledContainerColor = Color(0xFFBDBDBD)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !sistemaDesactivado && estadoPorton != "Cerrado" && estadoPorton != "Cerrando"
        ) {
            Text(
                text = "Cerrar Portón",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

