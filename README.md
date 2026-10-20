# 🌦️ **JAmbience** [![CodeFactor](https://www.codefactor.io/repository/github/mitriyf/jambience/badge)](https://www.codefactor.io/repository/github/mitriyf/jambience)
## 🍃 Add realistic weather conditions, thereby improving the environment
This plugin adds realistic weather conditions by receiving data via OpenWeatherAPI or a manual method.
- $ Versions 1.8.1-26+ are supported. Possibly version 1.7 as well, but it has not been tested.
- $ Has been tested on versions: 1.8.8, 1.19.4, 1.21, 26+. The best performance was noted on the following versions: 1.8.8, 1.12.2, and 1.16.5.
- $ On versions below 1.9, the result may not be what you expect.
- $ It is recommended to use the Paper core and its forks, otherwise the plugin functionality may be cut.
- $ Plugin Review (JAmbience v1.1 - Preview): https://youtu.be/60T4r6diyvY
# 💨 Weather conditions
- **Wind:**
  - Particle control (move in the direction of the wind. On some versions, they cannot be controlled correctly):
    <img width="640" height="237" alt="2026-10-0522-27-50-ezgif com-crop (2)" src="https://github.com/user-attachments/assets/2bda265c-a41c-4f2a-a40e-943a96bf1b3a" />
  - Player movement control (in the wind direction - faster, against the direction - slower):
    <img width="320" height="133" alt="2026-10-0522-34-35-ezgif com-video-to-gif-converter (1)" src="https://github.com/user-attachments/assets/c0f2a6f8-49d2-4900-b9f6-36e70ac83885" />

  - Wind volume (if the wind speed increases, the wind sound also increases):
    - Versions 1.9+ - flying elytra sound.
    - Versions 1.8 and below - flying dragon sound.
- **Visibility:**
  - The plugin controls the visibility of players depending on the weather:
    - Visibility 1 chunk:
      <img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/0e5295d9-ae37-479e-b055-6c7aa86ff91f" />
    - Visibility 2 chunks (this effect does not work on versions below 1.19):
      <img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/79b7b3eb-b1ff-413a-9937-71c9656b538e" />
    - Visibility 3 chunks or more (10 chunks in the photo):
      <img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/4571de11-1ef0-4fe0-b8f0-cf08129a5c9c" />
- **Freezing effect at certain temperatures** (progresses at low temperatures. Works only on versions 1.17+):
  <img width="1920" height="920" alt="image" src="https://github.com/user-attachments/assets/bc584a72-9ce8-4d8f-ba39-6e4c547b90ff" />
- **Particles:**
  - Rain:
    <img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/6304d67e-5d14-475d-9222-1ba9da7d7844" />
  - Snow:
    <img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/860df8b7-5b32-4b4f-9626-5f84f4966cb4" />
  - Seasonal particles (each season has its own particle color. Not controlled by the wind). You can see more details in the photos above or in the configuration.

## ⚡ Special weather conditions
- **Squall** (push in the direction of the wind. Depends on speed):
  <img width="600" height="249" alt="2026-10-0522-55-19-ezgif com-video-to-gif-converter" src="https://github.com/user-attachments/assets/df12cfac-bff3-4d86-8915-3ee7c6047148" />
- The rest were not thought out, as it is difficult to imagine with a normal game. You can track the weather status via the placeholder %jambience_status%

## ⚙️ Modes
- **OpenWeatherAPI (auto):**
  - Automatically getting up-to-date data from the website.
- **Manual (manual):**
  - Adjusts to the data from the configuration.

## 🛠️ Supported:
### 🔮 Support HEX (1.16+, it works on lower versions too, but displays incorrect colors), MiniMessage (1.18+).
### 🌐 Plugins:
- **PlaceholderAPI** (Scoreboard system).
  - You can download it here: [Releases · PlaceholderAPI/PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI/releases)
  - Don't forget to enable it in the plugin configuration.
  - Placeholders:
    - %jambience_name% - City name taken from the configuration.
    - %jambience_emoji% - Weather emoji taken from the configuration.
    - %jambience_windspeed% - Wind speed.
    - %jambience_color% - Weather status color.
    - %jambience_temperature% - Get the temperature.
    - %jambience_winddeg% - Get the wind degrees.
    - %jambience_visibility% - Visibility in chunks.
    - %jambience_time% - Current time taken from the configuration.
    - %jambience_event% - Translated event name from the configuration.
    - %jambience_amount% - Amount of particles in given ticks.
  - $ Ready-made scoreboard configuration:
    - <img width="270" height="307" alt="image" src="https://github.com/user-attachments/assets/8edde6e1-372e-43df-a931-1553c1b8bf77" />
    - Get here: https://github.com/mitriyf/JAmbience/blob/main/downloads/config.yml
### 📄 OpenWeatherAPI:
- You can insert a link so that the plugin starts working with your city:
  - [OpenWeather APIs for Weather, Energy, and Environment](https://openweathermap.org/)
  - Steps to connect:
    1. Visit the website above.
    2. Create an account on this service.
    3. Find the API key in the account settings.
    4. Select a location following the website instructions.
    5. Paste the link into the plugin configuration.
### 🔎 Checks:
- The plugin will automatically detect your server version so that it starts working correctly with your project.
- Whether the player is in GM/fly so as not to interfere with the operator's work.
- How many incorrect connections before switching to ManualMode.
- Access permissions check.
- And much more...
## ♾️ Functions:
### ⌨️ Command (/jambience):
- /jambience status - Get the plugin status.
- /jambience reload - Reload the plugin configuration.
- /jambience reloadSmoothly - Reload the configuration, but smoothly.
- /jambience update - Update information from OpenWeatherAPI.
- /jambience updateSmoothly - Update information, but smoothly.
- /jambience set value double/int - Set a weather parameter (experimental).
### 📖 Permissions:
- jambience.use - Can the player use the command?
### 🏃 Cut actions:
- [message] - Send a message to the player.
- [broadcast] - Broadcast a message to all players.
- [delay] - Delay in ticks.
- [console] - Send a command from the console.
- [log] - Send a log.
- Information:
  - Messages sent to the console may not replace placeholders or perform certain actions above.
### ⚙️ Configuration:
- Send actions to players using messages. (HEX support in version 1.16+, MiniMessage support in version 1.18+)
- Settings for supported functions (you can disable unnecessary ones).
## 📝 Configurations:
View them by navigating through the files using the following path: src\main\resources

# You can consider the rest of the possibilities when using the plugin.
