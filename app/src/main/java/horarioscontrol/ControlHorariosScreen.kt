package horarioscontrol

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.database.DatabaseReference
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults



@Composable
fun ControlHorariosScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val horariosRef = database.getReference("horarios")

    var horaApertura by remember { mutableStateOf("Sin definir") }
    var horaCierre by remember { mutableStateOf("Sin definir") }

    // Leer valores iniciales desde Firebase
    LaunchedEffect(Unit) {
        horariosRef.child("apertura").get().addOnSuccessListener {
            horaApertura = it.value?.toString() ?: "Sin definir"
        }
        horariosRef.child("cierre").get().addOnSuccessListener {
            horaCierre = it.value?.toString() ?: "Sin definir"
        }
    }

    // Mostrar contenido directamente (sin botón, sin ventana emergente)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Horarios Automáticos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar horarios actuales
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Apertura: $horaApertura", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cierre: $horaCierre", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón Editar Apertura
        Button(
            onClick = {
                val picker = TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val hora = String.format("%02d:%02d", hour, minute)
                        horariosRef.child("apertura").setValue(hora)
                        horaApertura = hora
                        Toast.makeText(context, "Apertura guardada: $hora", Toast.LENGTH_SHORT).show()
                    },
                    8, 0, true
                )
                picker.show()
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
        ) {
            Text("Editar Hora de Apertura", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Editar Cierre
        Button(
            onClick = {
                val picker = TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val hora = String.format("%02d:%02d", hour, minute)
                        horariosRef.child("cierre").setValue(hora)
                        horaCierre = hora
                        Toast.makeText(context, "Cierre guardado: $hora", Toast.LENGTH_SHORT).show()
                    },
                    18, 0, true
                )
                picker.show()
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
        ) {
            Text("Editar Hora de Cierre", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

