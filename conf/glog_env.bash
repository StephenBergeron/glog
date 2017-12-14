#!/bin/bash

########################################################################
# Tired of searching for log content; and redirect; and logs and..
# Centralize and uniformize
########################################################################

#
# *Public* member
# [+] Change the glog context for easy filtering
export ___glog_context="glog_env"

#
# *Private* member
# [-] Change that filename if you need to change the log file location
export ___glog_file="$CACHEDIR/glog.json"

#
# *Private* function
# [-] The standard header of a json glog element
function glog_json_open() {
    printf "{\"ts\": \"$(date --utc +"%m %d %Y %H:%M:%S")\""
}
export -f glog_json_open

#
# *Private* function
# [-] The standard footer of a json glog element
function glog_json_close() {
    printf ", \"wfm_cloudappname\": \"${wfm_cloudappname}\""
    printf ", \"simulation\": \"%s\"" "${simulation}"
    printf ", \"db_dump\": \"%s\"" "$db_dump"
    printf ", \"context\": \"${___glog_context}\"}\n"
}
export -f glog_json_close

#
# *Private* function
# [-] Define a key val entry for a file
function glog_json_key_file() {
    key=$1
    file=$2
    event=$3

    glog_json_open
    printf ", \"event\": \"%s\"" "$event"
    printf ", \"%s\": {\"file\":   \"%s\"}" "$key" "$file"
    glog_json_close
}
export -f glog_json_key_file


#
# *Public* function
# [+] Initialize
function ___glog_init() {
    if [ ! -f ${___glog_file} ]; then
        export ___glog_initial_pass=true
        touch $___glog_file
    else
        export ___glog_initial_pass=false
    fi
}

#
# *Public* function
# [+] the main glog utility - log message like key/value
function ___glog() {
    key=$1
    msg=$2
    glog_json_open  >> ${___glog_file} 2>&1
    printf ", \"%s\": \"%s\"" "$key" "$msg" >> ${___glog_file} 2>&1
    glog_json_close  >> ${___glog_file} 2>&1
}

#
# *Public* function
# [+] Redirect the stdout and stderr to a specific file
function ___glog_redirect() {
    glog_json_key_file "$1" "$2" "start" >> ${___glog_file} 2>&1
    "$1"                                 >> "$2" 2>&1
    glog_json_key_file "$1" "$2" "end"   >> ${___glog_file} 2>&1
}

#
# *Public* function
# [+] run the analysis tool on the key/message log
function glog_analysis() {
    cd $HOME/projects/glog
    lein test
}

export -f glog_analysis

export -f ___glog_init
export -f ___glog
export -f ___glog_redirect

___glog_init
