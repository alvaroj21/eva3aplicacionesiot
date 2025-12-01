package com.example.eva2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.eva2.ui.theme.Eva2Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import android.content.Context
import barranavegacion.BarraNavegadoraSimple
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Inicializar Firebase
        FirebaseApp.initializeApp(this)

        //Escribir un valor de prueba directo a la firebase
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("mensaje_prueba")
        myRef.setValue("Conexión exitosa desde Android Studio")
            .addOnSuccessListener {
                Log.d("Firebase", "Dato enviado correctamente")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Error al enviar dato", it)

                // --- NUEVO BLOQUE PARA CONTROL DE LED ---
                val ledRef = database.getReference("ledEstado")



                //Escucha los cambios en tiempo real activando el volumen y los muestra en consola en el logcat
                ledRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val estado = snapshot.getValue(String::class.java)
                        Log.d("FirebaseLED", "Estado actual: $estado")  // se verá en Logcat
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseLED", "Error al leer datos", error.toException())
                    }
                })

            }

        //contenido de Compose
        setContent {
            Eva2Theme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var tipoUsuario by remember { mutableStateOf<String?>(null) }

                if (isLoggedIn && tipoUsuario != null) {
                    // Si es admin, muestra todas las pantallas; si es comun, solo portón
                    barranavegacion.BarraNavegadoraSimple(tipoUsuario = tipoUsuario!!)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                Log.d("FirebaseAuth", "Sesión cerrada correctamente por el usuario: ${FirebaseAuth.getInstance().currentUser?.email ?: "usuario"}")
                                isLoggedIn = false
                                tipoUsuario = null
                            },
                            modifier = Modifier
                                .width(70.dp)
                                .height(28.dp)
                                .offset(x = (-300).dp, y = 70.dp),
                            shape = RoundedCornerShape(3.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = null,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Salir",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    loginyclave.LoginScreen(onLoginSuccess = { tipo ->
                        isLoggedIn = true
                        tipoUsuario = tipo
                    })
                }
            }
        }
    }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Eva2Theme {
        Greeting(name = "Android")
    }
}

// este es el @composable de la parte 1/5 del LED APAGADO / ENCENDIDO
@Composable
fun ControlLedScreen(ledRef: DatabaseReference) {
    val context: Context = LocalContext.current
    var ledEncendido: Boolean by remember { mutableStateOf(false) } // este es para
    // comprobar si es un verdadero o falso de un valor =1 encendido o  = 0 apagado
    var estadoTexto: String by remember { mutableStateOf("Desconocido") }
    // el estado comprueba si el valor de encendido es = 1 o apagado es = 0
    // por usar un string creado desde el mismo firebase con un valor/clave

    // Leer estado del LED en tiempo real desde Firebase
    LaunchedEffect(key1 = true) {
        leerEstadoLed(ledRef) { estado ->
            if (estado != null) {
                ledEncendido = (estado == "ON")
                estadoTexto = if (ledEncendido) "LED ENCENDIDO" else "LED APAGADO"
                Log.d("FirebaseLED", "Estado actual desde Firebase: $estado")
            } else {
                Log.e("FirebaseLED", "Error al leer estado desde Firebase")
            }
        }
    }

    // Interfaz de control LED
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = estadoTexto,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (ledEncendido) Color.Green else Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val nuevoEstado: String = if (ledEncendido) "OFF" else "ON"

                // Escribir nuevo estado en Firebase
                escribirEstadoLed(ledRef, nuevoEstado)

                ledEncendido = !ledEncendido
                estadoTexto = if (ledEncendido) "LED ENCENDIDO" else "LED APAGADO"

                Toast.makeText(
                    context,
                    if (ledEncendido) "LED ENCENDIDO" else "LED APAGADO",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FirebaseLED", "Nuevo estado enviado: $nuevoEstado")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ledEncendido) Color.Red else Color.Green
            ),
            modifier = Modifier
                .fillMaxWidth(fraction = 0.6f)
                .height(55.dp)
        ) {
            Text(
                text = if (ledEncendido) "Apagar LED" else "Encender LED",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}}

// apuntes
// se crea la conexion entre la app con firebase
// trabaja en tiempo real y logcat al aplicar un cambio da un registro
// al oprimir ON / OFF envia un estado de valor: 1 = ENCENDIDO 0 = APAGADO
// aparece entre verde y rojo de apagar LED o encender LED
// objetivo que permite controlar el LED desde la app y la base responde correctamente
// tiene un login y clave que al ingresar los campos se validan y tiene opcion de salir del login