# SkypeForBusinessAndroidVideoIssue
This is a sample project related to https://github.com/OfficeDev/skype-android-app-sdk-samples/issues/41, based on Microsoft Android Sample HealhCareApp, to show an issue with the automatic start the video when someone join's the metting.

# Problem
Sometimes the method `onCanStartVideoServiceChanged(boolean canStartVideoService)` is never called with value true.

# Effect
In the App, I'm trying to archive the automatic start of the video, when someone join's the skype for business metting.
When the method is called with true, the video work's out of the box, otherwise it doesn't, and the only way to get it to work, is to click the button pause/resume the video.

# Where do I set the url metting 
https://github.com/4brunu/SkypeForBusinessAndroidVideoIssue/blob/master/app/src/main/java/com/microsoft/office/sfb/healthcare/SkypeCall.java#L63

# How do I run this?
1 - Downlaod the SkypeForBusiness SDK from [here](https://msdn.microsoft.com/en-us/skype/appsdk/download)
2 - Copy the SDK and helper files
3 - Open project in Android Studio
4 - [Set the url metting](https://github.com/4brunu/SkypeForBusinessAndroidVideoIssue/blob/master/app/src/main/java/com/microsoft/office/sfb/healthcare/SkypeCall.java#L63)

