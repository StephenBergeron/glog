#!/bin/bash

########################################################################
# Tired of searching for log content; and redirect; and logs and...
# Centralize and use a uniform format
########################################################################

# ######   #     #  ######   #        ###   #####
# #     #  #     #  #     #  #         #   #     #
# #     #  #     #  #     #  #         #   #
# ######   #     #  ######   #         #   #
# #        #     #  #     #  #         #   #
# #        #     #  #     #  #         #   #     #
# #         #####   ######   #######  ###   #####

#
# *Public* member
# [+] Change the glog context for easy filtering
export ___glog_context="glog_env"

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
export -f ___glog_init

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
export -f ___glog

#
# *Public* function
# [+] Redirect the stdout and stderr to a specific file
function ___glog_redirect() {
    glog_json_key_file "$1" "$2" "start" >> ${___glog_file} 2>&1
    "$1"                                 >> "$2" 2>&1
    glog_json_key_file "$1" "$2" "end"   >> ${___glog_file} 2>&1
}
export -f ___glog_redirect

#
# *Public* function
# [+] run the analysis tool on the key/message log
function glog_analysis() {
    cd $HOME/projects/glog
    lein test
    emacs ${___glog_file} ${___glog_file}.delta.json &
}
export -f glog_analysis

#
# *Public* function
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






# ######   ######   ###  #     #     #     #######  #######
# #     #  #     #   #   #     #    # #       #     #
# #     #  #     #   #   #     #   #   #      #     #
# ######   ######    #   #     #  #     #     #     #####
# #        #   #     #    #   #   #######     #     #
# #        #    #    #     # #    #     #     #     #
# #        #     #  ###     #     #     #     #     #######


#
# *Private* member
# [-] Change that filename if you need to change the log file location
export ___glog_file="$CACHEDIR/glog.json"

#
# *Private* function
# [-] The standard header of a json glog element
function glog_json_open() {
    printf "{\"ts\": \"$(date -u)\""
    printf ", \"ts_epoch\": \"$(date +%s)\""
}
export -f glog_json_open

#
# *Private* function
# [-] The standard footer of a json glog element
function glog_json_close() {
    printf ", \"PGHOST\": \"${PGHOST}\""
    printf ", \"db_server\": \"${db_server}\""
    printf ", \"PGDATABASE\": \"${PGDATABASE}\""
    printf ", \"db_name\": \"${CACHEDIR}\""
    printf ", \"db_dump\": \"%s\"" "$db_dump"

    printf ", \"dt_agent\": \"${dt_agent}\""
    printf ", \"dt_profile\": \"${dt_profile}\""

    printf ", \"openam_url\": \"${openam_url}\""
    printf ", \"redis_url\": \"${redis_url}\""
    printf ", \"buildnumber\": \"${buildnumber}\""
    printf ", \"wfm_cloudappname\": \"${wfm_cloudappname}\""

    printf ", \"simulation\": \"%s\"" "${simulation}"
    printf ", \"CACHEDIR\": \"${CACHEDIR}\""
    printf ", \"context\": \"${___glog_context}\""
    printf "}\n"
}
export -f glog_json_close



# ###  #     #  ###  #######
#  #   ##    #   #      #
#  #   # #   #   #      #
#  #   #  #  #   #      #
#  #   #   # #   #      #
#  #   #    ##   #      #
# ###  #     #  ###     #

___glog_init
