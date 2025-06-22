package com.VaSeguro.ui.components.Container.TopBarContainer

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.R
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.Aux.generateQRCode
import com.VaSeguro.ui.navigations.ConfigurationScreenNavigation

enum class InfoDialogType {
    NONE, TERMS, NOTIFICATIONS, SUPPORT, ABOUT,QR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "VaSeguro",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    navController: NavController,
    navControllerx: NavController
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val viewModel: TopBarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return TopBarViewModel(appProvider.provideUserPreferences(), appProvider.provideAuthRepository(), appProvider.provideRequestRepository()) as T
            }
        }
    )
    val confirmationPhrase = "BORRAR"
    var infoDialog by remember { mutableStateOf(InfoDialogType.NONE) }


    TopAppBar(
        title = {},
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(onClick = { viewModel.openConfigDialog() }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Configuración"
                    )
                }
            }
        },
        actions = {
            Row {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones"
                    )
                }
                IconButton(onClick = { navControllerx.navigate(ConfigurationScreenNavigation) }) {
                    val profilePic = viewModel.userProfilePic
                    if (!profilePic.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profilePic),
                            contentDescription = "Perfil",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )

    if (viewModel.isConfigDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                viewModel.closeConfigDialog()
                infoDialog = InfoDialogType.NONE
            },
            confirmButton = {},
            title = {},
            text = {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(570.dp)
                ) {
                    IconButton(
                        onClick = {
                            viewModel.closeConfigDialog()
                            infoDialog = InfoDialogType.NONE
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-40).dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamic title
                        if (infoDialog == InfoDialogType.NONE) {
                            Text(
                                "Configuración",
                                fontSize = 30.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    top = 16.dp,
                                    bottom = 16.dp
                                )
                            )

                        }else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 16.dp)
                            ) {
                                IconButton(
                                    onClick = { infoDialog = InfoDialogType.NONE },
                                    modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                                }
                                Text(
                                    text = when (infoDialog) {
                                        InfoDialogType.TERMS -> "Términos y condiciones"
                                        InfoDialogType.NOTIFICATIONS -> "Notificaciones"
                                        InfoDialogType.SUPPORT -> "Soporte"
                                        InfoDialogType.ABOUT -> "Acerca de"
                                        else -> ""
                                    },
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 38.sp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .padding(start = 32.dp)
                                )
                            }
                        }

                        if (infoDialog == InfoDialogType.NONE) {
                            ConfigOption("Cuenta", Icons.Outlined.AccountCircle) {
                                viewModel.closeConfigDialog()
                                navControllerx.navigate(ConfigurationScreenNavigation)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (viewModel.userRoleId == 4) {
                                Log.d("TopBar", "User is admin, showing QR option"+
                                        " with role ID: ${viewModel.userRoleId}")
                                ConfigOption("Mostrar QR", Icons.Filled.QrCode) {
                                    infoDialog=InfoDialogType.QR
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            ConfigOption("Terminos y condiciones", Icons.Filled.Book) {
                                infoDialog = InfoDialogType.TERMS
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfigOption("Notificaciones", Icons.Filled.Notifications) {
                                infoDialog = InfoDialogType.NOTIFICATIONS
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfigOption("Soporte", Icons.Filled.Help) {
                                infoDialog = InfoDialogType.SUPPORT
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfigOption("Acerca de", Icons.Filled.Info) {
                                infoDialog = InfoDialogType.ABOUT
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfigOption("Borrar cuenta", Icons.Filled.Delete) {
                                showDeleteDialog = true
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfigOption("Cerrar Sesion", Icons.Filled.ExitToApp) {
                                viewModel.closeConfigDialog()
                                viewModel.logout(context) {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Confirm Delete Account") },
                                    text = {
                                        Column {
                                            Text("Type \"$confirmationPhrase\" to confirm account deletion.")
                                            OutlinedTextField(
                                                value = password,
                                                onValueChange = { password = it },
                                                label = { Text("Type confirmation") },
                                                singleLine = true
                                            )
                                            errorMessage?.let { Text(it, color = Color.Red) }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                isLoading = true
                                                errorMessage = null
                                                viewModel.deleteAccount(context) { success ->
                                                    isLoading = false
                                                    if (success) {
                                                        viewModel.logout(context) {
                                                            navController.navigate("login") {
                                                                popUpTo(0) { inclusive = true }
                                                            }
                                                        }
                                                    } else {
                                                        errorMessage = "Error deleting account."
                                                    }
                                                }
                                            },
                                            enabled = !isLoading && password == confirmationPhrase
                                        ) { Text("Delete") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            when (infoDialog) {
                                InfoDialogType.TERMS -> Text(
                                    buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("Bienvenido a VaSeguro\n\n")
                                        }

                                        append("Al utilizar esta aplicación, aceptas los siguientes términos y condiciones. Te pedimos que leas detenidamente este documento antes de continuar.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("1. Uso de la Aplicación\n")
                                        }
                                        append("VaSeguro está diseñada para mejorar tu seguridad personal y proporcionar herramientas útiles para la gestión de tu información y servicios relacionados. El uso ")
                                        withStyle(style = SpanStyle(color = Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) {
                                            append("indebido")
                                        }
                                        append(", incluyendo actividades fraudulentas o maliciosas, está estrictamente prohibido.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("2. Privacidad de Datos\n")
                                        }
                                        append("Nos comprometemos a proteger tu ")
                                        withStyle(style = SpanStyle(color = Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) {
                                            append("información personal")
                                        }
                                        append(". Todos los datos proporcionados serán tratados conforme a nuestra política de privacidad.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("3. Responsabilidad del Usuario\n")
                                        }
                                        append("Eres responsable de mantener la confidencialidad de tus ")
                                        withStyle(style = SpanStyle(color = Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) {
                                            append("credenciales")
                                        }
                                        append(". VaSeguro no se hace responsable por accesos no autorizados.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("4. Modificaciones del Servicio\n")
                                        }
                                        append("Nos reservamos el derecho de actualizar o modificar funcionalidades sin previo aviso, lo cual puede afectar temporal o permanentemente el servicio.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("5. Propiedad Intelectual\n")
                                        }
                                        append("Todos los contenidos y elementos visuales de la app son propiedad de VaSeguro y están protegidos por las leyes de ")
                                        withStyle(style = SpanStyle(color = Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) {
                                            append("derechos de autor")
                                        }
                                        append(".\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("6. Cancelación de la Cuenta\n")
                                        }
                                        append("Puedes eliminar tu cuenta en cualquier momento. También podemos suspender cuentas que infrinjan estos términos.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("7. Contacto\n")
                                        }
                                        append("Para dudas o soporte, contáctanos al correo ")
                                        withStyle(style = SpanStyle(color = Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) {
                                            append("soporte@vaseguro.com")
                                        }
                                        append(".\n\n")

                                        append("Al continuar, confirmas que has leído, comprendido y aceptado estos términos.\n\n")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("Fecha de entrada en vigencia: 20 de junio de 2025")
                                        }
                                    },
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                                InfoDialogType.QR -> {
                                    LaunchedEffect(Unit) {
                                        viewModel.fetchDriverCode()
                                    }
                                    val code = viewModel.driverCode ?: "Cargando..."
                                    val qrBitmap = remember(code) {
                                        if (code != "Cargando...") generateQRCode(code) else null
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            "Escanea el código QR",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                        if (qrBitmap != null) {
                                            Image(
                                                bitmap = qrBitmap.asImageBitmap(),
                                                contentDescription = "Código QR",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(300.dp)
                                                    .padding(4.dp)
                                            )
                                        } else {
                                            Text("Generando QR...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                        }
                                        Text(
                                            "Código: $code",
                                            fontSize = 20.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            "Comparte este codigo QR con tus clientes para que puedan escanearlo y acceder a tu perfil.",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                InfoDialogType.NOTIFICATIONS -> {
                                    var emergencyAlerts by remember { mutableStateOf(true) }
                                    var appUpdates by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            "Notification Settings",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Emergency Alerts", fontWeight = FontWeight.Medium)
                                                Text("Receive critical safety notifications", fontSize = 13.sp, color = Color.Gray)
                                            }
                                            Switch(
                                                checked = emergencyAlerts,
                                                onCheckedChange = { emergencyAlerts = it }
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("App Updates", fontWeight = FontWeight.Medium)
                                                Text("Get notified about new features", fontSize = 13.sp, color = Color.Gray)
                                            }
                                            Switch(
                                                checked = appUpdates,
                                                onCheckedChange = { appUpdates = it }
                                            )
                                        }
                                    }
                                }
                                InfoDialogType.SUPPORT -> Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        "Preguntas Frecuentes",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    )

                                    val faqs = listOf(
                                        "¿Qué hacer si la app no muestra la ubicación del bus?" to
                                                "Asegúrate de tener conexión a internet y que el GPS del dispositivo del conductor esté activado.",
                                        "¿Cómo cambio mi contraseña?" to
                                                "Ve a la sección de Cuenta dentro del menú Configuración y selecciona 'Cambiar contraseña'.",
                                        "¿Puedo registrar a más de un hijo?" to
                                                "Sí. Puedes agregar múltiples perfiles desde la sección de Cuenta > Hijos.",
                                        "¿Qué hago si olvidé mi contraseña?" to
                                                "En la pantalla de inicio de sesión, selecciona '¿Olvidaste tu contraseña?' y sigue las instrucciones enviadas a tu correo."
                                    )

                                    faqs.forEach { (question, answer) ->
                                        var expanded by remember { mutableStateOf(false) }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .border(1.dp, Color.DarkGray, shape = MaterialTheme.shapes.medium)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(Color.White)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { expanded = !expanded }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    question,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (expanded) "Cerrar" else "Abrir",
                                                    tint = Color(0xFFBDBDBD)
                                                )
                                            }
                                            AnimatedVisibility(visible = expanded) {
                                                Text(
                                                    answer,
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        "¿Necesitas más ayuda? Escríbenos a:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        "soporte@vaseguro.com",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1976D2),
                                        textDecoration = TextDecoration.Underline,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    )
                                }
                                InfoDialogType.ABOUT -> Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Image(
                                        painter = painterResource(id = R.drawable.logoteam),
                                        contentDescription = "Logo del equipo",
                                        modifier = Modifier
                                            .size(128.dp)
                                            .padding(bottom = 8.dp)
                                    )

                                    Text(
                                        "VaSeguro v1.0",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Aplicación móvil diseñada para brindar seguridad, confianza y asistencia personalizada a los padres y responsables en el seguimiento de sus hijos durante el transporte escolar.",
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        "Desarrollado por PipilTeam",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        "Versión: 1.0.0 (Junio 2025)",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            },
            containerColor = Color.White,
        )
    }
}