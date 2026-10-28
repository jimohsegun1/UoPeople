Project: Weather Information App
Author: Jeremiah Segun Jimoh
Course: CS 1103 (Programming 2) - Unit 8 Programming Assignment

1. Overview:
The Weather Information App is a graphical desktop application built in Java using the Swing GUI toolkit (javax.swing) and Java 2D Graphics (java.awt). It integrates live weather and geocoding REST APIs over HTTP (HttpURLConnection) to provide real-time meteorological conditions and a 5-day short-term forecast for any location worldwide.

2. Implemented Features:
- Real-Time API Integration: Connects to the Open-Meteo Forecast and Geocoding APIs using HttpURLConnection to retrieve live temperature, relative humidity, wind speed, and WMO weather condition codes.
- Flexible Location Input: Users can search by either a city name (e.g., "Lagos", "London", "Tokyo") or direct geographic coordinates in "Latitude, Longitude" format (e.g., "6.45, 3.39").
- Graphical Icon Representation: A custom WeatherIconPanel utilizes anti-aliased Java 2D vector graphics to dynamically render visual representations of Clear Sky (Sun), Partly Cloudy, Rain Showers, Snowfall, Overcast Clouds, and Thunderstorms.
- Short-Term Forecast Display: Displays a 5-day weather forecast grid showing daily dates, expected weather conditions, and daily high and low temperatures.
- Dynamic Unit Conversion: Users can switch temperature units between Celsius (°C) and Fahrenheit (°F) and wind speed units between kilometers per hour (km/h) and miles per hour (mph). All current and forecast metrics recalculate immediately.
- Input Validation and Error Handling: Gracefully handles blank queries, out-of-range geographic coordinates, unknown city names, and network timeouts using JOptionPane error dialogs. Background network requests run inside a SwingWorker thread so the interface never freezes.
- Search History Tracking: Records every successful weather search along with an exact local timestamp (yyyy-MM-dd HH:mm:ss) in a scrollable sidebar list.
- Dynamic Time-of-Day Backgrounds: Automatically inspects the system clock (LocalTime) to apply a custom gradient background matching Morning, Afternoon, Evening Sunset, or Night. Includes a theme selector dropdown so users can also preview all four time-of-day themes manually.

3. How to Compile and Run:
1. Open Windows PowerShell or a command prompt and navigate to the folder containing WeatherApp.java.
2. Compile the source code:
   javac -encoding UTF-8 WeatherApp.java
3. Launch the GUI application:
   java WeatherApp