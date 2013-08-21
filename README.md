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
  

###Running the application on the Android emulator

COMING SOON


