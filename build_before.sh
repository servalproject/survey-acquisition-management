#!/bin/bash

#build the project with ndk (this erases the delorme libraries)
~/android-ndk-r10d/ndk-build

#extract the libraries from delorme
tar zxvf delorme.tgz

#erase all the documents starting with '._' and the armeabi-v7a
cd libs
rm ._inreachcore.jar
rm -r armeabi-v7a
cd armeabi
rm ._libinreachcorelib.so
