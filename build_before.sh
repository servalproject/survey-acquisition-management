#!/bin/bash

#build the project with ndk (this erases the delorme libraries)
~/android-ndk-r10d/ndk-build

#extract the libraries from delorme
cd ~/survey-acquisition-management
tar zxvf delorme.tgz

#erase all the documents starting with '._' and the armeabi-v7a
cd ~/survey-acquisition-management/libs 
rm ._inreachcore.jar
rm -r armeabi-v7a
cd armeabi
rm ._libinreachcorelib.so
