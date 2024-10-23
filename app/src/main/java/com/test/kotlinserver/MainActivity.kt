package com.test.kotlinserver

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.kotlinserver.ui.theme.KotlinServerTheme
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.security.KeyStore
import java.time.Duration
import java.util.*



val client = HttpClient()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinServerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
        GlobalScope.launch {
            startWebAPI("127.0.0.1:8080", false, "", "", "", Duration.ofSeconds(10), Duration.ofSeconds(10), UUID.fromString("00000000-0000-0000-0000-000000000000"))
        }
    }
    @Composable
    fun Greeting(name: String) {

        val client = HttpClient()

        val coroutineScope = rememberCoroutineScope()
        var text by remember { mutableStateOf("No data yet") }

        val onClick: () -> Unit = {
            coroutineScope.launch {
                try {
                    val response: HttpStatement = client.get("http://127.0.0.1:8080/test")
                    val textResponse: String = response.receive<String>()

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        text = textResponse
                    }
                } catch(e: Exception) {
                    // Handle the error
                    withContext(Dispatchers.Main) {
                        text = "Error: ${e.message}"
                    }
                } finally {
                    client.close()
                }
            }
        }
        Column {
            Text(text = text) // Display the fetched text here
            Button(
                onClick = onClick,
                modifier = Modifier.width(200.dp).height(50.dp)
            ) {
                Text(text = "Click Me")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        KotlinServerTheme {
            Greeting("Android")
        }
    }

    suspend fun startWebAPI(
        webListen: String,
        useSSL: Boolean,
        certificateFile: String,
        certificateKey: String,
        info: String,
        readTimeout: Duration,
        writeTimeout: Duration,
        APIKey: UUID
    ) {



        println("Starting API at '$webListen'")
        fun Application.registerRoutes() {

        }

        val environment = applicationEngineEnvironment {
            module {
                install(CallLogging)
                install(Routing) {
                    route("/test") {
                        get { apiTest() }
                    }
                    route("/status") {
                        get { apiStatus() }
                        route("/peers") {
                            get { apiStatusPeers() }
                        }
                        route("/config") {
                            get { apiStatusConfig() }
                        }
                    }
                    route("/account") {
                        route("/info") {
                            get { apiAccountInfo() }
                        }
                        route("/delete") {
                            get { apiAccountDelete() }
                        }
                    }
                }
            }
            if (useSSL) {
                val keystore: KeyStore = KeyStore.getInstance("PKCS12") // or "PKCS12" for .p12 files
                val fileInputStream: FileInputStream = FileInputStream(certificateFile)
                keystore.load(fileInputStream, certificateKey.toCharArray())
                sslConnector(
                    keyStore = keystore,
                    keyAlias = "alias",
                    keyStorePassword = { certificateKey.toCharArray() },
                    privateKeyPassword = { certificateFile.toCharArray() }
                ) {
                    this.host = "127.0.0.1"
                    this.port = 8080
                }
            } else {
                connector {
                    this.host = "127.0.0.1"
                    this.port = 8080
                }
            }
        }

        val server = embeddedServer(Netty, environment)
        server.start(wait = true)
    }
    suspend fun PipelineContext<Unit, ApplicationCall>.apiTest() {
        // Implement function here, for testing you can just return a simple string
        call.respondText("API Test Successful")
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.apiStatus() {
        // Implement your function here
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.apiStatusPeers() {
        // Implement your function here
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.apiStatusConfig() {
        // Implement your function here
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.apiAccountInfo() {
        // Implement your function here
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.apiAccountDelete() {
        // Implement your function here
    }
}
