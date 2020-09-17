package com.castrec.stephane.dialogflownavigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.castrec.stephane.dialogflownavigation.dialogflow.DialogflowCredentials
import com.castrec.stephane.dialogflownavigation.dialogflow.TaskRunner
import com.castrec.stephane.dialogflownavigation.dialogflow.TaskRunner.ChatbotCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import java.io.IOException
import java.io.InputStream
import java.util.*


class MainActivity: AppCompatActivity(), ChatbotCallback {

    val SPEECH_INPUT = 10070

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var dialogflowTaskRunner: TaskRunner? = null

    var drawerLayout: DrawerLayout? = null
    lateinit var navController: NavController

    private var sessionsClient: SessionsClient? = null
    private var session: SessionName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // provide your Dialogflow's Google Credential JSON saved under RAW folder in resources
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            askToSpeak()
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        DialogflowCredentials.getInstance().setInputStream(getResources().openRawResource(R.raw.df_key));
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        init(UUID.randomUUID().toString())
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0]
                Log.d("MainActivity", "onActivityResult: $result")
                dialogflowTaskRunner =
                    TaskRunner(
                        this,
                        session,
                        sessionsClient,
                        result
                    )
                dialogflowTaskRunner!!.executeChat()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun OnChatbotResponse(response: DetectIntentResponse?) {
        Log.d("MainActivity",
            "Dialogflow Detected Intent " + response!!.queryResult.intent.displayName)
        processDFResponse(response)
    }

    fun processDFResponse(response: DetectIntentResponse) {
            // TODO Your Intents here
            when (response.queryResult.intent.displayName) {
                "MenuIntent" -> drawerLayout!!.openDrawer(Gravity.LEFT)
                "NavSlideshowIntent" -> navController.navigate(R.id.nav_slideshow)
                "NavGalleryIntent" -> navController.navigate(R.id.nav_gallery)
                else -> {
                    // If another
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("My bot")
                        .setCancelable(true)
                        .setMessage(response.queryResult.fulfillmentText)
                    if (!response.queryResult.intent.isFallback) {
                        builder.setMessage(response.queryResult.fulfillmentText + "\n\nLa réponse vous convient elle ?")
                        /*Snackbar.make(drawerLayout as View, response.queryResult.fulfillmentText, Snackbar.LENGTH_LONG)
                        .setAction("Terminer", null).show()*/
                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            Toast.makeText(
                                applicationContext,
                                "\ud83d\ude0d Merci", Toast.LENGTH_SHORT
                            ).show()
                        }
                            .setNegativeButton(android.R.string.no) { dialog, which ->
                                Toast.makeText(
                                    applicationContext,
                                    "\ud83d\ude1e Je ferais mieux la prochaine fois",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    builder.show()
                }
            }
    }

    fun askToSpeak() {
        val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Je vous écoute");
        try {
            startActivityForResult(intent, SPEECH_INPUT);
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(getApplicationContext(),
                "Ce périphérique ne semble pas accepeter que vus lui parliez.", Toast.LENGTH_SHORT).show();
        }
    }

    @Throws(IOException::class)
    private fun init(UUID: String) {
        val credentialStream: InputStream = DialogflowCredentials.getInstance().getInputStream()
        val credentials = GoogleCredentials.fromStream(credentialStream)
        val projectId = (credentials as ServiceAccountCredentials).projectId
        val settingsBuilder: SessionsSettings.Builder = SessionsSettings.newBuilder()
        val sessionsSettings: SessionsSettings =
            settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()
        sessionsClient = SessionsClient.create(sessionsSettings)
        session = SessionName.of(projectId, UUID)
    }

}