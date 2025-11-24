package barranavegacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background

@Composable
fun BarraNavegadoraSimple() {
    var pantallaSeleccionada by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Área de contenido que cambia según la pantalla seleccionada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when (pantallaSeleccionada) {
                0 -> controlporton.ControlPortonScreen()
                1 -> controlmodo.ControlModoScreen()
                2 -> controltiempos.ControlTiemposScreen()
                3 -> horarioscontrol.ControlHorariosScreen()
                4 -> deteccioncontrol.ControlDeteccionScreen()
                5 -> alertascontrol.ControlAlertasScreen()
                6 -> lucescontrol.ControlLucesScreen()
                7 -> monitor.MonitoreoScreen()
            }
        }

        // Barra inferior simple
        Surface(
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val titulos = listOf("Portón", "Modo", "Tiempos", "Horarios", "Detección", "Alertas", "Luces", "Monitoreo")
                
                titulos.forEachIndexed { index, titulo ->
                    val isSelected = pantallaSeleccionada == index
                    
                    TextButton(
                        onClick = { pantallaSeleccionada = index },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isSelected) Color.Black else Color.Gray
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = titulo,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
