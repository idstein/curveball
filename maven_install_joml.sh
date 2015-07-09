#!/usr/bin/env bash

mkdir mvn_install_joml_temp
pushd mvn_install_joml_temp
 
curl -L https://github.com/JOML-CI/JOML/archive/master.zip -o joml.zip || { echo "wget call failed, exiting."; exit 1; }
if [ ! -f joml.zip ]; then echo "Download failed, could not find file: lwjgl.zip"; exit 1; fi
 
unzip -o joml.zip || { echo "unzip call failed, exiting."; exit 1; }

cd JOML-master
mvn install -DskipTests -DskipJar
 
popd
rm -rf mvn_install_joml_temp