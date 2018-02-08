# AndroidTestApk

this an tool apk to test your own apk through android test framework.

## usage


### step1

build the [AndroidTestPlugin](https://github.com/heavy-james/AndroidTestPlugin) on your system.

update the local maven path in root-project's build.gradle file.

### step2

modify the local.properties, fill with right values of your own.

### step3

place the keystore file to the path specified in the local.properties, which you did in step1.

if your target app use debug sign config, you just use debug flavor of this apk, key store file and properties in local.properties file can be ignored.

### step4

find instrumentation node in AndroidManifest.xml, set the value of targetPackage attribute to your own.

### step5

gradle sync and assembleDebug or assembleRelease depends on your situation. install the apk to your

device then you will be able to use the gradle script to control your android test. please see the

[AndroidTestPlugin](https://github.com/heavy-james/AndroidTestPlugin) to get more information.