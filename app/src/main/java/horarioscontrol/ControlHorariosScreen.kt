package horarioscontrol

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.database.FirebaseDatabase


@SuppressLint("DefaultLocale")
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
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Horarios Automáticos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text("Apertura: $horaApertura", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Cierre: $horaCierre", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(40.dp))

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
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("Editar Apertura", color = Color.White, fontSize = 16.sp)
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
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Editar Cierre", color = Color.White, fontSize = 16.sp)
        }
    }
}

