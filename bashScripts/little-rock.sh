#!/bin/bash
#expect "$"
#HOST_NAME="montgomery"
spawn ssh little-rock
send -- "cd ~/cs455/PA1/src\r"
expect "$"
send -- "bash MessagingNode.sh\r"
interact 
