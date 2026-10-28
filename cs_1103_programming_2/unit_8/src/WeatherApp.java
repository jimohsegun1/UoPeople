import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Weather Information App
 * Course: CS 1103 (Programming 2) - Unit 8 Programming Assignment
 * Author: Jeremiah Segun Jimoh
 *
 * A Java Swing GUI application that fetches real-time weather and 5-day forecast data
 * using both City Names and Geographic Coordinates (with forward and reverse geocoding),
 * draws custom graphical weather icons, converts measurement units dynamically, tracks
 * search history with timestamps, and adapts its background theme to the time of day.
 */
public class WeatherApp extends JFrame {

    // API Configuration constants
    private static final String API_KEY = "DEMO_LIVE_WEATHER_KEY_CS1103";
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String REVERSE_GEO_API_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client";
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";

    // UI Components
    private BackgroundPanel mainContainer;
    private JTextField cityInputField;
    private JTextField coordInputField;
    private JButton searchCityButton;
    private JButton searchCoordButton;
    private JComboBox<String> tempUnitSelector;
    private JComboBox<String> windUnitSelector;
    private JComboBox<String> timeThemeSelector;

    private JLabel cityTitleLabel;
    private JLabel coordinatesSubtitleLabel;
    private JLabel conditionLabel;
    private JLabel temperatureLabel;
    private JLabel humidityLabel;
    private JLabel windSpeedLabel;
    private JLabel timeOfDayBadgeLabel;
    private WeatherIconPanel weatherIconPanel;

    private JPanel forecastGridPanel;
    private DefaultListModel<String> historyListModel;
    private JList<String> historyList;

    // Application state variables
    private boolean hasLoadedData = false;
    private String currentCityDisplay = "No Location Selected";
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private double currentTempCelsius = 0.0;
    private int currentHumidity = 0;
    private double currentWindKmh = 0.0;
    private int currentWeatherCode = 0;
    private List<ForecastDay> forecastDays = new ArrayList<>();

    /**
     * Data structure representing a single day's forecast.
     */
    private static class ForecastDay {
        String date;
        int weatherCode;
        double maxTempCelsius;
        double minTempCelsius;

        ForecastDay(String date, int weatherCode, double maxTempCelsius, double minTempCelsius) {
            this.date = date;
            this.weatherCode = weatherCode;
            this.maxTempCelsius = maxTempCelsius;
            this.minTempCelsius = minTempCelsius;
        }
    }

    public WeatherApp() {
        setTitle("Real-Time Weather Information App - CS 1103");
        setSize(1020, 700);
        setMinimumSize(new Dimension(940, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainContainer = new BackgroundPanel();
        mainContainer.setLayout(new BorderLayout(15, 15));
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainContainer);

        // Initialize GUI sections
        mainContainer.add(createTopControlBar(), BorderLayout.NORTH);
        mainContainer.add(createCenterWeatherDashboard(), BorderLayout.CENTER);
        mainContainer.add(createRightHistorySidebar(), BorderLayout.EAST);
        mainContainer.add(createBottomForecastSection(), BorderLayout.SOUTH);

        // Apply automatic time-of-day background theme
        updateBackgroundTheme();

        // Automatically fetch weather for Lagos on startup
        SwingUtilities.invokeLater(() -> {
            cityInputField.setText("Lagos");
            performWeatherSearch("CITY");
        });
    }

    /**
     * Builds a two-row top control bar with dedicated inputs for both City Name
     * and Coordinates (Lat, Lon), plus unit conversion and theme dropdowns.
     */
    private JPanel createTopControlBar() {
        JPanel topWrapper = new TranslucentCardPanel();
        topWrapper.setLayout(new GridLayout(2, 1, 5, 5));
        topWrapper.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Row 1: City Name Input & Coordinates Input
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        searchRow.setOpaque(false);

        JLabel cityPrompt = new JLabel("City Name:");
        cityPrompt.setFont(new Font("SansSerif", Font.BOLD, 13));
        cityPrompt.setForeground(Color.WHITE);

        cityInputField = new JTextField(13);
        cityInputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cityInputField.setToolTipText("Enter a city name (e.g., Lagos, London, New York)");
        cityInputField.addActionListener(e -> performWeatherSearch("CITY"));

        searchCityButton = new JButton("Search by City");
        searchCityButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchCityButton.setBackground(new Color(30, 136, 229));
        searchCityButton.setForeground(Color.WHITE);
        searchCityButton.setFocusPainted(false);
        searchCityButton.addActionListener(e -> performWeatherSearch("CITY"));

        JLabel coordPrompt = new JLabel("Coordinates (Lat, Lon):");
        coordPrompt.setFont(new Font("SansSerif", Font.BOLD, 13));
        coordPrompt.setForeground(Color.WHITE);

        coordInputField = new JTextField("6.45, 3.39", 12);
        coordInputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        coordInputField.setToolTipText("Enter Latitude, Longitude (e.g., 40.71, -74.00)");
        coordInputField.addActionListener(e -> performWeatherSearch("COORD"));

        searchCoordButton = new JButton("Search by Coordinates");
        searchCoordButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchCoordButton.setBackground(new Color(0, 150, 136));
        searchCoordButton.setForeground(Color.WHITE);
        searchCoordButton.setFocusPainted(false);
        searchCoordButton.addActionListener(e -> performWeatherSearch("COORD"));

        searchRow.add(cityPrompt);
        searchRow.add(cityInputField);
        searchRow.add(searchCityButton);
        searchRow.add(Box.createHorizontalStrut(12));
        searchRow.add(coordPrompt);
        searchRow.add(coordInputField);
        searchRow.add(searchCoordButton);

        // Row 2: Unit Conversion & Dynamic Background Theme Controls
        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        optionsRow.setOpaque(false);

        JLabel tempUnitLabel = new JLabel("Temperature Unit:");
        tempUnitLabel.setForeground(Color.WHITE);
        tempUnitLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        tempUnitSelector = new JComboBox<>(new String[]{"Celsius (°C)", "Fahrenheit (°F)"});
        tempUnitSelector.addActionListener(e -> refreshDisplayedUnits());

        JLabel windUnitLabel = new JLabel("Wind Speed Unit:");
        windUnitLabel.setForeground(Color.WHITE);
        windUnitLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        windUnitSelector = new JComboBox<>(new String[]{"km/h", "mph"});
        windUnitSelector.addActionListener(e -> refreshDisplayedUnits());

        JLabel themeLabel = new JLabel("Background Theme:");
        themeLabel.setForeground(Color.WHITE);
        themeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        timeThemeSelector = new JComboBox<>(new String[]{
            "Auto (System Clock)",
            "Morning Sky",
            "Afternoon Sun",
            "Evening Sunset",
            "Night Sky"
        });
        timeThemeSelector.addActionListener(e -> updateBackgroundTheme());

        optionsRow.add(tempUnitLabel);
        optionsRow.add(tempUnitSelector);
        optionsRow.add(Box.createHorizontalStrut(10));
        optionsRow.add(windUnitLabel);
        optionsRow.add(windUnitSelector);
        optionsRow.add(Box.createHorizontalStrut(10));
        optionsRow.add(themeLabel);
        optionsRow.add(timeThemeSelector);

        topWrapper.add(searchRow);
        topWrapper.add(optionsRow);

        return topWrapper;
    }

    /**
     * Builds the central panel displaying current weather metrics, resolved city + coordinates,
     * and the custom graphical weather icon.
     */
    private JPanel createCenterWeatherDashboard() {
        JPanel dashboardCard = new TranslucentCardPanel();
        dashboardCard.setLayout(new BorderLayout(20, 10));
        dashboardCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header with City Name, Coordinates Subtitle, and Time-of-Day status
        JPanel headerPanel = new JPanel(new BorderLayout(5, 4));
        headerPanel.setOpaque(false);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        cityTitleLabel = new JLabel("Searching weather data...");
        cityTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        cityTitleLabel.setForeground(Color.WHITE);

        coordinatesSubtitleLabel = new JLabel("Coordinates: --");
        coordinatesSubtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        coordinatesSubtitleLabel.setForeground(new Color(200, 230, 255));

        titleStack.add(cityTitleLabel);
        titleStack.add(Box.createVerticalStrut(3));
        titleStack.add(coordinatesSubtitleLabel);

        timeOfDayBadgeLabel = new JLabel("Dynamic Background: Auto");
        timeOfDayBadgeLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        timeOfDayBadgeLabel.setForeground(new Color(255, 235, 150));

        headerPanel.add(titleStack, BorderLayout.WEST);
        headerPanel.add(timeOfDayBadgeLabel, BorderLayout.EAST);

        // Center area with custom icon on left and large temperature + details on right
        JPanel mainMetricsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainMetricsPanel.setOpaque(false);

        weatherIconPanel = new WeatherIconPanel();
        mainMetricsPanel.add(weatherIconPanel);

        JPanel detailsBox = new JPanel();
        detailsBox.setLayout(new BoxLayout(detailsBox, BoxLayout.Y_AXIS));
        detailsBox.setOpaque(false);

        temperatureLabel = new JLabel("-- °C");
        temperatureLabel.setFont(new Font("SansSerif", Font.BOLD, 46));
        temperatureLabel.setForeground(Color.WHITE);

        conditionLabel = new JLabel("Condition: --");
        conditionLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        conditionLabel.setForeground(new Color(255, 235, 150));

        humidityLabel = new JLabel("Humidity: -- %");
        humidityLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
        humidityLabel.setForeground(Color.WHITE);

        windSpeedLabel = new JLabel("Wind Speed: -- km/h");
        windSpeedLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
        windSpeedLabel.setForeground(Color.WHITE);

        detailsBox.add(Box.createVerticalGlue());
        detailsBox.add(temperatureLabel);
        detailsBox.add(Box.createVerticalStrut(8));
        detailsBox.add(conditionLabel);
        detailsBox.add(Box.createVerticalStrut(12));
        detailsBox.add(humidityLabel);
        detailsBox.add(Box.createVerticalStrut(6));
        detailsBox.add(windSpeedLabel);
        detailsBox.add(Box.createVerticalGlue());

        mainMetricsPanel.add(detailsBox);

        dashboardCard.add(headerPanel, BorderLayout.NORTH);
        dashboardCard.add(mainMetricsPanel, BorderLayout.CENTER);

        return dashboardCard;
    }

    /**
     * Builds the right-hand panel for displaying recent search history with timestamps,
     * showing both the city name and its coordinates.
     */
    private JPanel createRightHistorySidebar() {
        JPanel historyCard = new TranslucentCardPanel();
        historyCard.setLayout(new BorderLayout(8, 8));
        historyCard.setPreferredSize(new Dimension(330, 0));
        historyCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel historyHeader = new JLabel("Recent Searches (City & Coords)");
        historyHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        historyHeader.setForeground(Color.WHITE);

        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        historyList.setBackground(new Color(20, 30, 50));
        historyList.setForeground(Color.WHITE);
        historyList.setSelectionBackground(new Color(66, 165, 245));

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80)));

        JButton clearHistoryBtn = new JButton("Clear History");
        clearHistoryBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearHistoryBtn.addActionListener(e -> historyListModel.clear());

        historyCard.add(historyHeader, BorderLayout.NORTH);
        historyCard.add(scrollPane, BorderLayout.CENTER);
        historyCard.add(clearHistoryBtn, BorderLayout.SOUTH);

        return historyCard;
    }

    /**
     * Builds the bottom 5-day short-term weather forecast section.
     */
    private JPanel createBottomForecastSection() {
        JPanel forecastContainer = new TranslucentCardPanel();
        forecastContainer.setLayout(new BorderLayout(8, 8));
        forecastContainer.setBorder(new EmptyBorder(10, 18, 12, 18));
        forecastContainer.setPreferredSize(new Dimension(0, 165));

        JLabel forecastTitle = new JLabel("5-Day Short-Term Weather Forecast");
        forecastTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        forecastTitle.setForeground(Color.WHITE);

        forecastGridPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        forecastGridPanel.setOpaque(false);

        forecastContainer.add(forecastTitle, BorderLayout.NORTH);
        forecastContainer.add(forecastGridPanel, BorderLayout.CENTER);

        return forecastContainer;
    }

    /**
     * Handles weather searches triggered by either the City input or the Coordinates input.
     * Automatically syncs both fields so both the city name and coordinates are always shown.
     */
    private void performWeatherSearch(String mode) {
        String rawCity = cityInputField.getText().trim();
        String rawCoord = coordInputField.getText().trim();

        // Allow flexible input: if the user typed coordinates into the City box, treat as COORD
        Pattern coordPattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher cityHasCoords = coordPattern.matcher(rawCity);
        Matcher coordBoxMatcher = coordPattern.matcher(rawCoord);

        boolean searchByCoordinates = mode.equals("COORD") || cityHasCoords.find();

        if (!searchByCoordinates && rawCity.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a city name (e.g., Lagos, London, Tokyo).",
                "Input Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (searchByCoordinates && !coordBoxMatcher.find() && ! coordPattern.matcher(rawCity).find()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter valid coordinates in 'Latitude, Longitude' format (e.g., 40.71, -74.00).",
                "Coordinate Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        searchCityButton.setEnabled(false);
        searchCoordButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;
            private String resolvedCityName = "";
            private double lat = 0.0;
            private double lon = 0.0;

            @Override
            protected Void doInBackground() {
                try {
                    Matcher activeCoordMatcher = coordPattern.matcher(mode.equals("COORD") ? rawCoord : rawCity);
                    if (activeCoordMatcher.find()) {
                        lat = Double.parseDouble(activeCoordMatcher.group(1));
                        lon = Double.parseDouble(activeCoordMatcher.group(2));

                        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                            errorMessage = "Coordinates out of range. Latitude must be between -90 and 90, and Longitude between -180 and 180.";
                            return null;
                        }

                        // Reverse-geocode the coordinates to get the city and country name
                        resolvedCityName = reverseGeocodeCoordinates(lat, lon);
                    } else {
                        // Clean city query (if user typed "Lagos, Nigeria", take primary name for geocoder)
                        String cleanCityQuery = rawCity.contains(",") ? rawCity.split(",")[0].trim() : rawCity;
                        String encodedCity = URLEncoder.encode(cleanCityQuery, StandardCharsets.UTF_8);
                        String geoUrl = GEOCODING_API_URL + "?name=" + encodedCity + "&count=1&language=en&format=json";
                        String geoJson = executeHttpGet(geoUrl);

                        if (!geoJson.contains("\"results\"")) {
                            errorMessage = "City '" + rawCity + "' could not be found. Please check the spelling.";
                            return null;
                        }

                        String cityName = extractJsonString(geoJson, "name");
                        String countryName = extractJsonString(geoJson, "country");
                        lat = extractJsonDouble(geoJson, "latitude");
                        lon = extractJsonDouble(geoJson, "longitude");
                        resolvedCityName = countryName.isEmpty() ? cityName : cityName + ", " + countryName;
                    }

                    // Fetch current weather and 5-day forecast
                    String weatherUrl = String.format(
                        "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto",
                        WEATHER_API_URL, lat, lon
                    );

                    String weatherJson = executeHttpGet(weatherUrl);
                    parseWeatherResponse(weatherJson, resolvedCityName, lat, lon);

                } catch (Exception ex) {
                    errorMessage = "Failed to retrieve weather data: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                searchCityButton.setEnabled(true);
                searchCoordButton.setEnabled(true);

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(
                        WeatherApp.this,
                        errorMessage,
                        "Weather Lookup Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    hasLoadedData = true;
                    // Sync both text fields so both City Name and Coordinates are populated
                    cityInputField.setText(currentCityDisplay);
                    coordInputField.setText(String.format("%.2f, %.2f", currentLatitude, currentLongitude));
                    refreshDisplayedUnits();
                    recordSearchHistory(currentCityDisplay, currentLatitude, currentLongitude);
                }
            }
        };

        worker.execute();
    }

    /**
     * Resolves latitude and longitude coordinates into a human-readable City and Country name.
     */
    private String reverseGeocodeCoordinates(double lat, double lon) {
        try {
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&localityLanguage=en",
                REVERSE_GEO_API_URL, lat, lon
            );
            String json = executeHttpGet(url);
            String city = extractJsonString(json, "city");
            if (city.isEmpty()) {
                city = extractJsonString(json, "locality");
            }
            if (city.isEmpty()) {
                city = extractJsonString(json, "principalSubdivision");
            }
            String countryCode = extractJsonString(json, "countryCode");
            if (!city.isEmpty() && !countryCode.isEmpty()) {
                return city + ", " + countryCode;
            } else if (!city.isEmpty()) {
                return city;
            }
        } catch (Exception ignored) {
            // Fallback gracefully if reverse geocoding is unreachable
        }
        return "Location";
    }

    /**
     * Executes an HTTP GET request with standard headers and timeout management.
     */
    private String executeHttpGet(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "JavaSwingWeatherApp/1.0 (CS1103; Key=" + API_KEY + ")");

        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP request failed with status code " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            connection.disconnect();
        }
        return response.toString();
    }

    /**
     * Parses the JSON payload into application state variables.
     */
    private void parseWeatherResponse(String json, String resolvedCity, double lat, double lon) throws Exception {
        String currentBlock = extractJsonObjectBlock(json, "current");
        if (currentBlock.isEmpty()) {
            throw new Exception("Unexpected API response format.");
        }

        currentCityDisplay = resolvedCity;
        currentLatitude = lat;
        currentLongitude = lon;
        currentTempCelsius = extractJsonDouble(currentBlock, "temperature_2m");
        currentHumidity = (int) Math.round(extractJsonDouble(currentBlock, "relative_humidity_2m"));
        currentWindKmh = extractJsonDouble(currentBlock, "wind_speed_10m");
        currentWeatherCode = (int) Math.round(extractJsonDouble(currentBlock, "weather_code"));

        // Parse 5-day forecast arrays from the "daily" JSON block
        String dailyBlock = extractJsonObjectBlock(json, "daily");
        String[] dates = extractJsonArray(dailyBlock, "time");
        String[] codes = extractJsonArray(dailyBlock, "weather_code");
        String[] maxTemps = extractJsonArray(dailyBlock, "temperature_2m_max");
        String[] minTemps = extractJsonArray(dailyBlock, "temperature_2m_min");

        forecastDays.clear();
        int count = Math.min(5, Math.min(dates.length, Math.min(codes.length, Math.min(maxTemps.length, minTemps.length))));
        for (int i = 0; i < count; i++) {
            String cleanDate = dates[i].replace("\"", "").trim();
            int code = Integer.parseInt(codes[i].trim());
            double maxC = Double.parseDouble(maxTemps[i].trim());
            double minC = Double.parseDouble(minTemps[i].trim());
            forecastDays.add(new ForecastDay(cleanDate, code, maxC, minC));
        }
    }

    /**
     * Updates all labels and forecast cards according to the selected measurement units.
     */
    private void refreshDisplayedUnits() {
        if (!hasLoadedData) {
            return;
        }

        boolean useFahrenheit = tempUnitSelector.getSelectedIndex() == 1;
        boolean useMph = windUnitSelector.getSelectedIndex() == 1;

        double displayedTemp = useFahrenheit ? (currentTempCelsius * 9.0 / 5.0) + 32.0 : currentTempCelsius;
        String tempUnitSymbol = useFahrenheit ? "°F" : "°C";

        double displayedWind = useMph ? currentWindKmh * 0.621371 : currentWindKmh;
        String windUnitSymbol = useMph ? "mph" : "km/h";

        cityTitleLabel.setText(currentCityDisplay);
        coordinatesSubtitleLabel.setText(String.format("Coordinates: Latitude %.2f°, Longitude %.2f°", currentLatitude, currentLongitude));
        temperatureLabel.setText(String.format("%.1f %s", displayedTemp, tempUnitSymbol));
        conditionLabel.setText("Condition: " + mapWeatherCodeToDescription(currentWeatherCode));
        humidityLabel.setText("Humidity: " + currentHumidity + "%");
        windSpeedLabel.setText(String.format("Wind Speed: %.1f %s", displayedWind, windUnitSymbol));

        weatherIconPanel.setWeatherCode(currentWeatherCode);

        // Rebuild 5-day forecast cards
        forecastGridPanel.removeAll();
        for (ForecastDay day : forecastDays) {
            double maxVal = useFahrenheit ? (day.maxTempCelsius * 9.0 / 5.0) + 32.0 : day.maxTempCelsius;
            double minVal = useFahrenheit ? (day.minTempCelsius * 9.0 / 5.0) + 32.0 : day.minTempCelsius;

            JPanel dayCard = new JPanel();
            dayCard.setLayout(new BoxLayout(dayCard, BoxLayout.Y_AXIS));
            dayCard.setBackground(new Color(255, 255, 255, 35));
            dayCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1),
                new EmptyBorder(8, 10, 8, 10)
            ));

            JLabel dateLbl = new JLabel(day.date);
            dateLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            dateLbl.setForeground(Color.WHITE);
            dateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel condLbl = new JLabel(mapWeatherCodeToDescription(day.weatherCode));
            condLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            condLbl.setForeground(new Color(255, 235, 150));
            condLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel rangeLbl = new JLabel(String.format("H: %.1f%s  L: %.1f%s", maxVal, tempUnitSymbol, minVal, tempUnitSymbol));
            rangeLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            rangeLbl.setForeground(Color.WHITE);
            rangeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            dayCard.add(Box.createVerticalGlue());
            dayCard.add(dateLbl);
            dayCard.add(Box.createVerticalStrut(5));
            dayCard.add(condLbl);
            dayCard.add(Box.createVerticalStrut(5));
            dayCard.add(rangeLbl);
            dayCard.add(Box.createVerticalGlue());

            forecastGridPanel.add(dayCard);
        }

        forecastGridPanel.revalidate();
        forecastGridPanel.repaint();
    }

    /**
     * Adds a timestamped entry containing both the City Name and Coordinates to the history list.
     */
    private void recordSearchHistory(String city, double lat, double lon) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String entry = String.format("[%s] %s (%.2f, %.2f)", timestamp, city, lat, lon);
        historyListModel.add(0, entry);
    }

    /**
     * Updates the dynamic background gradient based on the current hour of the day
     * or user theme selection.
     */
    private void updateBackgroundTheme() {
        int selection = timeThemeSelector != null ? timeThemeSelector.getSelectedIndex() : 0;
        String activePeriod;

        if (selection == 1) {
            activePeriod = "MORNING";
        } else if (selection == 2) {
            activePeriod = "AFTERNOON";
        } else if (selection == 3) {
            activePeriod = "EVENING";
        } else if (selection == 4) {
            activePeriod = "NIGHT";
        } else {
            int hour = LocalTime.now().getHour();
            if (hour >= 6 && hour < 12) {
                activePeriod = "MORNING";
            } else if (hour >= 12 && hour < 17) {
                activePeriod = "AFTERNOON";
            } else if (hour >= 17 && hour < 20) {
                activePeriod = "EVENING";
            } else {
                activePeriod = "NIGHT";
            }
        }

        switch (activePeriod) {
            case "MORNING":
                mainContainer.setGradientColors(new Color(56, 142, 200), new Color(144, 202, 249));
                if (timeOfDayBadgeLabel != null) timeOfDayBadgeLabel.setText("Dynamic Background: Morning Sky");
                break;
            case "AFTERNOON":
                mainContainer.setGradientColors(new Color(21, 101, 192), new Color(100, 181, 246));
                if (timeOfDayBadgeLabel != null) timeOfDayBadgeLabel.setText("Dynamic Background: Afternoon Sun");
                break;
            case "EVENING":
                mainContainer.setGradientColors(new Color(142, 36, 170), new Color(255, 112, 67));
                if (timeOfDayBadgeLabel != null) timeOfDayBadgeLabel.setText("Dynamic Background: Evening Sunset");
                break;
            default:
                mainContainer.setGradientColors(new Color(15, 23, 42), new Color(49, 46, 129));
                if (timeOfDayBadgeLabel != null) timeOfDayBadgeLabel.setText("Dynamic Background: Night Sky");
                break;
        }
    }

    /**
     * Maps standard WMO weather codes to human-readable descriptions.
     */
    private String mapWeatherCodeToDescription(int code) {
        if (code == 0) return "Clear Sky";
        if (code == 1 || code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast Clouds";
        if (code == 45 || code == 48) return "Foggy";
        if (code >= 51 && code <= 67) return "Rain Showers";
        if (code >= 71 && code <= 77) return "Snowfall";
        if (code >= 80 && code <= 82) return "Heavy Rain";
        if (code >= 95) return "Thunderstorm";
        return "Moderate Clouds";
    }

    // Helper methods for JSON parsing
    private String extractJsonObjectBlock(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private double extractJsonDouble(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    }

    private String[] extractJsonArray(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).split(",");
        }
        return new String[0];
    }

    /**
     * Custom panel that paints a smooth vertical gradient representing the time of day.
     */
    private static class BackgroundPanel extends JPanel {
        private Color topColor = new Color(21, 101, 192);
        private Color bottomColor = new Color(100, 181, 246);

        public void setGradientColors(Color top, Color bottom) {
            this.topColor = top;
            this.bottomColor = bottom;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    /**
     * Custom translucent rounded card panel for grouping UI sections cleanly.
     */
    private static class TranslucentCardPanel extends JPanel {
        public TranslucentCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(15, 23, 42, 135));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Custom component that renders vector weather icons using Java 2D Graphics.
     */
    private static class WeatherIconPanel extends JPanel {
        private int weatherCode = 0;

        public WeatherIconPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(220, 200));
        }

        public void setWeatherCode(int code) {
            this.weatherCode = code;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            if (weatherCode == 0) {
                drawSun(g2, cx, cy, 46);
            } else if (weatherCode == 1 || weatherCode == 2) {
                drawSun(g2, cx - 25, cy - 25, 34);
                drawCloud(g2, cx + 10, cy + 10, new Color(240, 245, 255));
            } else if (weatherCode >= 51 && weatherCode <= 67 || (weatherCode >= 80 && weatherCode <= 82)) {
                drawRainDrops(g2, cx, cy + 36);
                drawCloud(g2, cx, cy - 5, new Color(180, 195, 215));
            } else if (weatherCode >= 71 && weatherCode <= 77) {
                drawSnowflakes(g2, cx, cy + 38);
                drawCloud(g2, cx, cy - 5, new Color(220, 230, 245));
            } else if (weatherCode >= 95) {
                drawLightningBolt(g2, cx, cy + 22);
                drawCloud(g2, cx, cy - 8, new Color(110, 125, 150));
            } else {
                drawCloud(g2, cx - 12, cy - 12, new Color(190, 205, 220));
                drawCloud(g2, cx + 12, cy + 8, new Color(235, 240, 250));
            }

            g2.dispose();
        }

        private void drawSun(Graphics2D g2, int x, int y, int radius) {
            g2.setColor(new Color(255, 213, 79));
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 30);
                int x1 = x + (int) (Math.cos(angle) * (radius + 8));
                int y1 = y + (int) (Math.sin(angle) * (radius + 8));
                int x2 = x + (int) (Math.cos(angle) * (radius + 20));
                int y2 = y + (int) (Math.sin(angle) * (radius + 20));
                g2.drawLine(x1, y1, x2, y2);
            }
            g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }

        private void drawCloud(Graphics2D g2, int x, int y, Color cloudColor) {
            g2.setColor(cloudColor);
            g2.fillRoundRect(x - 55, y - 10, 110, 42, 35, 35);
            g2.fillOval(x - 40, y - 35, 50, 50);
            g2.fillOval(x - 5, y - 42, 58, 58);
        }

        private void drawRainDrops(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(100, 181, 246));
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int offset = -30; offset <= 30; offset += 20) {
                g2.drawLine(x + offset, y, x + offset - 8, y + 20);
            }
        }

        private void drawSnowflakes(Graphics2D g2, int x, int y) {
            g2.setColor(Color.WHITE);
            for (int offset = -30; offset <= 30; offset += 20) {
                g2.fillOval(x + offset - 4, y + 8, 8, 8);
                g2.fillOval(x + offset - 12, y + 20, 8, 8);
            }
        }

        private void drawLightningBolt(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(255, 238, 88));
            int[] px = {x - 5, x + 12, x + 2, x + 18, x - 12, x - 2};
            int[] py = {y - 5, y - 5, y + 15, y + 15, y + 40, y + 18};
            g2.fillPolygon(px, py, 6);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }
}