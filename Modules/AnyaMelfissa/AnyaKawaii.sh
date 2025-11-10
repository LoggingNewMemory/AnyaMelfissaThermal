#!/system/bin/sh

# Function to find all relevant thermal and logging properties
get_properties() {
    getprop | grep 'thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

get_properties | while read -r prop; do
    if [[ -n "$prop" && "$prop" == init.svc.* ]]; then
        service=${prop:9}
        resetprop -n running "$service"
    fi
done