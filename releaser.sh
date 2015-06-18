#!/bin/bash
#
# This script has two main parts:
# PART I: checkout a clean version of the code and build it
# PART II: create a database from the loading sheets
#
# arguments: $0 [clearfirst] [tool] [database] [experimental]
# 

TOOL="no"
DB="no"
EXP="no"
while [[ $# > 0 ]]; do
    case $1 in
        c*) rm -rf "$WORKSPACE/gito";;
        t*) TOOL="yes";;
        d*) DB="yes";;
        e*) EXP="yes";;
    esac
  shift
done

echo "tool? $TOOL; db? $DB"

mkdir -p "$WORKSPACE/gito"
cd "$WORKSPACE/gito"

CLASSPATH="$WORKSPACE/gito/classpath"
JOURNAL="$WORKSPACE/gito/database.jnl"
rm -rf "$JOURNAL" "$CLASSPATH"

######
# 
# This is PART I
#
######
if [ "yes" = $TOOL ]; then
	if [ -d va-semoss ]; then
		cd va-semoss
		git pull
		cd ..
	else
		#git clone git@github.com:Mantech/va-semoss.git
		git clone https://github.com/Mantech/va-semoss.git/
	fi

	if [ -d vcamp ]; then
		cd vcamp
		git pull
		cd ..
	else
		#git clone git@github.com:Mantech/vcamp.git
		git clone https://github.com/Mantech/vcamp.git/
	fi

	if [ -d VA_MainDB ]; then
		cd VA_MainDB
		git pull
		cd ..
	else
		#git clone git@github.com:Mantech/VA_MainDB.git
		git clone https://github.com/Mantech/VA_MainDB.git/
	fi

	cd va-semoss
	mvn clean install
	cd ..

	cd vcamp
	# are we doing an experimental build?
	if [ "yes" = $EXP ]; then
            mv src/main/resources/images/V-CAMP-Dev-Ops-Splash.png \
                src/main/resources/images/V-CAMP-Splash.png
            DATE=$(date --iso-8601)
            sed -i "s|<ReleaseDate/>|$DATE|" src/main/resources/help/release.txt
            sed -i "s|<Version/>|Experimental Release|" \
                src/main/resources/help/release.txt
	fi
	mvn clean package
  cd ..
fi

######
# 
# This is PART II
#
######
if [ "yes" = $DB ]; then
  cd vcamp
	# make a classpath containing everything in the lib dir
	for lib in $(ls target/lib/*); do echo -n ":$lib"; done > "$CLASSPATH"

	# the loading sheets are in a directory with a space, which fouls
	# up the command line tool...so move the files someplace without spaces
	rm -rf /tmp/LS
	mkdir -p /tmp/LS
	find ../VA_MainDB/$(date +%Y)/$(date +%B_%Y|tr a-z A-Z)/Loading\ Sheets \
		-name \*.xlsx | while read "file";do 
		newfile=/tmp/LS/$(basename "$file"|tr \  _)
		cp "$file" "$newfile"
	done

	# the filename is something like /tmp/LS/VA_MainDBv19.0_June_2015_Node_Loader.xlsx
	# so figure out what version we're talking about
	DBVERSION=$(ls /tmp/LS/*Node*|xargs basename|cut -dv -f2-|cut -d_ -f1)

	# build the new database with the latest loader files
	java -Xmx750M -classpath $(cat "$CLASSPATH") gov.va.semoss.util.CLI \
		-insights ../VA_MainDB/$(date +%Y)/$(date +%B_%Y|tr a-z A-Z)/Insights/*.ttl \
		-vocab ../va-semoss/src/main/resources/models/*.ttl \
			src/main/resources/models/*.ttl \
		-load $(ls /tmp/LS/*) \
		-organization "V-CAMP" \
		-poc "Ryan Bobko <ryan@ostrich-emulators.com>" \
		-title "Veterans Affairs Main v$DBVERSION" \
		-summary "The V-CAMP Enterprise Capabilities Database" \
		-out "$JOURNAL"

	#rename the database file to be more descriptive
	newfilename=$(dirname "$JOURNAL")/$(ls /tmp/LS/*Node*|xargs basename|cut -d_ -f-2).jnl
	mv "$JOURNAL" "$newfilename"
fi

rm -rf /tmp/LS $CLASSPATH

