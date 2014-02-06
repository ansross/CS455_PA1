#!/bin/bash
#expect "$"
#HOST_NAME="montgomery"
spawn ssh salt-lake-city
send -- "cd ~/cs455/PA1/src\r"
expect "$"
send -- "bash MessagingNode.sh\r"
interact 
