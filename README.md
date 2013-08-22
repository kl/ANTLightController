ANTLightController
==================

###Building with Android Studio

1. Download and extract Gradle (http://services.gradle.org/distributions/gradle-1.7-bin.zip) somewhere on your computer.
2. Download and install the latest version of Android Studio (http://developer.android.com/sdk/installing/studio.html)
3. Open Android Studio and click 'Check out from Version Control' -> 'Git'.
4. In the next dialog, put ```https://github.com/kl/ANTLightController.git``` in the 'Git Repository URL' field and click 'Clone'.
5. When it asks if you want to open the project click 'Yes'.
6. In the next dialog uncheck 'Use auto-import' and enter the folder where you extracted Gradle in the 'Gradle home' field. Click next and the project will now open and attempt to compile, but it will fail to do so at this point.
7. Now there will be a new folder called ```ANTLightController``` in your working directory. Open this folder and create a new folder in it called ```libs```.
7. Download ```android_antlib.jar``` and put it in the ```libs``` folder - http://www.thisisant.com/resources/android-ant-sdk-package/ (the jar is in the ```API/ANT v4.0.0``` folder)
8. Back in Android Studio rebuild the project by selecting 'Build' -> 'Rebuild Project' from the main menu. The build should now succeed.

__Because Android Studio is still in a beta version, the Gradle build system is not completely integrated with the IDE. Therefore, to make the IDE recognize Guava library do the following:__

1. Click on 'File' -> 'Project Structure...' in the main menu.
2. Under 'Project Settings' select 'Libraries' and click the green + button.
3. Select 'From Maven...'.
4. In the next dialog put ```com.google.guava:guava:14.0.1``` in the search field.
5. Check the 'Download to:' checkbox and put in the path of the 'libs' folder you created before. Click 'OK'. All libraries should now be recognized by the IDE.

###Running the application on a physical Android device

To run the app on a physical device the following prerequisites must be met:
  1. The device must have built in ANT support or if an ANT USB stick is to be used have the ANT USB Service Android application installed (https://play.google.com/store/apps/details?id=com.dsi.ant.usbservice)
  2. The ANT Radio Service Android application must be installed on the device (https://play.google.com/store/apps/details?id=com.dsi.ant.service.socket)
  3. The device must run an Android version between 2.1 to 4.1. Android versions lower than 2.1 are not supported by the application code, and Android versions higher than 4.1 are not currently supported by the ANT Radio Service. If an ANT USB stick is used, the device must run Android 3.1 or higher.

Once the prerequisites have been met create a run-configuration that targets the physical device like this:

1. Click the button with the green Android icon to the left of the 'Play' button in the Android Studio menu.
2. Select 'Edit configurations...'
3. Click the + icon and select 'Android Application'.
4. Under 'Module' select 'ANTLightController' and under 'Target Device' select 'USB device'. Click OK.
5. Click the 'Play' button and the application will be built and then run on the USB device.

###Pulling changes to your local machine from within Android Studio

If you have cloned the repository and set up the Android Studio project as described you can easily download changes made to this repository to your local machine from within Android Studio, without using Git from the command line. To do so, click 'VCS' -> 'Update Project' in the main menu. In the next dialog select 'Merge' and 'Using Stash' and then click OK.

###Running the application on the Android emulator

1. Open the SDK Manager (from Android Studio click 'Tools' -> 'Android' -> 'SDK Manager' in the main menu).
2. In the SKD Manager, click 'Tools' -> 'Manage Add-on Sites...' in the main menu.
3. Click 'User Defined Sites' -> 'New...'.
4. Enter the following URL in the URL field and click OK: ```http://thisisant.com/android/emulator/repository.xml```
5. Back in the main SDK Manager window look under 'Android 2.3.3 (API10)' and check the 'ANT Radio' checkbox. Also make sure that the SDK platform has been installed, if not check it's box too.
6. Next click 'Install packages...' and wait for the installation to finish. You can now close the SDK Manager.
7. Open the AVD Manager (from Android Studio click 'Tools' -> 'Android' -> 'AVD Manager' in the main menu).
8. Click the 'New...' button.
9. In the next dialog select one of the pre-defined device profiles, for example 'Nexus One'. In the 'Target' field make sure to select 'ANT Radio (ANT Wireless) - API Level 10'. Click 'OK'.
10. Start the emulator by selecting it and clicking the 'Start...' button.
11. Once the emulator has booted, open the pre-installed app 'ANT Emulator Configuration'.
12. In the 'IP Address' field enter the local IP address of your computer (on Windows you can get this info by running the ```ipconfig``` command on the command line). In the 'Port' field enter ```9050```.
13. Click 'Reconnect' and then click the home button to get back to the startup screen.
14. If you have not done so already, download the ANT Android SDK package (http://www.thisisant.com/resources/android-ant-sdk-package/). In the SDK package folder, locate the file 'Android_ANTRadioService_4-0-0.apk' in the 'Services' folder.
15. Go back to Android Studio and start the 'Monitor' tool ('Tools' -> 'Android' -> 'Monitor (DDMS included)').
16. If the emulator is still running, you should see it the left side of the screen under the 'Devices' tab. Select the emulator and then click the 'File Explorer' tab. Expand the 'data' folder by clicking the arrow to the left of it, and select the 'app' folder. Next click the button with an arrow pointing right in the upper right corner of the screen labeled 'Push a file unto the device'.
17. In the next dialog, navigate to the 'Android_ANTRadioService_4-0-0.apk' file and select 'Open'. You can now close the 'Monitor' tool.
18. Download and extract the ANT Emulator Bridge Tool (http://www.thisisant.com/resources/ant-android-emulator-bridge-tool/).
19. Start the bridge tool and click 'Connect to USB' (make sure that an ANT USB stick is connected to the computer first). Now the bridge tool should find the emulator and it should say "Found emulator" in the status window. If not, try disconnecting and connecting from the USB stick a couple of times and/or restarting the emulator.
20. Finally go back to Android Studio and click the 'Play' button. In the next dialog select the emulator and click 'OK'. The app should now be uploaded to and started on the emulator.


