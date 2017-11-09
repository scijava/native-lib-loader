#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_fb177a2a35c8_key $encrypted_fb177a2a35c8_iv
