# Toggimmer

## Summary
Toggimmer toggles and dims selected lights. Toggimmer is a SmartApp designed to work with wireless switches like the Cooper RF9500 (link to device handler below) that operater wirelessly only and are not wired into your lights. Toggimmer allows you to select 1 to many dimmers and control 1 to many dimmable lights without having to worry about keeping these dimmers in sync with each other or the lights. You can 100% replicate the functionality of this SmartApp with something like CoRE. The reason for this apps existance is that I felt something as powerful as CoRE was overkill for this kind of function.

## Features
1. Coarse to fine dimming. When lowering the light level, if the light level is drops to 25% or lower the app transitions to dimming down by 5% increments. When raising the light level, if the light level is below 25% the app will increase the light level by 5% until it reaches 25% and then it increases. Take a look at the code to see the actual values for dimming.

## Installation via GitHub Integration
1. Open SmartThings IDE in your web browser and log into your account.
2. Click on the "My SmartApps" section in the navigation bar.
3. Click on "Settings".
4. Click "Add New Repository".
5. Enter "ericvitale" as the namespace.
6. Enter "ST-Loggimmer" as the repository.
7. Hit "Save".
8. Select "Update from Repo" and select "ST-Loggimmer".
9. Select "loggimmer.groovy".
10. Check "Publish" and hit "Execute".

## Manual Installation (if that is your thing)
1. Open SmartThings IDE in your web browser and log into your account.
2. Click on the "My SmartApps" section in the navigation bar.
3. Click the blue "+ New SmartApp" button at the bottom of the page.
4. Click "From Code".
5. Paste in the code from "loggimmer.groovy" and hit "Create".
6. Click the "Publish" --> "For Me".
7. The app will appear on your app under "Marketplace" --> "My Apps"

## Preferences
1. Dimmers
2. Lights
3. Log Level - Enter: TRACE, DEBUG, INFO, WARN, ERROR

## Cooper RF9500 Beast Device Handler
https://github.com/ericvitale/ST-CooperRF9500Beast
