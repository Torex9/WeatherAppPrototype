package com.rufus.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rufus.weatherapp.constant.Const.Companion.colorBg1
import com.rufus.weatherapp.constant.Const.Companion.colorBg2
import com.rufus.weatherapp.constant.Const.Companion.permissions
import com.rufus.weatherapp.model.MyLatLng
import com.rufus.weatherapp.model.forecast.ForecastResult
import com.rufus.weatherapp.model.weather.WeatherResult
import com.rufus.weatherapp.ui.theme.Purple80
import com.rufus.weatherapp.ui.theme.WeatherAppTheme
import com.rufus.weatherapp.ui.theme.purple_700
import com.rufus.weatherapp.view.ForecastSection
import com.rufus.weatherapp.view.WeatherSection
import com.rufus.weatherapp.viewmodel.MainViewModel
import com.rufus.weatherapp.viewmodel.STATE
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mainViewModel: MainViewModel
    private var locationRequired: Boolean = false

    override fun onResume() {
        super.onResume()
        if (locationRequired) startLocationUpdate();
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        locationCallback?.let {

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(100)
                .build()

            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }
    //test 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        initlocationClient()
        initViewModel()


        setContent {

            //This will Keep the value of the current location
            var currentLocation by remember {
                mutableStateOf(MyLatLng(0.0,0.0))
            }

            //implement location callback
            locationCallback = object: LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for(location in p0.locations) {
                        currentLocation = MyLatLng(
                            location.latitude,
                            location.longitude
                        )
                    }
                }

            }
            navigation(currentLocation)

            /*WeatherAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationScreen(this@MainActivity, currentLocation)
                }
            } */
        }
    }



    private fun fetchWeatherInformation(mainViewModel: MainViewModel, currentLocation: MyLatLng) {
        mainViewModel.state = STATE.LOADING
        mainViewModel.getWeatherByLocation(currentLocation)
        mainViewModel.getForecastByLocation(currentLocation)
        mainViewModel.state = STATE.SUCCESS
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
    }

    @Composable
    private fun LocationScreen(context: Context, currentLocation: MyLatLng) {

        //Request runtime permission
        val launcherMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionMap ->
            val areGranted = permissionMap.values.reduce{
                accepted, next -> accepted && next
            }
            //Check all permission is accepted
            if(areGranted) {
                locationRequired = true;
                startLocationUpdate();
                Toast.makeText(context, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Location Permission Denied, Enable permission in Settings", Toast.LENGTH_SHORT).show()
            }
        }

        val systemUiController = rememberSystemUiController()

        DisposableEffect(key1 = true, effect = {
            systemUiController.isSystemBarsVisible = false //Hide status bar
            onDispose {
                systemUiController.isSystemBarsVisible = true //Show status bar
            }
        } )

        LaunchedEffect(key1 = currentLocation, block = {
            coroutineScope {
                if (permissions.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                    }) {
                    //if all permission accepted
                    startLocationUpdate()
                }
                else {
                    launcherMultiplePermissions.launch(permissions)
                }
            }
        })

        LaunchedEffect(key1 = true, block = {
            fetchWeatherInformation(mainViewModel, currentLocation)
        })

        val gradient = Brush.linearGradient(
            colors = listOf(Color(colorBg1) , Color(colorBg2)),
            start = Offset(1000f, -1000f),
            end = Offset(1000f, 1000f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.BottomCenter
        ) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val marginTop = screenHeight * 0.1f // I want the margin top at 20% height
            val marginTopPx = with(LocalDensity.current) {marginTop.toPx()}

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        //Define the layout for the child
                        layout(
                            placeable.width,
                            placeable.height + marginTopPx.toInt()
                        ) {
                            placeable.placeRelative(0, marginTopPx.toInt())
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Text(text = "Hello")
                //Text(text = "${currentLocation.lat}/${currentLocation.lng}")
                when (mainViewModel.state) {
                    STATE.LOADING -> {
                        LoadingSection()
                    }
                    STATE.FAILED -> {
                        ErrorSection(mainViewModel.errorMessage)
                    }
                    else -> {
                        //*Creating two sections Corresponding to Weather and forecast*
                        WeatherSection(mainViewModel.weatherResponse)
                        ForecastSection(mainViewModel.forecastResponse)
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    //Fetch Api when location changes
                    fetchWeatherInformation(mainViewModel, currentLocation)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Add")
            }
        }
    }





    @Composable
    fun ErrorSection(errorMessage: String) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = errorMessage, color = Color.White)
        }
    }
    @Composable
    fun LoadingSection() {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           CircularProgressIndicator(color = Color.White)
        }
    }

    private fun initlocationClient() {
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
    }


    //navigation from splash screen to main page
    @Composable
    fun navigation(currentLocation: MyLatLng) {
        var navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "splash_screen"
        ){
            composable("splash_screen") {
                SplashScreen(navController = navController)
            }

            composable("main_screen") {
                LocationScreen(this@MainActivity, currentLocation)
            }
        }
    }


    //splash screen
    @Composable
    fun SplashScreen(navController: NavController) {
        val gradient = Brush.linearGradient(
            colors = listOf(Color(colorBg1) , Color(colorBg2)),
            start = Offset(1000f, -1000f),
            end = Offset(1000f, 1000f)
        )
        val scale = remember{ androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(key1 = true) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = {
                        OvershootInterpolator(4f).getInterpolation(it)
                    }
                )
            )
            delay(3000L)
            navController.navigate("main_screen") {
                popUpTo("splash_screen") {
                    inclusive = true
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {


            Text(
                text = "WEATHER APP",
                modifier = Modifier
                    .scale(scale.value),
                color = Color.White,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "By Rufus C2676906",
                modifier = Modifier
                    .scale(scale.value),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

