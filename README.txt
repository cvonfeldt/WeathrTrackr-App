# WeathrTrackr

WeathrTrackr is an Android app that allows users to track and save weather conditions at different locations. The app combines real-time weather data from OpenWeatherMap with Google Maps (both using REST API transfer) integration to provide a visual and interactive weather tracking experience.

## Key Features

- Real-time weather data display (temperature, conditions, etc.)
- Interactive Google Maps integration
- Location-based weather checkpoints
- Historical weather data tracking
- Location-based notifications
- Customizable settings for data storage

## Quick Usage Guide

### Navigation

- Open the navigation drawer by tapping the hamburger menu in the top-left corner or swiping right from the left edge of the screen.
- Available pages:
  - Map
  - History
  - Settings
  - About

### Basic Operations

#### Create a Checkpoint

1. Tap the **+** button on the Map screen.
2. The app retrieves the current location's weather and saves a checkpoint.

#### View Weather Data

- Tap any map marker to view its temperature and weather conditions.
- Open the **History** tab to view saved checkpoints in chronological order.

#### Settings

- Toggle auto-save.
- Clear all saved checkpoints.

## Challenges

Probably the biggest challenge I faced was implementing the swipe-to-dismiss gesture in the History screen. Coordinating the visual sliding animation with the actual deletion while preventing dismissed items from briefly reappearing required using a `dismissedItems` list to track deletions and a custom `swipeToDismiss` modifier to handle the gesture detection and offset calculations.
