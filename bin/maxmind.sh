#! /usr/bin/env bash

# Bash strict-mode
set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

## Required due to JVM busybox usage
IS_BB=0
agnostic_cp () {
	if [ $IS_BB == 1 ]; then
		cp -f "${2}" "${1}"
	else
		cp -ut "${1}" "${2}"
	fi
}

## Check sys + args
if [ -z "${1}" ]; then
  echo "An argument for location of DB files needs to be set"
  exit 1
fi

## retrieve license key
if [ -z "${MAXMIND_LICENSE_KEY:-}" ]; then
  echo "You must export an environment variable MAXMIND_LICENSE_KEY in order to use this script"
  exit 1
fi

mkdir -p "${1}/bin"
mkdir -p "${1}/db"
pushd "${1:-"."}" || exit 1

echo "Getting data from MaxMind"
curl -sSL "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&suffix=tar.gz&license_key=$MAXMIND_LICENSE_KEY" | tar zxv -C bin --wildcards '*.mmdb'
curl -sSL "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&suffix=tar.gz&license_key=$MAXMIND_LICENSE_KEY" | tar zxv -C bin --wildcards '*.mmdb'
curl -sSL "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country-CSV&suffix=zip&license_key=$MAXMIND_LICENSE_KEY" -o GeoLite2-Country-CSV.zip
unzip GeoLite2-Country-CSV.zip
rm -vf GeoLite2-Country-CSV.zip

## Copy the Country CSV data to a location on the drive
echo "Moving MaxMind GeoIP databases"
cp -vf GeoLite2-Country**/GeoLite2-Country-Locations-en.csv db/
cp -vf GeoLite2-Country**/GeoLite2-Country-Blocks* db/
rm -rvf GeoLite2-Country*

echo "Moving MaxMind GeoIP binaries"
## Extract the binary geoIP databases
mv -vf bin/GeoLite2-**/GeoLite2-C*.mmdb bin/
rm -rvf bin/GeoLite2-City_*
rm -rvf bin/GeoLite2-Country_*

popd || exit 1
