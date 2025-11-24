package controlmodo

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

@Composable
fun ControlModoScreen() {
    var modoActual by remember { mutableStateOf("Manual") }
    val context = LocalContext.current

    val database = FirebaseDatabase.getInstance()
    val modoRef = database.getReference("modoOperacion")

    // Leer modo inicial desde Firebase
    LaunchedEffect(Unit) {
        modoRef.get().addOnSuccessListener { snapshot ->
            modoActual = snapshot.getValue(String::class.java) ?: "Manual"
            Log.d("ControlModo", "Modo inicial: $modoActual")
        }
    }

    // Escuchar cambios en tiempo real desde Firebase
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoModo = snapshot.getValue(String::class.java)
                if (nuevoModo != null && nuevoModo != modoActual) {
                    modoActual = nuevoModo
                    Log.d("ControlModo", "Modo actualizado desde Firebase: $nuevoModo")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ControlModo", "Error al leer Firebase: ${error.message}")
            }
        }
        modoRef.addValueEventListener(listener)
        onDispose { modoRef.removeEventListener(listener) }
    }

    // Mostrar la interfaz de control directamente
    Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Modo de Operación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF424242)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Indicador del modo actual
                Text(
                    text = "Modo actual:",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = when (modoActual) {
                        "Automatico" -> "Automático"
                        else -> modoActual
                    },
                    fontSize = 32.sp,
                    color = when (modoActual) {
                        "Automatico" -> Color(0xFF66BB6A)
                        "Manual" -> Color(0xFF42A5F5)
                        "Desactivado" -> Color(0xFFEF5350)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Modo Manual
                Button(
                    onClick = {
                        modoActual = "Manual"
                        modoRef.setValue("Manual")
                        Toast.makeText(context, "Modo Manual activado", Toast.LENGTH_SHORT).show()
                        Log.d("ControlModo", "Cambio a modo Manual")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modoActual == "Manual") Color(0xFF42A5F5) else Color(0xFFE3F2FD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Modo Manual",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (modoActual == "Manual") Color.White else Color(0xFF424242)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón Modo Automático
                Button(
                    onClick = {
                        modoActual = "Automatico"
                        modoRef.setValue("Automatico")
                        Toast.makeText(context, "Modo Automático activado", Toast.LENGTH_SHORT).show()
                        Log.d("ControlModo", "Cambio a modo Automatico")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modoActual == "Automatico") Color(0xFF66BB6A) else Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Modo Automático",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (modoActual == "Automatico") Color.White else Color(0xFF424242)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón Desactivar
                Button(
                    onClick = {
                        modoActual = "Desactivado"
                        modoRef.setValue("Desactivado")
                        Toast.makeText(context, "Sistema desactivado", Toast.LENGTH_SHORT).show()
                        Log.d("ControlModo", "Sistema desactivado")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modoActual == "Desactivado") Color(0xFFEF5350) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Desactivar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (modoActual == "Desactivado") Color.White else Color(0xFF424242)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

