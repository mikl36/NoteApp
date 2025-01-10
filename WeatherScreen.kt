package com.example.harkkat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.harkkat.data.model.WeatherResponse
import com.example.harkkat.database.Note
import com.example.harkkat.viewmodel.NoteViewModel
import com.example.harkkat.viewmodel.WeatherViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// data class representing the state of the weather screen
data class WeatherState(
    val weather: WeatherResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isError: Boolean = false
)

// composable function for the weather screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel,
    noteViewModel: NoteViewModel,
    navController: NavController
) {
    // collect the current weather state and last searched city from the ViewModel
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val lastCity by weatherViewModel.lastCity.collectAsState(initial = null) // last searched weather city
    var city by remember { mutableStateOf(lastCity?.city ?: "") } // weather city, empty string if null
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val language by rememberSaveable { // for changing the localisation, now fi and en
        mutableStateOf(context.getString(R.string.weather_screen_language_setting))
    }
    // debounce the search input and avoid multiple rapid API calls
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Fetch weather data when the last city changes
    LaunchedEffect(lastCity) {
        lastCity?.city?.let {
            city = it
            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                delay(500) // Delay of 500 milliseconds for debouncing / limit api calls
                try {
                    weatherViewModel.fetchWeather(city, getApiKey(), language)
                } catch (e: Exception) { // general error message
                    Log.e("WeatherScreen", context.getString
                        (R.string.weather_data_error_fetching_weather_data), e)
                }
            }
        }
    }

    //  Save the last searched city when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            if (city.isNotEmpty()) {
                weatherViewModel.fetchWeather(city, getApiKey(), language)
            }
        }
    }

    // Scaffold layout with a top app bar and content, city name and weather data
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_screen_topbar_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                            stringResource(R.string.weather_screen_topbar_icon_back))
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CityInputField(city) { newCity ->
                        city = newCity
                        searchJob?.cancel()
                        searchJob = coroutineScope.launch {
                            delay(500)
                            weatherViewModel.fetchWeather(city, getApiKey(), language)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    when {
                        weatherState.isLoading -> {
                            LoadingIndicator() // we are fetching the data
                        }
                        weatherState.isError -> {
                            ErrorMessage(stringResource(R.string.city_not_found))
                        }
                        weatherState.weather != null -> { // a fetch was successful, new city data
                            WeatherContent(
                                city = city,
                                weather = weatherState.weather!!,
                                noteViewModel = noteViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    )
}

// Composable function for the city input field
@Composable
fun CityInputField(city: String, onCityChange: (String) -> Unit) {
    OutlinedTextField(
        value = city,
        onValueChange = onCityChange,
        label = { Text(stringResource(R.string.weather_screen_enter_city)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

// weather content displayed when data is fetched, now city, description, temperature, wind speed
@Composable
fun WeatherContent(
    city: String,
    weather: WeatherResponse,
    noteViewModel: NoteViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val mainWeather = weather.main
    val weatherDescription = weather.weather[0].description
    val windSpeed = weather.wind.speed
    val kelvinToCelsius = 273.15
    val tempCelsius = mainWeather.temp - kelvinToCelsius

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = String.format(Locale.US, "%.2f°C", tempCelsius), // should make a resource
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.weather_screen_weather_state_msg, weatherDescription),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.weather_screen_wind_m_s_msg, windSpeed),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            noteViewModel.insert(
                Note(
                    title = context.getString
                        (R.string.weather_screen_weather_in_city_title, city),
                    content = context.getString
                        (R.string.weather_screen_temperature_content,
                        String.format(Locale.US, "%.2f°C", tempCelsius)) + // should make a resource
                            context.getString(R.string.weather_screen_status_content,
                                weatherDescription) +
                            String.format(Locale.US, context.getString
                                (R.string.weather_screen_wind_2f_m_s_content), windSpeed)
                )
            )
            navController.popBackStack()
        }) {
            Text(stringResource(R.string.weather_screen_add_as_note))
        }
    }
}

//  Composable function for displaying a loading indicator
@Composable
fun LoadingIndicator() {
    CircularProgressIndicator()
}

// Composable function for displaying an error message
@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error
    )
}

// Function to get the API key for fetching weather data
fun getApiKey(): String {
    return BuildConfig.OPENWEATHER_API_KEY
}