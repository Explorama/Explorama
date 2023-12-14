#!/bin/bash
file="woco.css"
targetFile="icons.edn"
echo "{" > $targetFile
while read line; do
  if [[ $line =~ icon__ ]] ; then 
   tmp=${line//"icon__"/$'\2'}
   IFS=$'\2' read -ra ICON <<< "$tmp"
   tmp=${ICON[1]//" "/$'\2'}
   IFS=$'\2' read -ra ICON <<< "$tmp"
   tmp=${ICON[0]//","/$'\2'}
   IFS=$'\2' read -ra ICON <<< "$tmp"
   echo ":${ICON[0]} {:group \"Todo\" :class \"icon__${ICON[0]}\"}" >> $targetFile
  fi
done <"$file"
echo "}" >> $targetFile
