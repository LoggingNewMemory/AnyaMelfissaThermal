#!/system/bin/sh

# Function to find all relevant thermal and logging properties
get_properties() {
    getprop | grep -E 'logd|thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

# Iterate over the identified properties and start the corresponding services
get_properties | while read -r prop; do
    # Ensure the property name is not empty and is a service property
    if [[ -n "$prop" && "$prop" == init.svc.* ]]; then
        # Extract the service name (e.g., 'thermal-engine' from 'init.svc.thermal-engine')
        service=${prop:9}
        
        # Use 'setprop ctl.start' to start the service
        setprop ctl.start "$service"
    fi
done