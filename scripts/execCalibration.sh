#!/bin/bash

calibrationFile=$1
logPath=$2
fromIndiv=$3
toIndiv=$4

./scripts/compile.sh

mkdir -p $logPath

/usr/bin/java -cp "./platform/src/:./multiscalemodel/src/:./bin/:./deps/*:./deps/j3dport/*" edu.usf.experiment.CalibrationPreExperiment $calibrationFile $logPath > $logPath/preProc.txt 2>&1
idMessage=`sbatch -a $fromIndiv-$toIndiv ./scripts/execOneCalib.sh $logPath`
slurmId=`echo $idMessage | cut -d " " -f 4`
sbatch --dependency=afterok:$slurmId scripts/postProcessCalib.sh $logPath
